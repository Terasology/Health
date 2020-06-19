// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.logic.health.event;

import org.terasology.entitySystem.event.Event;

/**
 * This Event is sent out whenever a system wants to alter the maxHealth of an entity.
 */
public class ChangeMaxHealthEvent implements Event {
    private int newMaxHealth;

    public ChangeMaxHealthEvent(int newMaxHealth) {
        this.newMaxHealth = newMaxHealth;
    }

    public int getNewMaxHealth() {
        return newMaxHealth;
    }
}
