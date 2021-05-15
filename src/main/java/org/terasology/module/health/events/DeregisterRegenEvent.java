// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.events;

import org.terasology.engine.entitySystem.event.Event;

import static org.terasology.module.health.systems.RegenAuthoritySystem.BASE_REGEN;

public class DeregisterRegenEvent implements Event {
    public String id;

    public DeregisterRegenEvent(String id) {
        this.id = id;
    }
}
