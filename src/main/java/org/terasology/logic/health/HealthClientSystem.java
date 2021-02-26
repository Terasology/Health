// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.health;

import org.joml.Vector3f;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.health.event.OnDamagedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.math.Direction;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.hud.DamageDirection;
import org.terasology.rendering.nui.layers.hud.MajorDamageOverlay;

@RegisterSystem(RegisterMode.CLIENT)
public class HealthClientSystem extends BaseComponentSystem {

    private static final long DAMAGE_OVERLAY_DELAY_MS = 500L;
    private static final String REMOVE_TOP_ACTION = "Health:RemoveTopHud";
    private static final String REMOVE_BOTTOM_ACTION = "Health:RemoveBottomHud";
    private static final String REMOVE_LEFT_ACTION = "Health:RemoveLeftHud";
    private static final String REMOVE_RIGHT_ACTION = "Health:RemoveRightHud";
    private static final String REMOVE_DAMAGE_OVERLAY_ACTION = "Health:RemoveMajorDamageOverlay";
    private static final String DAMAGE_OVERLAY = "majorDamageOverlay";
    private static final float DAMAGE_OVERLAY_REQUIRED_PERCENT = 0.25f;

    @In
    private NUIManager nuiManager;

    @In
    private DelayManager delayManager;

    private boolean overlayDisplaying;
    private DamageDirection damageDirection;

    @Override
    public void initialise() {
        nuiManager.getHUD().addHUDElement("healthHud");
        damageDirection = (DamageDirection) nuiManager.getHUD().addHUDElement("damageDirection");
        damageDirection.clearAll();
    }

    @ReceiveEvent(components = PlayerCharacterComponent.class)
    public void onDamaged(OnDamagedEvent event, EntityRef entity) {
        // Show the relevant direction element
        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        EntityRef instigator = event.getInstigator();
        if (instigator != null && instigator.hasComponent(LocationComponent.class)) {
            switch (determineDamageDirection(instigator, locationComponent)) {
                case RIGHT:
                    damageDirection.setRight(true);
                    delayManager.addDelayedAction(entity, REMOVE_RIGHT_ACTION, DAMAGE_OVERLAY_DELAY_MS);
                    break;
                case LEFT:
                    damageDirection.setLeft(true);
                    delayManager.addDelayedAction(entity, REMOVE_LEFT_ACTION, DAMAGE_OVERLAY_DELAY_MS);
                    break;
                case BACKWARD:
                    damageDirection.setBottom(true);
                    delayManager.addDelayedAction(entity, REMOVE_BOTTOM_ACTION, DAMAGE_OVERLAY_DELAY_MS);
                    break;
                case FORWARD:
                    damageDirection.setTop(true);
                    delayManager.addDelayedAction(entity, REMOVE_TOP_ACTION, DAMAGE_OVERLAY_DELAY_MS);
                    break;
                default:
                    // do nothing
            }
        }

        // Is it major damage?
        HealthComponent healthComponent = entity.getComponent(HealthComponent.class);
        int amount = event.getDamageAmount();
        int current = healthComponent.currentHealth;
        if (current > 0 && amount > 0) {
            float percent = (float) amount / current;
            if (percent >= DAMAGE_OVERLAY_REQUIRED_PERCENT && !overlayDisplaying) {
                overlayDisplaying = true;
                nuiManager.addOverlay(DAMAGE_OVERLAY, MajorDamageOverlay.class);
                delayManager.addDelayedAction(entity, REMOVE_DAMAGE_OVERLAY_ACTION, DAMAGE_OVERLAY_DELAY_MS);
            }
        }
    }

    private Direction determineDamageDirection(EntityRef instigator, LocationComponent locationComponent) {
        LocationComponent instigatorLocation = instigator.getComponent(LocationComponent.class);
        Vector3f loc = locationComponent.getWorldPosition(new Vector3f());
        Vector3f locDiff = instigatorLocation.getWorldPosition(new Vector3f()).sub(loc).normalize();

        // facing x and z are "how much" of that direction we are facing
        // e.g. (0.0, 1.0) means that going forward increases world z position without increasing x position
        Vector3f worldFacing = locationComponent.getWorldDirection(new Vector3f()).normalize();

        double direction = org.joml.Math.toDegrees(worldFacing.angleSigned(locDiff, Direction.UP.asVector3f()));

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

    @ReceiveEvent
    public void onDelayedAction(DelayedActionTriggeredEvent event, EntityRef entityRef) {
        switch (event.getActionId()) {
            case REMOVE_DAMAGE_OVERLAY_ACTION:
                nuiManager.removeOverlay(DAMAGE_OVERLAY);
                overlayDisplaying = false;
                break;
            case REMOVE_TOP_ACTION:
                damageDirection.setTop(false);
                break;
            case REMOVE_BOTTOM_ACTION:
                damageDirection.setBottom(false);
                break;
            case REMOVE_LEFT_ACTION:
                damageDirection.setLeft(false);
                break;
            case REMOVE_RIGHT_ACTION:
                damageDirection.setRight(false);
                break;
        }
    }

}
