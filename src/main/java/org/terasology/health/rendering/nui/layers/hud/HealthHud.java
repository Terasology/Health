// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.rendering.nui.layers.hud;

import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.health.logic.HealthComponent;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIIconBar;

public class HealthHud extends CoreHudWidget {

    @In
    private LocalPlayer localPlayer;

    @Override
    public void initialise() {
        UIIconBar healthBar = find("healthBar", UIIconBar.class);
        healthBar.bindValue(new ReadOnlyBinding<Float>() {
            @Override
            public Float get() {
                HealthComponent healthComponent = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
                if (healthComponent != null) {
                    return (float) healthComponent.currentHealth;
                }
                return 0f;
            }
        });
        healthBar.bindMaxValue(new ReadOnlyBinding<Float>() {
            @Override
            public Float get() {
                HealthComponent healthComponent = localPlayer.getCharacterEntity().getComponent(HealthComponent.class);
                if (healthComponent != null) {
                    return (float) healthComponent.maxHealth;
                }
                return 0f;
            }
        });
    }
}
