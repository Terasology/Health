/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private static final long DAMAGE_OVERLAY_DELAY_MS = 150L;
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
            double direction = determineDamageDirection(instigator, locationComponent);
            if (direction <= 45.0 && direction > -45.0) {
                damageDirection.setTop(true);
                delayManager.addDelayedAction(entity, REMOVE_TOP_ACTION, DAMAGE_OVERLAY_DELAY_MS);
            } else if (direction <= -45.0 && direction > -135.0) {
                damageDirection.setRight(true);
                delayManager.addDelayedAction(entity, REMOVE_RIGHT_ACTION, DAMAGE_OVERLAY_DELAY_MS);
            } else if (direction <= 135.0 && direction > 45.0) {
                damageDirection.setLeft(true);
                delayManager.addDelayedAction(entity, REMOVE_LEFT_ACTION, DAMAGE_OVERLAY_DELAY_MS);
            } else {
                damageDirection.setBottom(true);
                delayManager.addDelayedAction(entity, REMOVE_BOTTOM_ACTION, DAMAGE_OVERLAY_DELAY_MS);
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

    private double determineDamageDirection(EntityRef instigator, LocationComponent locationComponent) {
        LocationComponent instigatorLocation = instigator.getComponent(LocationComponent.class);
        Vector3f loc = new Vector3f();
        loc = locationComponent.getWorldPosition(loc);
		Vector3f locDiff = instigatorLocation.getWorldPosition(new Vector3f()).sub(loc).normalize();

        // facing x and z are "how much" of that direction we are facing
        // e.g. (0.0, 1.0) means that going forward increases world z position without increasing x position
		Vector3f worldFacing = locationComponent.getWorldDirection(new Vector3f()).normalize();

        return Math.toDegrees(worldFacing.angleSigned(locDiff, Direction.UP.asVector3f()));
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
