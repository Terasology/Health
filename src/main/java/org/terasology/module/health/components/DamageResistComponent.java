// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.components;

import org.terasology.engine.entitySystem.Component;
import java.util.Map;

public class DamageResistComponent implements Component {
   public Map<String, Float> damageTypes;
}
