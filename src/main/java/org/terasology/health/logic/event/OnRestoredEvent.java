// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * This event is sent after the entity is restored. Final event of restoration logic.
 */
public class OnRestoredEvent extends HealthChangedEvent {
    /**
     * The amount by which the entity is restored.
     */
    private final int amount;

    public OnRestoredEvent(int amount, EntityRef instigator) {
        super(instigator, amount);
        this.amount = amount;
    }

    public int getRegenAmount() {
        return amount;
    }

}
