// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gestalt.naming.Name;
import org.terasology.module.health.components.HealthComponent;
import org.terasology.module.health.systems.RegenAuthoritySystem;
import org.terasology.module.health.components.RegenComponent;

import static org.terasology.module.health.systems.RegenAuthoritySystem.BASE_REGEN;

/**
 * Send this event to active regeneration of health points for an entity.
 * <p>
 * The targeted entity must have a {@link HealthComponent} for this event to have an
 * effect.
 * <p>
 * The {@link RegenAuthoritySystem} manages regeneration effects and updates affected
 * components every "tick". A tick currently occurs once per second.
 *
 * @see RegenAuthoritySystem
 * @see RegenComponent
 * @see HealthComponent
 */
public class RegisterRegenEvent implements Event {
    /**
     * Identifier for the cause of this regeneration effect activation.
     */
    public final String id;
    /**
     * Amount of additional health points per tick.
     */
    public float value;
    /**
     * Effect duration in seconds.
     */
    public float durationInSeconds;

    /**
     * Active base regeneration for the target entity.
     * <p>
     * Base regeneration (or "natural regeneration") is active until the entity is back at full health. The regeneration
     * value is typically derived from {@link HealthComponent#regenRate}.
     */
    public RegisterRegenEvent(float value) {
        this.value = value;
        this.id = BASE_REGEN;
        this.durationInSeconds = -1;
    }

    public RegisterRegenEvent(Name id) {
        this.id = id.toString();
        this.durationInSeconds = -1;
    }

    /**
     * Activate additional regeneration for the target entity.
     * <p>
     * The {@code id} is intended to uniquely identify the reason for this regeneration effect. For instance, it can
     * denote that regeneration was trigger by a potion, caused by a spell, or any other condition.
     * <p>
     * The {@code value} denotes the additional amount of health points regenerated with each regeneration tick while
     * this effect is active.
     * <p>
     * The {@code endTime} denotes after how many seconds the regeneration effect phases out. Once the time span is
     * passed the effect will be removed from the entity.
     * <p>
     * For instance, the following constructor call could belong to a small healing potion which restores 8 health
     * points every second over a duration of 5 seconds (e.g., healing 40 health points).
     * <pre>
     * {@code
     * new RegisterRegenEvent("potions:smallHealingPotion", 8, 5);
     * }
     * </pre>
     *
     * @param id identifier for the cause of this effect
     * @param value additional health generation amount per tick
     * @param durationInSeconds the effect duration in seconds
     */
    public RegisterRegenEvent(String id, float value, float durationInSeconds) {
        this.id = id;
        this.value = value;
        this.durationInSeconds = durationInSeconds;
    }
}
