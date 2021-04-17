// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.health.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.module.health.components.HealthComponent;
import org.terasology.module.health.events.ChangeMaxHealthEvent;
import org.terasology.module.health.events.DoDamageEvent;
import org.terasology.module.health.events.MaxHealthChangedEvent;
import org.terasology.nui.widgets.UIIconBar;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class HealthAuthoritySystem extends BaseComponentSystem {
    @In
    NUIManager nuiManager;
    @In
    PrefabManager prefabManager;

    /**
     * Sends out an immutable notification event when maxHealth of a character is changed.
     */
    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void changeMaxHealth(ChangeMaxHealthEvent event, EntityRef player, HealthComponent health) {
        int oldMaxHealth = health.maxHealth;
        health.maxHealth = (int) event.getResultValue();
        Prefab maxHealthReductionDamagePrefab = prefabManager.getPrefab("Health:maxHealthReductionDamage");
        player.send(new DoDamageEvent(Math.max(health.currentHealth - health.maxHealth, 0),
                maxHealthReductionDamagePrefab));
        player.send(new MaxHealthChangedEvent(oldMaxHealth, health.maxHealth));
        player.saveComponent(health);
    }

    /**
     * Reacts to the {@link MaxHealthChangedEvent} notification event. Is responsible for the change in maximum number
     * of icons in the Health Bar UI.
     */
    @ReceiveEvent
    public void onMaxHealthChanged(MaxHealthChangedEvent event, EntityRef player) {
        //TODO: as this is updating UI, I think this should not be in an AUTHORITY system...
        //      this potentially requires the event to be distributed to the client (owner)
        UIIconBar healthBar = nuiManager.getHUD().find("healthBar", UIIconBar.class);
        healthBar.setMaxIcons(event.getNewValue() / 10);
    }
}
