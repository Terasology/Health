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

import java.util.HashMap;
import java.util.Map;

/**
 * Not for direct access! Use regen events instead.
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

    @Replicate
    public float remainder;
}
