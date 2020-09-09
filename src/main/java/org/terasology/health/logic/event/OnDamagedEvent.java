// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;

/**
 * This event is sent after damage has been dealt to an entity.
 */
public class OnDamagedEvent extends HealthChangedEvent {
    private final Prefab damageType;
    private final int fullAmount;

    public OnDamagedEvent(int fullAmount, int change, Prefab damageType, EntityRef instigator) {
        super(instigator, change);
        this.fullAmount = fullAmount;
        this.damageType = damageType;
    }

    public int getDamageAmount() {
        return fullAmount;
    }

    public Prefab getType() {
        return damageType;
    }

}
