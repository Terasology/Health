// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.destruction.EngineDamageTypes;
import org.terasology.engine.logic.players.event.OnPlayerRespawnedEvent;
import org.terasology.health.logic.event.BeforeRestoreEvent;
import org.terasology.health.logic.event.DoDamageEvent;
import org.terasology.health.logic.event.DoRestoreEvent;
import org.terasology.health.logic.event.OnFullyHealedEvent;
import org.terasology.health.logic.event.OnRestoredEvent;
import org.terasology.health.logic.event.RestoreFullHealthEvent;
import org.terasology.math.TeraMath;

/**
 * This system takes care of restoration of entities with HealthComponent. To increase the health of an entity, send
 * DoRestoreEvent
 * <p>
 * Logic flow for restoration: - DoRestoreEvent - BeforeRestoreEvent - (HealthComponent saved) - OnRestoredEvent -
 * OnFullyHealedEvent (if healed to full health)
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class RestorationAuthoritySystem extends BaseComponentSystem {

    @ReceiveEvent
    public void onRestore(DoRestoreEvent event, EntityRef entity, HealthComponent health) {
        // Ignore 0 restoration
        if (event.getAmount() == 0) {
            return;
        }
        BeforeRestoreEvent beforeRestoreEvent = entity.send(new BeforeRestoreEvent(event.getAmount(), entity));
        if (!beforeRestoreEvent.isConsumed()) {
            int modifiedRestoreAmount = TeraMath.floorToInt(beforeRestoreEvent.getResultValue());
            if (modifiedRestoreAmount > 0) {
                restore(entity, health, modifiedRestoreAmount);
            } else {
                // Cause "healing" damage to entity if modified value of restoration is negative
                entity.send(new DoDamageEvent(-modifiedRestoreAmount, EngineDamageTypes.HEALING.get()));
            }
        }
    }

    private void restore(EntityRef entity, HealthComponent health, int restoreAmount) {
        int cappedHealth = Math.min(health.maxHealth, health.currentHealth + restoreAmount);
        int cappedRestoreAmount = cappedHealth - health.currentHealth;
        health.currentHealth = cappedHealth;
        entity.saveComponent(health);
        entity.send(new OnRestoredEvent(cappedRestoreAmount, entity));
        if (cappedHealth == health.maxHealth) {
            entity.send(new OnFullyHealedEvent(entity));
        }
    }

    @ReceiveEvent
    public void onRestoreFullHealthEvent(RestoreFullHealthEvent event, EntityRef entity, HealthComponent health) {
        restoreFullHealth(entity, health);
    }

    @ReceiveEvent
    public void onRespawn(OnPlayerRespawnedEvent event, EntityRef entity, HealthComponent healthComponent) {
        restoreFullHealth(entity, healthComponent);
    }

    private void restoreFullHealth(EntityRef entity, HealthComponent healthComponent) {
        healthComponent.currentHealth = healthComponent.maxHealth;
        entity.saveComponent(healthComponent);
    }
}
