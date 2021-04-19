// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.systems;

import com.google.common.annotations.VisibleForTesting;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.math.Direction;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.module.health.components.HealthComponent;
import org.terasology.module.health.events.MaxHealthChangedEvent;
import org.terasology.module.health.events.OnDamagedEvent;
import org.terasology.module.health.ui.DirectionalDamageOverlay;
import org.terasology.nui.widgets.UIIconBar;

@RegisterSystem(RegisterMode.CLIENT)
public class HealthClientSystem extends BaseComponentSystem {

    private static final float DAMAGE_OVERLAY_DELAY_SECONDS = 0.5f;

    @In
    private NUIManager nuiManager;

    private DirectionalDamageOverlay directionalDamageOverlay;

    @Override
    public void initialise() {
        nuiManager.getHUD().addHUDElement("healthHud");
        directionalDamageOverlay = (DirectionalDamageOverlay) nuiManager.getHUD().addHUDElement(
                "directionalDamageOverlay");
    }

    @ReceiveEvent(components = PlayerCharacterComponent.class)
    public void onDamaged(OnDamagedEvent event, EntityRef entity,
                          LocationComponent locationComponent,
                          HealthComponent healthComponent) {

        EntityRef instigator = event.getInstigator();
        if (instigator != null && instigator.hasComponent(LocationComponent.class)) {
            // Show the relevant direction element
            Direction direction = determineDamageDirection(instigator, locationComponent);
            directionalDamageOverlay.show(direction, DAMAGE_OVERLAY_DELAY_SECONDS);
        } else {
            // Show non-directional damage indication by making all four indicators visible
            for (Direction direction : Direction.values()) {
                directionalDamageOverlay.show(direction, DAMAGE_OVERLAY_DELAY_SECONDS);
            }
        }
    }

    /**
     * Reacts to the {@link MaxHealthChangedEvent} notification event. Is responsible for the change in maximum number
     * of icons in the Health Bar UI.
     */
    @ReceiveEvent
    public void onMaxHealthChanged(MaxHealthChangedEvent event, EntityRef player) {
        UIIconBar healthBar = nuiManager.getHUD().find("healthBar", UIIconBar.class);
        healthBar.setMaxIcons(event.getNewValue() / 10);
    }

    @VisibleForTesting
    Direction determineDamageDirection(EntityRef instigator, LocationComponent locationComponent) {
        LocationComponent instigatorLocation = instigator.getComponent(LocationComponent.class);
        Vector3f loc = locationComponent.getWorldPosition(new Vector3f());
        Vector3f locDiff = instigatorLocation.getWorldPosition(new Vector3f()).sub(loc).normalize();

        // facing x and z are "how much" of that direction we are facing
        // e.g. (0.0, 1.0) means that going forward increases world z position without increasing x position
        Vector3f worldFacing = locationComponent.getWorldDirection(new Vector3f()).normalize();

        double direction = Math.toDegrees(worldFacing.angleSigned(locDiff, Direction.UP.asVector3f()));

        if (direction <= 45.0 && direction > -45.0) {
            return Direction.FORWARD;
        } else if (direction <= -45.0 && direction > -135.0) {
            return Direction.RIGHT;
        } else if (direction <= 135.0 && direction > 45.0) {
            return Direction.LEFT;
        } else {
            return Direction.BACKWARD;
        }
    }
}
