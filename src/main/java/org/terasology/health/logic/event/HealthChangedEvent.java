// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * This event (or a subtype) is sent whenever health changes
 */
public class HealthChangedEvent implements Event {
    private final EntityRef instigator;
    private final int change;

    public HealthChangedEvent(EntityRef instigator, int change) {
        this.instigator = instigator;
        this.change = change;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    /**
     * Method to get the amount by which health has changed.
     *
     * @return The amount by which health changed. This is capped (by the implementing method), so if the entity
     *         received 9999 damage and only had 10 health, only 10 points damage will be inflicted.
     */
    public int getHealthChange() {
        return change;
    }
}
