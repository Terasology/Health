/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.health;

import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.health.event.ActivateRegenEvent;
import org.terasology.logic.health.event.DeactivateRegenEvent;
import org.terasology.logic.health.event.OnFullyHealedEvent;
import org.terasology.registry.In;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This system handles the natural regeneration of entities with HealthComponent.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class RegenAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    public static final String ALL_REGEN = "all";
    public static final String BASE_REGEN = "baseRegen";
    public static final String WAIT = "wait";

    /**
     * Integer storing when to check each effect.
     */
    private static final int CHECK_INTERVAL = 200;

    /**
     * Long storing when entities are to be regenerated again.
     */
    private static long nextTick;

    // Stores when next to check for new value of regen, contains only entities which are being regenerated
    private SortedSetMultimap<Long, EntityRef> regenSortedByTime = TreeMultimap.create(Ordering.natural(),
            Ordering.arbitrary());

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
        // Execute regen schedule
        if (currentTime > nextTick) {
            invokeRegenOperations(currentTime);
            nextTick = currentTime + CHECK_INTERVAL;
        }
    }

    private void invokeRegenOperations(long currentWorldTime) {
        // Contains all the entities with current time crossing EndTime
        List<EntityRef> operationsToInvoke = new LinkedList<>();
        Iterator<Long> regenTimeIterator = regenSortedByTime.keySet().iterator();
        long processedTime;
        while (regenTimeIterator.hasNext()) {
            processedTime = regenTimeIterator.next();
            if (processedTime > currentWorldTime) {
                break;
            }
            operationsToInvoke.addAll(regenSortedByTime.get(processedTime));
            regenTimeIterator.remove();
        }

        // Add new regen if present, or remove RegenComponent
        operationsToInvoke.stream().filter(EntityRef::exists).forEach(regenEntity -> {
            RegenComponent regen = regenEntity.getComponent(RegenComponent.class);
            regenSortedByTime.removeAll(regen.soonestEndTime);
            regen.removeCompleted(currentWorldTime);
            if (regen.regenValue.isEmpty()) {
                regenEntity.removeComponent(RegenComponent.class);
            } else {
                regenEntity.saveComponent(regen);
                regenSortedByTime.put(regen.soonestEndTime, regenEntity);
            }
        });

        // Regenerate the entities with EndTime greater than Current time
        regenerate(currentWorldTime);
    }

    private void regenerate(long currentTime) {
        List<Long> regenToBeRemoved = new LinkedList<>();
        for (EntityRef entity : regenSortedByTime.values()) {
            RegenComponent regen = entity.getComponent(RegenComponent.class);
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health != null && health.nextRegenTick < currentTime) {
                health.currentHealth += regen.getRegenValue();
                health.nextRegenTick = currentTime + 1000;
                if (health.currentHealth >= health.maxHealth) {
                    regenToBeRemoved.add(regen.soonestEndTime);
                    entity.removeComponent(RegenComponent.class);
                    entity.send(new OnFullyHealedEvent(entity));
                }
                entity.saveComponent(health);
            }
        }
        for (Long endTime : regenToBeRemoved) {
            regenSortedByTime.removeAll(endTime);
        }
    }

    @ReceiveEvent
    public void onRegenAdded(ActivateRegenEvent event, EntityRef entity, RegenComponent regen,
                             HealthComponent health) {
        // Remove previous scheduled regen, new will be added by addRegenToScheduler()
        regenSortedByTime.remove(regen.soonestEndTime, entity);
        addRegenToScheduler(event, entity, regen, health);
    }

    @ReceiveEvent
    public void onRegenAddedWithoutComponent(ActivateRegenEvent event, EntityRef entity, HealthComponent health) {
        if (!entity.hasComponent(RegenComponent.class)) {
            RegenComponent regen = new RegenComponent();
            regen.soonestEndTime = Long.MAX_VALUE;
            entity.addComponent(regen);
            addRegenToScheduler(event, entity, regen, health);
        }
    }

    private void addRegenToScheduler(ActivateRegenEvent event, EntityRef entity, RegenComponent regen,
                                     HealthComponent health) {
        if (event.id.equals(BASE_REGEN)) {
            // setting endTime to MAX_VALUE because natural regen happens till entity fully regenerates
            regen.addRegen(BASE_REGEN, health.regenRate, Long.MAX_VALUE);
            regen.addRegen(WAIT, -health.regenRate,
                    time.getGameTimeInMs() + (long) (health.waitBeforeRegen * 1000));
        } else {
            regen.addRegen(event.id, event.value, time.getGameTimeInMs() + (long) (event.endTime * 1000));
        }
        regenSortedByTime.put(regen.soonestEndTime, entity);
    }

    @ReceiveEvent
    public void onRegenRemoved(DeactivateRegenEvent event, EntityRef entity, HealthComponent health,
                               RegenComponent regen) {
        regenSortedByTime.remove(regen.soonestEndTime, entity);
        if (event.id.equals(ALL_REGEN)) {
            entity.removeComponent(RegenComponent.class);
        } else {
            regen.removeRegen(event.id);
            if (event.id.equals(BASE_REGEN)) {
                regen.removeRegen(WAIT);
            }
            if (!regen.regenValue.isEmpty()) {
                regenSortedByTime.put(regen.soonestEndTime, entity);
            }
        }
    }
}
