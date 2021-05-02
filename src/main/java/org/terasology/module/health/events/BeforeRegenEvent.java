// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.health.events;

import org.terasology.engine.entitySystem.event.AbstractConsumableValueModifiableEvent;
import org.terasology.gestalt.naming.Name;

/**
 * A <i>collector event</i> to allow interested systems to contribute to a regeneration action.
 *
 * A separate collector event is sent for each registered regeneration id. The resulting amount per regeneration id is
 * at minimum 0 (no negative effects due to regeneration). The final regeneration amount is computed from the sum of
 * all amounts per regeneration id.
 *
 * To inflict damage caused by regeneration send a separate {@link DoDamageEvent} instead.
 */
public class BeforeRegenEvent extends AbstractConsumableValueModifiableEvent {

    final Name id;

    public BeforeRegenEvent(Name id, float baseValue) {
        super(baseValue);
        this.id = id;
    }

    public BeforeRegenEvent(String id, float baseValue) {
        this(new Name(id), baseValue);
    }

    public Name getId() {
        return id;
    }
}
