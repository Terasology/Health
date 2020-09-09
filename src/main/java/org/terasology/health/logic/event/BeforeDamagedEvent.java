// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableValueModifiableEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;

/**
 * This event is sent to allow damage to be modified or cancelled, before it is processed.
 * <br><br>
 * Damage modifications are accumulated as additions/subtractions (modifiers) and multipliers.
 */
public class BeforeDamagedEvent extends AbstractConsumableValueModifiableEvent {
    private final Prefab damageType;
    private final EntityRef instigator;
    private final EntityRef directCause;

    public BeforeDamagedEvent(int baseDamage, Prefab damageType, EntityRef instigator, EntityRef directCause) {
        super(baseDamage);
        this.damageType = damageType;
        this.instigator = instigator;
        this.directCause = directCause;
    }

    public Prefab getDamageType() {
        return damageType;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getDirectCause() {
        return directCause;
    }
}
