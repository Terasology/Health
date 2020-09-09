// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.event.Event;

import static org.terasology.health.logic.RegenAuthoritySystem.BASE_REGEN;

public class DeactivateRegenEvent implements Event {
    public String id;

    public DeactivateRegenEvent() {
        id = BASE_REGEN;
    }

    public DeactivateRegenEvent(String id) {
        this.id = id;
    }
}
