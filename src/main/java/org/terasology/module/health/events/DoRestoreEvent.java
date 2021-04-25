// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.events;

import com.google.common.base.Preconditions;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * The event sent to restore health to entity. Starting point of Restoration cycle.
 */
public class DoRestoreEvent implements Event {
    /** The amount of health points being restored. */
    private int amount;
    /** The entity that caused the restoration */
    private EntityRef instigator;

    public DoRestoreEvent(int amount) {
        this(amount, EntityRef.NULL);
    }

    public DoRestoreEvent(int amount, EntityRef entity) {
        Preconditions.checkArgument(amount >= 0, "restoration amount must be non-negative - use DoDamageEvent instead");
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
