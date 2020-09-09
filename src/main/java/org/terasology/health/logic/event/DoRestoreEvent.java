// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * The event sent to restore health to entity. Starting point of Restoration cycle.
 */
public class DoRestoreEvent implements Event {
    /**
     * The amount of health points being restored.
     */
    private final int amount;
    /**
     * The entity that caused the restoration
     */
    private final EntityRef instigator;

    public DoRestoreEvent(int amount) {
        this(amount, EntityRef.NULL);
    }

    public DoRestoreEvent(int amount, EntityRef entity) {
        this.amount = amount;
        this.instigator = entity;
    }

    public int getAmount() {
        return amount;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
