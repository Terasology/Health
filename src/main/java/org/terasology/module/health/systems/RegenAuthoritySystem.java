// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.systems;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.registry.In;
import org.terasology.math.TeraMath;
import org.terasology.module.health.components.HealthComponent;
import org.terasology.module.health.components.RegenComponent;
import org.terasology.module.health.events.ActivateRegenEvent;
import org.terasology.module.health.events.BeforeRegenEvent;
import org.terasology.module.health.events.DeactivateRegenEvent;
import org.terasology.module.health.events.OnFullyHealedEvent;
import org.terasology.naming.Name;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This system handles the natural regeneration of entities with HealthComponent.
 * <p>
 * Regeneration is applied once every second (every 1000ms) per {@link RegenComponent}. The active components are
 * checked five times per second (every 200ms) whether they are due for application.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class RegenAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    public static final String ALL_REGEN = "all";
    public static final String BASE_REGEN = "baseRegen";

    private static final Logger logger = LoggerFactory.getLogger(RegenAuthoritySystem.class);

    /**
     * Integer storing when to check each effect.
     */
    private static final int CHECK_INTERVAL = 200;

    /**
     * The in-game time in ms at which entities are to be regenerated again.
     */
    private static long nextTick;

    // Stores when next to check for new value of regen, contains only entities which are being regenerated
    private final SortedSetMultimap<Long, EntityRef> regenSortedByTime =
            TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());

    @In
    private Time time;
    @In
    private EntityManager entityManager;

    /**
     * For every update, check to see if the time's been over the CHECK_INTERVAL. If so, verify if a REGENERATION_TICK
     * has passed for every regeneration effect.
     *
     * @param delta The time (in seconds) since the last engine update.
     */
    @Override
    public void update(float delta) {
        final long currentTime = time.getGameTimeInMs();
        if (currentTime > nextTick) {
            invokeRegenOperations(currentTime);
            nextTick = currentTime + CHECK_INTERVAL;
        }
    }


    /**
     * @param currentWorldTime the current in-game time in ms
     */
    private void invokeRegenOperations(long currentWorldTime) {
        // Contains all the entities with current time crossing EndTime
        List<EntityRef> entitiesWithExpiringRegenActions = new LinkedList<>();
        Iterator<Long> regenTimeIterator = regenSortedByTime.keySet().iterator();
        long endTime;
        while (regenTimeIterator.hasNext()) {
            endTime = regenTimeIterator.next();
            if (endTime == -1) {
                continue;
            }
            if (endTime > currentWorldTime) {
                break;
            }
            entitiesWithExpiringRegenActions.addAll(regenSortedByTime.get(endTime));
            regenTimeIterator.remove();
        }

        // Add new regen if present, or remove RegenComponent
        entitiesWithExpiringRegenActions.stream()
                .filter(EntityRef::exists)
                .filter(entityRef -> entityRef.hasComponent(RegenComponent.class))
                .forEach(regenEntity -> {
                    RegenComponent regen = regenEntity.getComponent(RegenComponent.class);
                    regenSortedByTime.remove(regen.soonestEndTime, regenEntity);
                    removeCompleted(currentWorldTime, regen);
                    if (regen.regenValue.isEmpty()) {
                        regenEntity.removeComponent(RegenComponent.class);
                    } else {
                        regenEntity.saveComponent(regen);
                        regenSortedByTime.put(findSoonestEndTime(regen), regenEntity);
                    }
                });

        // Regenerate the entities with EndTime greater than Current time
        regenerate(currentWorldTime);
    }

    private void regenerate(long currentTime) {
        Map<EntityRef, Long> regenToBeRemoved = new HashMap<>();
        for (EntityRef entity : regenSortedByTime.values()) {
            RegenComponent regen = entity.getComponent(RegenComponent.class);
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (regen == null || health == null) {
                logger.debug("Entity '{}' without RegenComponent or HealthComponent scheduled for regeneration - skipping", entity);
                continue;
            }
            if (health.nextRegenTick < currentTime) {
                health.currentHealth += getRegenValue(regen);
                health.nextRegenTick = currentTime + 1000;
                if (health.currentHealth >= health.maxHealth) {
                    regenToBeRemoved.put(entity, regen.soonestEndTime);
                    if (hasBaseRegenOnly(regen) || regen.regenValue.isEmpty()) {
                        entity.removeComponent(RegenComponent.class);
                    }
                    entity.send(new OnFullyHealedEvent(entity));
                }
                entity.saveComponent(health);
            }
        }
        for (EntityRef entity : regenToBeRemoved.keySet()) {
            regenSortedByTime.remove(regenToBeRemoved.get(entity), entity);
        }
    }

    /**
     * Send out a {@link BeforeRegenEvent} collector event to ask systems for collaboration on determining the
     * regeneration value.
     *
     * @param id the regeneration id to collect the current amount for
     * @param entity the entity the regeneration action affects
     * @return the collector event after event processing
     */
    private BeforeRegenEvent collectRegenValueFor(Name id, EntityRef entity) {
        BeforeRegenEvent beforeRegenEvent = new BeforeRegenEvent(id, 0);
        entity.send(beforeRegenEvent);
        return beforeRegenEvent;
    }

    /**
     * Send out <i>collector events</i> for all registered regeneration ids and apply the resulting amount to the
     * entity's health component.
     * <p>
     * The final amount is adjusted by the time {@code delta}.
     *
     * <pre>
     *     δ × ∑ max(BeforeRegenValue(id), 0) ∀ registered id
     * </pre>
     *
     * @param entity the entity targeted by the regeneration action
     * @param health the entity's health component
     * @param regen the entity's regen component tracking registered regeneration ids
     * @param delta the time delta to adjust the regeneration amount for
     */
    private void collectAndApplyRegenFor(EntityRef entity, HealthComponent health, RegenComponent regen, float delta) {
        // retrieve the set of registered regeneration ids for the given entity
        Set<Name> registeredRegenIds =
                regen.regenEndTime.values().stream()
                        .map(Name::new) //TODO: store registered ids as Name
                        .collect(Collectors.toSet());
        // send out a collector event for each of the registered ids, filter out consumed ids, and sum up capped result
        Float collectedRegenValue = registeredRegenIds.stream()
                .map(id -> collectRegenValueFor(id, entity))
                .filter(event -> !event.isConsumed())
                .reduce(0f, (accumulator, event) -> accumulator + event.getResultValue(), Float::sum);
        // compute the time-adjusted regeneration amount and update the entity's health component
        int regenAmount = Math.round(collectedRegenValue * delta);
        RestorationAuthoritySystem.restore(entity, health, regenAmount);
    }

    private void removeCompleted(Long currentTime, RegenComponent regen) {
        List<String> toBeRemoved = new LinkedList<>();
        Long endTime;
        Iterator<Long> iterator = regen.regenEndTime.keySet().iterator();
        while (iterator.hasNext()) {
            endTime = iterator.next();
            if (endTime <= currentTime) {
                if (endTime != -1) {
                    // Add all string ids to be removed from regenComponent.regenValue
                    toBeRemoved.addAll(regen.regenEndTime.get(endTime));
                    // Remove from regenComponent.regenEndTime sorted map
                    iterator.remove();
                }
            } else {
                break;
            }
        }
        for (String id : toBeRemoved) {
            regen.regenValue.remove(id);
        }
        regen.soonestEndTime = findSoonestEndTime(regen);
    }


    @ReceiveEvent
    public void onRegenAdded(ActivateRegenEvent event, EntityRef entity, RegenComponent regen,
                             HealthComponent health) {
        if (event.value != 0) {
            logger.debug("activate regen '{}' for entity {} with regen component", event.id, entity);

            // Remove previous scheduled regen, new will be added by addRegenToScheduler()
            regenSortedByTime.remove(regen.soonestEndTime, entity);
            addRegenToScheduler(event, regen);
            regenSortedByTime.put(regen.soonestEndTime, entity);
        }
    }

    @ReceiveEvent
    public void onRegenAddedWithoutComponent(ActivateRegenEvent event, EntityRef entity, HealthComponent health) {
        logger.debug("activate regen '{}' for entity {}", event.id, entity);

        if (!entity.hasComponent(RegenComponent.class)) {
            logger.debug("creating new regen component for entity {}", entity);
            RegenComponent regen = new RegenComponent();
            regen.soonestEndTime = Long.MAX_VALUE;
            addRegenToScheduler(event, regen);
            entity.addComponent(regen);
        }
    }

    private void addRegenToScheduler(ActivateRegenEvent event, RegenComponent regen) {
        if (event.value != 0) {
            // handle indefinite regeneration actions
            final long endTime = event.endTime < 0 ? -1 : time.getGameTimeInMs() + (long) (event.endTime * 1000);
            regen.regenValue.put(event.id, event.value);
            regen.regenEndTime.put(endTime, event.id);
            if (endTime > 0) {
                regen.soonestEndTime = Math.min(regen.soonestEndTime, endTime);
            }
        }
    }

    @ReceiveEvent
    public void onRegenComponentAdded(OnActivatedComponent event, EntityRef entity, RegenComponent regen) {
        if (!regen.regenValue.isEmpty()) {
            logger.debug("register regen component for entity {} at {}", entity, regen.soonestEndTime);
            regenSortedByTime.put(regen.soonestEndTime, entity);
        } else {
            entity.removeComponent(RegenComponent.class);
        }
    }

    @ReceiveEvent
    public void onRegenRemoved(DeactivateRegenEvent event, EntityRef entity, HealthComponent health,
                               RegenComponent regen) {
        regenSortedByTime.remove(regen.soonestEndTime, entity);
        if (event.id.equals(ALL_REGEN)) {
            entity.removeComponent(RegenComponent.class);
        } else {
            removeRegen(event.id, regen);
            if (!regen.regenValue.isEmpty()) {
                regenSortedByTime.put(regen.soonestEndTime, entity);
            }
        }
    }

    private Long findSoonestEndTime(RegenComponent regen) {
        Long endTime = 0L;
        Iterator<Long> iterator = regen.regenEndTime.keySet().iterator();
        while (iterator.hasNext()) {
            endTime = iterator.next();
            if (endTime > 0) {
                return endTime;
            }
        }
        return endTime;
    }


    private void removeRegen(String id, RegenComponent regen) {
        Long removeKey = 0L;
        for (Long key : regen.regenEndTime.keySet()) {
            for (String value : regen.regenEndTime.get(key)) {
                if (id.equals(value)) {
                    removeKey = key;
                    break;
                }
            }
        }
        regen.regenEndTime.remove(removeKey, id);
        regen.regenValue.remove(id);
    }

    @VisibleForTesting
    int getRegenValue(RegenComponent regen) {
        float totalValue = regen.remainder;
        for (float value : regen.regenValue.values()) {
            totalValue += value;
        }
        totalValue = Math.max(0, totalValue);
        regen.remainder = totalValue % 1;
        return TeraMath.floorToInt(totalValue);
    }

    public boolean hasBaseRegenOnly(RegenComponent regen) {
        return (regen.regenValue.size() == 1) && (regen.regenValue.containsKey(BASE_REGEN));
    }
}
