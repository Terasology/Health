// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.events;

import com.google.common.base.Preconditions;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.OwnerEvent;

/**
 * This event is sent after the entity is restored. Final event of restoration logic.
 */
@OwnerEvent
public class OnRestoredEvent extends OnHealthChangedEvent {

    public OnRestoredEvent(int amount, EntityRef instigator) {
        super(amount, instigator);
        Preconditions.checkArgument(amount >= 0, "restoration amount must be non-negative - use OnDamagedEvent instead");
    }

    public int getRestorationAmount() {
        return change;
    }

}
