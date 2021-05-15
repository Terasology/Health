// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.health.systems;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.registry.In;
import org.terasology.module.health.components.HealthComponent;
import org.terasology.module.health.components.RegenComponent;
import org.terasology.module.health.core.BaseRegenAuthoritySystem;
import org.terasology.module.health.core.BaseRegenComponent;
import org.terasology.module.health.events.RegisterRegenEvent;
import org.terasology.module.health.events.DeregisterRegenEvent;
import org.terasology.module.health.events.DoDamageEvent;
import org.terasology.moduletestingenvironment.MTEExtension;
import org.terasology.moduletestingenvironment.ModuleTestingHelper;
import org.terasology.moduletestingenvironment.extension.Dependencies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MTEExtension.class)
@Dependencies({"Health"})
@Tag("MteTest")
public class RegenTest {

    private static final float BUFFER = 0.2f; // 200 ms buffer time

    @In
    protected EntityManager entityManager;
    @In
    protected Time time;
    @In
    protected ModuleTestingHelper helper;

    EntityRef createNewPlayer(int currentHealth, int regenRate) {
        HealthComponent healthComponent = new HealthComponent();
        healthComponent.currentHealth = currentHealth;
        healthComponent.maxHealth = 100;

        BaseRegenComponent baseRegenComponent = new BaseRegenComponent();
        baseRegenComponent.waitBeforeRegen = 1;
        baseRegenComponent.regenRate = regenRate;

        final EntityRef player = entityManager.create();
        player.addComponent(new PlayerCharacterComponent());
        player.addComponent(healthComponent);
        player.addComponent(baseRegenComponent);

        player.send(new RegisterRegenEvent(BaseRegenAuthoritySystem.BASE_REGEN.toString(), -20, -1));

        return player;
    }

    EntityRef createNewPlayer(int currentHealth) {
        return createNewPlayer(currentHealth, 1);
    }

    @Test
    public void regenCancelTest() {
        EntityRef player = createNewPlayer(100);

        player.send(new DoDamageEvent(20));

        // Deactivate base regen
        player.send(new DeregisterRegenEvent(BaseRegenAuthoritySystem.BASE_REGEN.toString()));
        // there may have been some regeneration between the damage event and the deactivation
        // Thus, we compare against the current health which should be less than the max health.
        int currentHealth = player.getComponent(HealthComponent.class).currentHealth;
        assertTrue(currentHealth < 100, "regeneration should have been canceled before reaching max health");

        // wait for 2 seconds to give enough time that the regeneration could have kicked in
        helper.runWhile(2000, () -> true);

        assertEquals(currentHealth, player.getComponent(HealthComponent.class).currentHealth);
    }

    @Test
    public void zeroRegenTest() {
        EntityRef player = createNewPlayer(100, 0);

        player.send(new DoDamageEvent(5));
        assertEquals(95, player.getComponent(HealthComponent.class).currentHealth);

        float tick = time.getGameTime() + 2 + 0.500f;
        helper.runWhile(() -> time.getGameTime() <= tick);

        assertEquals(95, player.getComponent(HealthComponent.class).currentHealth,
                "entity should not have been recovered.");
    }

    @Test
    public void regenTest() {
        EntityRef player = createNewPlayer(100);

        player.send(new DoDamageEvent(5));
        assertEquals(95, player.getComponent(HealthComponent.class).currentHealth);

        // wait 1 second before regen starts (+ buffer of 200ms)
        helper.runWhile(1200, () -> true);
        assertTrue(player.hasComponent(RegenComponent.class));

        // wait for the regeneration to be finished: 5 dmg /  1hp/s = 5 seconds (+ 200ms buffer)
        float regenEnded = time.getGameTime() + 5f + BUFFER;
        helper.runWhile(5200, () -> true);

        assertEquals(player.getComponent(HealthComponent.class).currentHealth, 100);
    }
}
