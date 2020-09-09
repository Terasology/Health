// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.destruction.EngineDamageTypes;

/**
 * This event should be sent to cause damage to an entity.
 */
public class DoDamageEvent implements Event {
    private final int amount;
    private final Prefab damageType;
    private final EntityRef instigator;
    private final EntityRef directCause;

    public DoDamageEvent(int amount) {
        this(amount, EngineDamageTypes.DIRECT.get());
    }

    public DoDamageEvent(int amount, Prefab damageType) {
        this(amount, damageType, EntityRef.NULL);
    }

    public DoDamageEvent(int amount, Prefab damageType, EntityRef instigator) {
        this(amount, damageType, instigator, EntityRef.NULL);
    }

    /**
     * @param amount The amount of damage being caused
     * @param damageType The type of the damage being dealt
     * @param instigator The instigator of the damage (which entity caused it)
     * @param directCause Tool used to cause the damage
     */
    public DoDamageEvent(int amount, Prefab damageType, EntityRef instigator, EntityRef directCause) {
        this.amount = amount;
        this.damageType = damageType;
        this.instigator = instigator;
        this.directCause = directCause;
    }

    public int getAmount() {
        return amount;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public Prefab getDamageType() {
        return damageType;
    }

    public EntityRef getDirectCause() {
        return directCause;
    }
}
