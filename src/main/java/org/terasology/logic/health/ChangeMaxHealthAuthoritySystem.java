// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.logic.health;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.logic.health.event.ChangeMaxHealthEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.widgets.UIIconBar;

public class ChangeMaxHealthAuthoritySystem {
    NUIManager nuiManager;

    @ReceiveEvent
    public void changeMaxHealth(ChangeMaxHealthEvent event, EntityRef player,HealthComponent health) {
        ControlWidget healthHUD = nuiManager.getHUD().getHUDElement("healthHUD");
        UIIconBar healthBar = healthHUD.find("healthBar", UIIconBar.class);
        healthBar.setMaxIcons(event.getNewMaxHealth()/10);
    }


    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, HealthComponent health) {
        ControlWidget healthHUD = nuiManager.getHUD().getHUDElement("healthHUD");
        UIIconBar healthBar = healthHUD.find("healthBar", UIIconBar.class);
        healthBar.setMaxIcons(5);
    }
}
