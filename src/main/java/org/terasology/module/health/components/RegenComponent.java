// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.components;

import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.module.health.events.ActivateRegenEvent;
import org.terasology.module.health.events.DeactivateRegenEvent;
import org.terasology.module.health.time.Instant;
import org.terasology.naming.Name;

import java.util.HashMap;
import java.util.Map;

/**
 * [INTERNAL] This component is managed by {@link org.terasology.module.health.systems.NewRegenAuthoritySystem}.
 *
 * <p>
 * To register an indefinite regeneration action in a prefab you can give it a regen component and pre-fill the
 * registered actions with the respective id. To denote an indefinite timestamp, i.e., a never ending action, use a
 * negative value (usually {@code -1}).
 * <p>
 * For instance, to pre-register the base regeneration with id {@code health:baseRegen} include the following in your
 * prefab:
 *
 * <pre>
 * {
 *   "Regen": {
 *     "actions": [
 *       { "key": "health:baseRegen", "value": -1 }
 *     ]
 *   }
 * }
 * </pre>
 *
 * @see ActivateRegenEvent
 * @see DeactivateRegenEvent
 */
public class RegenComponent implements Component {
    /**
     * The timestamp in in-game time (ms) when the next regeneration action ends.
     */
    @Replicate
    public long soonestEndTime = Long.MAX_VALUE;

    /**
     * Mapping from regeneration action ids to the regeneration value.
     */
    @Replicate
    public Map<String, Float> regenValue = new HashMap<>();

    /**
     * Registered regeneration action ids associated to their end time.
     */
    @Replicate
    public SortedSetMultimap<Long, String> regenEndTime = TreeMultimap.create(Ordering.natural(),
            Ordering.arbitrary());

    /**
     * The decimal place of the last regen tick before rounding the amount to integer.
     * <p>
     * This is to compensate for inaccuracy when computing the regeneration amount in integers. The remainder is picked
     * up at the next iteration of the {@link org.terasology.module.health.systems.NewRegenAuthoritySystem}.
     */
    @Replicate
    public float remainder;

    /**
     * Registered regeneration action ids and their expiration timestamp.
     */
    @Replicate
    public Map<Name, Instant> actions = new HashMap<>();
}
