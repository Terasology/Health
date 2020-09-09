// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.event.AbstractValueModifiableEvent;

/**
 * This Event is sent out whenever a system wants to alter the maxHealth of an entity.
 */
public class ChangeMaxHealthEvent extends AbstractValueModifiableEvent {
    public ChangeMaxHealthEvent(float baseValue) {
        super(baseValue);
    }
}
