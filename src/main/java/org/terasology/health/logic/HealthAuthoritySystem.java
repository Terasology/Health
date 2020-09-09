// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.health.logic;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.health.logic.event.ChangeMaxHealthEvent;
import org.terasology.health.logic.event.DoDamageEvent;
import org.terasology.health.logic.event.MaxHealthChangedEvent;
import org.terasology.nui.widgets.UIIconBar;

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
        UIIconBar healthBar = nuiManager.getHUD().find("healthBar", UIIconBar.class);
        healthBar.setMaxIcons(event.getNewValue() / 10);
    }
}
