// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.health.events;

public class RegisterRegenEvent {
    private final String id;


    public RegisterRegenEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
