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

@RegisterSystem(RegisterMode.CLIENT)
public class HealthClientSystem extends BaseComponentSystem {

    private static final String REMOVE_DAMAGED_OVERLAY_ACTION = "Health:RemoveDamagedOverlay";
    private static final String DAMAGED_OVERLAY = "damagedOverlay";
    private static final long DAMAGED_OVERLAY_DELAY_MS = 150L;
    private static final float DAMAGED_OVERLAY_REQUIRED_PERCENT = 0.25f;

    @In
    private NUIManager nuiManager;

    @In
    private DelayManager delayManager;

    private boolean overlayDisplaying = false;
    private DamageDirection damageDirection;

    @Override
    public void initialise() {
        nuiManager.getHUD().addHUDElement("healthHud");
        damageDirection = (DamageDirection) nuiManager.getHUD().addHUDElement("damageDirection");
        damageDirection.clearAll();
    }

    @ReceiveEvent(components = PlayerCharacterComponent.class)
    public void onDamaged(OnDamagedEvent event, EntityRef entity) {
        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        EntityRef instigator = event.getInstigator();
        if (instigator != null && instigator.hasComponent(LocationComponent.class)) {
            LocationComponent instigatorLocation = instigator.getComponent(LocationComponent.class);
            Vector3f loc = new Vector3f();
            loc = locationComponent.getWorldPosition(loc);
            Vector3f instLoc = new Vector3f();
            instLoc = instigatorLocation.getWorldPosition(instLoc);
            Vector3f locDiff = instLoc.sub(loc);
            locDiff.normalize();

            Vector3f worldFacing = new Vector3f();
            // facing x and z are "how much" of that direction are we facing
            // e.g. (0.0, 1.0) would mean that going forward would increase z position without increasing x position
            worldFacing = locationComponent.getWorldDirection(worldFacing);
            worldFacing.normalize();

            double angleDegrees = rad2Deg(worldFacing.angleSigned(locDiff, Direction.UP.asVector3f()));

            if (!overlayDisplaying) {
                if (angleDegrees <= 45.0 && angleDegrees > -45.0) {
                    damageDirection.setTop(true);
                } else if (angleDegrees <= -45.0 && angleDegrees > -135.0) {
                    damageDirection.setRight(true);
                } else if (angleDegrees <= 135.0 && angleDegrees > 45.0) {
                    damageDirection.setLeft(true);
                } else {
                    damageDirection.setBottom(true);
                }
                overlayDisplaying = true;
                delayManager.addDelayedAction(entity, REMOVE_DAMAGED_OVERLAY_ACTION, DAMAGED_OVERLAY_DELAY_MS);
            }
        }
    }

    private double rad2Deg(double rad) {
        return rad / Math.PI * 180.0;
    }

    @ReceiveEvent
    public void onDelayedAction(DelayedActionTriggeredEvent event, EntityRef entityRef) {
        if (event.getActionId().equals(REMOVE_DAMAGED_OVERLAY_ACTION)) {
            nuiManager.removeOverlay(DAMAGED_OVERLAY);
            overlayDisplaying = false;
            damageDirection.clearAll();
        }
    }

}
