// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableValueModifiableEvent;

/**
 * This event is sent to an entity to allow modification and cancellation of restoration.
 * <p>
 * Modifications are accumulated as modifiers (additions), multipliers and postModifiers (additions after multipliers).
 */
public class BeforeRestoreEvent extends AbstractConsumableValueModifiableEvent {
    /**
     * The entity which is being restored.
     */
    private final EntityRef entity;

    public BeforeRestoreEvent(int amount, EntityRef entity) {
        super(amount);
        this.entity = entity;
    }

    public EntityRef getEntity() {
        return entity;
    }

}
