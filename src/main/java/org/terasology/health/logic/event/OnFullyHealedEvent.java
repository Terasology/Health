// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * Event sent upon an entity reaching full health if previously on less than full health.
 */
public class OnFullyHealedEvent implements Event {
    private final EntityRef instigator;

    public OnFullyHealedEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

}
