// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic.event;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.health.logic.HealthComponent;
import org.terasology.health.logic.RegenAuthoritySystem;
import org.terasology.health.logic.RegenComponent;

import static org.terasology.health.logic.RegenAuthoritySystem.BASE_REGEN;

/**
 * Send this event to active regeneration of health points for an entity.
 * <p>
 * The targeted entity must have a {@link HealthComponent} for this event to have an effect.
 * <p>
 * The {@link RegenAuthoritySystem} manages regeneration effects and updates affected components every "tick". A tick
 * currently occurs once per second.
 *
 * @see RegenAuthoritySystem
 * @see RegenComponent
 * @see HealthComponent
 */
public class ActivateRegenEvent implements Event {
    /**
     * Identifier for the cause of this regeneration effect activation.
     */
    public String id;
    /**
     * Amount of additional health points per tick.
     */
    public float value;
    /**
     * Effect duration in seconds.
     */
    public float endTime;

    /**
     * Active base regeneration for the target entity.
     * <p>
     * Base regeneration (or "natural regeneration") is active until the entity is back at full health. The regeneration
     * value is defined by {@link HealthComponent#regenRate}.
     */
    public ActivateRegenEvent() {
        id = BASE_REGEN;
        //TODO(skaldarnar): Why do we explicitly set the end time to 0 but don't set the value? The regeneration system
        //                  will replace this value by -1 to model indefinite duration.
        endTime = 0;
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
     * new ActivateRegenEvent("potions:smallHealingPotion", 8, 5);
     * }
     * </pre>
     *
     * @param id identifier for the cause of this effect
     * @param value additional health generation amount per tick
     * @param endTime the effect duration in seconds
     */
    public ActivateRegenEvent(String id, float value, float endTime) {
        this.id = id;
        this.value = value;
        this.endTime = endTime;
    }
}
