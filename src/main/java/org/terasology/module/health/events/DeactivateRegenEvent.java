// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.events;

import org.terasology.engine.entitySystem.event.Event;

import static org.terasology.module.health.systems.RegenAuthoritySystem.BASE_REGEN;

public class DeactivateRegenEvent implements Event {
    public String id;

    public DeactivateRegenEvent() {
        id = BASE_REGEN;
    }

    public DeactivateRegenEvent(String id) {
        this.id = id;
    }
}
