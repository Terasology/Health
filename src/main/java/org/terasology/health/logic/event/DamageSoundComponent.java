// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic.event;

import com.google.common.collect.Lists;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.Component;

import java.util.List;

/**
 * Contains list of damage sounds, one of which is played when entity is damaged.
 */
public class DamageSoundComponent implements Component {
    public List<StaticSound> sounds = Lists.newArrayList();
}
