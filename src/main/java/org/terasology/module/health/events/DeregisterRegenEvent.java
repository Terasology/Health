// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gestalt.naming.Name;
import org.terasology.module.health.components.RegenComponent;
import org.terasology.module.health.systems.RegenAuthoritySystem;

import static org.terasology.module.health.systems.RegenAuthoritySystem.BASE_REGEN;

public class DeregisterRegenEvent implements Event {
    public Name id;

    public DeregisterRegenEvent(Name id) {
        this.id = id;
    }
}
