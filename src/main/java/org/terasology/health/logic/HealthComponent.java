// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.nui.properties.TextField;

/**
 * Provides Health to entity attached with HealthComponent. Contains the parameters required for all health related
 * events.
 */
public class HealthComponent implements Component {

    /**
     * Maximum allowed health, capped to this if exceeding this value.
     */
    @Replicate
    public int maxHealth = 20;

    /**
     * Falling speed threshold above which damage is inflicted to entity.
     */
    @Replicate
    public float fallingDamageSpeedThreshold = 20;

    /**
     * Horizontal speed threshold above which damage is inflicted to entity.
     */
    @Replicate
    public float horizontalDamageSpeedThreshold = 20;

    /**
     * The multiplier used to calculate damage when horizontal or vertical threshold is crossed.
     */
    @Replicate
    public float excessSpeedDamageMultiplier = 10f;


    /**
     * The current value of health.
     */
    @Replicate
    @TextField
    public int currentHealth = 20;

    /**
     * Used to send Destroy event when health breaches zero.
     */
    public boolean destroyEntityOnNoHealth;

    // Regen Info

    /**
     * Amount of health restored in each regeneration tick.
     */
    @Replicate
    public float regenRate;

    /**
     * Time delay before regeneration starts.
     */
    @Replicate
    public float waitBeforeRegen;

    /**
     * Next tick time that will trigger regeneration.
     */
    public long nextRegenTick;

}
