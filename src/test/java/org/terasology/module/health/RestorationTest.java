// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.registry.In;
import org.terasology.module.health.components.HealthComponent;
import org.terasology.module.health.events.BeforeRestoreEvent;
import org.terasology.module.health.events.DoRestoreEvent;
import org.terasology.moduletestingenvironment.MTEExtension;
import org.terasology.moduletestingenvironment.ModuleTestingHelper;
import org.terasology.moduletestingenvironment.TestEventReceiver;
import org.terasology.moduletestingenvironment.extension.Dependencies;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MTEExtension.class)
@Dependencies("Health")
@Tag("MteTest")
public class RestorationTest {

    @In
    protected ModuleTestingHelper helper;
    @In
    private EntityManager entityManager;

    private EntityRef newPlayer(int currentHealth) {
        HealthComponent healthComponent = new HealthComponent();
        healthComponent.currentHealth = currentHealth;
        healthComponent.maxHealth = 100;

        final EntityRef player = entityManager.create();
        player.addComponent(new PlayerCharacterComponent());
        player.addComponent(healthComponent);
        return player;
    }

    @Test
    public void restoreTest() {
        final EntityRef player = newPlayer(0);

        player.send(new DoRestoreEvent(10));
        assertEquals(10, player.getComponent(HealthComponent.class).currentHealth);

        player.send(new DoRestoreEvent(999));
        assertEquals(100, player.getComponent(HealthComponent.class).currentHealth);
    }

    @Test
    public void restoreEventSentTest() {
        try (TestEventReceiver<BeforeRestoreEvent> receiver = new TestEventReceiver<>(helper.getHostContext(),
                BeforeRestoreEvent.class)) {
            List<BeforeRestoreEvent> list = receiver.getEvents();
            assertTrue(list.isEmpty());

            final EntityRef player = newPlayer(0);

            player.send(new DoRestoreEvent(10));
            assertEquals(1, list.size());
        }
    }

    @Test
    public void restoreEventCancelTest() {
        final EntityRef player = newPlayer(0);

        try (TestEventReceiver<BeforeRestoreEvent> ignored = new TestEventReceiver<>(helper.getHostContext(),
                BeforeRestoreEvent.class,
                (event, entity) -> event.consume())) {
            player.send(new DoRestoreEvent(10));
        }
        assertEquals(0, player.getComponent(HealthComponent.class).currentHealth);
    }

    @Test
    public void restorationModifyEventTest() {
        final EntityRef player = newPlayer(0);

        try (TestEventReceiver<BeforeRestoreEvent> ignored = new TestEventReceiver<>(helper.getHostContext(),
                BeforeRestoreEvent.class,
                (event, entity) -> {
                    event.add(5);
                    event.multiply(2);
                })) {
            // Expected value is ( initial:10 + 5 ) * 2 == 30
            player.send(new DoRestoreEvent(10));
        }
        assertEquals(30, player.getComponent(HealthComponent.class).currentHealth);
    }

    @Test
    public void restorationNegativeTest() {
        assertThrows(IllegalArgumentException.class, () -> new DoRestoreEvent(-10));
    }

    @Test
    public void restorationNegativeModifyEventTest() {
        final EntityRef player = newPlayer(50);

        try (TestEventReceiver<BeforeRestoreEvent> ignored = new TestEventReceiver<>(helper.getHostContext(),
                BeforeRestoreEvent.class,
                (event, entity) -> {
                    event.multiply(-2);
                })) {
            player.send(new DoRestoreEvent(10));
        }
        // Expected restoration value is ( initial:10 ) * (-2) == (-20)
        // The authority system handles both cases of positive and negative restoration, and sends out an OnRestoredEvent
        // or OnDamagedEvent, respectively. Therefore, the expected value is (initial: 50) - 20 = 30
        assertEquals(30, player.getComponent(HealthComponent.class).currentHealth);
    }
}
