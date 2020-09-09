// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.ForceBlockActive;

/**
 * This component is added by the authority to temporary block entities so that they can persist for a bit while the
 * block is being damaged.
 */
@ForceBlockActive
public class BlockDamagedComponent implements Component {
}
