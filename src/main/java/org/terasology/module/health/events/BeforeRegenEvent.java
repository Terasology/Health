// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.health.events;

import org.terasology.engine.entitySystem.event.AbstractConsumableValueModifiableEvent;

public class BeforeRegenEvent extends AbstractConsumableValueModifiableEvent {

    private final String id;

    protected BeforeRegenEvent(String id, float baseValue) {
        super(baseValue);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
