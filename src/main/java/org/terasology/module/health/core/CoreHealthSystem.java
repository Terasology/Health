// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.health.core;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.gestalt.naming.Name;
import org.terasology.module.health.components.HealthComponent;
import org.terasology.module.health.events.BeforeRegenEvent;

/**
 * A system adding core functionality and default behavior based on the mechanics defined in this module.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class CoreHealthSystem {

    public static final Name BASE_REGEN = new Name("health:baseRegen");

    /**
     * Contribute to regeneration actions by listening for {@link CoreHealthSystem#BASE_REGEN} and adding the regen rate
     * from the entity's {@link HealthComponent} to the result.
     * @param event collector event for regeneration actions
     * @param entity the entity affected by the regeneration action
     * @param health the entity's health component
     */
    @ReceiveEvent
    public void beforeBaseRegen(BeforeRegenEvent event, EntityRef entity, HealthComponent health) {
        if (event.getId().equals(BASE_REGEN)) {
            event.add(health.regenRate);
        }
    }
}
