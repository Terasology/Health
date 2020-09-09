// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic;

import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

import java.util.HashMap;
import java.util.Map;

public class RegenComponent implements Component {
    @Replicate
    public long soonestEndTime = Long.MAX_VALUE;
    @Replicate
    public Map<String, Float> regenValue = new HashMap<>();
    @Replicate
    public SortedSetMultimap<Long, String> regenEndTime = TreeMultimap.create(Ordering.natural(),
            Ordering.arbitrary());
    @Replicate
    public float remainder;
}
