// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.health;


import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.integrationenvironment.ModuleTestingHelper;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.logic.characters.events.AttackEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.module.health.components.BlockDamagedComponent;
import org.terasology.module.health.components.RegenComponent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationEnvironment(dependencies = "Health")
@Disabled("The test has some weird timing issues which will sporadically fail it. (see #70)")
public class BlockTest {
    private static final Vector3ic BLOCK_LOCATION = new Vector3i(0, 0, 0).add(0, -1, 0);

    private static final long BUFFER = 200; // 200 ms buffer time

    @In
    protected WorldProvider worldProvider;
    @In
    protected Time time;
    @In
    protected BlockManager blockManager;
    @In
    protected BlockEntityRegistry blockEntityRegistry;
    @In
    protected ModuleTestingHelper helper;

    @Test
    public void blockRegenTest() {
        Block testBlock = blockManager.getBlock("health:test");

        helper.forceAndWaitForGeneration(BLOCK_LOCATION);
        worldProvider.setBlock(BLOCK_LOCATION, testBlock);

        EntityRef testBlockEntity = blockEntityRegistry.getExistingBlockEntityAt(BLOCK_LOCATION);

        // Attack on block, damage of 1 inflicted
        testBlockEntity.send(new AttackEvent(testBlockEntity, testBlockEntity));

        // Make sure that the attack actually caused damage and started regen
        assertFalse(helper.runUntil(BUFFER, () -> testBlockEntity.hasComponent(BlockDamagedComponent.class)), "time out");
        assertTrue(testBlockEntity.hasComponent(BlockDamagedComponent.class));

        // Regen effects starts delayed after 1 second by default, so let's wait
        assertFalse(helper.runUntil(1000 + BUFFER, () -> testBlockEntity.hasComponent(RegenComponent.class)), "time out");
        assertTrue(testBlockEntity.hasComponent(RegenComponent.class));

        // Time for regen is 1 sec, 0.2 sec for processing buffer time
        assertFalse(helper.runUntil(3000 + BUFFER, () -> !testBlockEntity.hasComponent(BlockDamagedComponent.class)), "time out");

        // On regen, health is fully restored, and BlockDamagedComponent is removed from the block
        assertFalse(testBlockEntity.hasComponent(BlockDamagedComponent.class));
    }
}
