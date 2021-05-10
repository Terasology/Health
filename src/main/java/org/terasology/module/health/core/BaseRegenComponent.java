// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.health.core;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

/**
 * Configuration for the base regeneration as defined by {@link BaseRegenAuthoritySystem#BASE_REGEN}.
 */
public class BaseRegenComponent implements Component {

    /**
     * Amount of health points restored per (real-time) second.
     */
    @Replicate
    public float regenRate;

    /**
     * The cool down in seconds before the base regeneration applies after taking damage.
     */
    @Replicate
    public float waitBeforeRegen;

    /** The last game time in milliseconds at which the entity received damage. */
    public long lastHitTimestampInMs;
}
