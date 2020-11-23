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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.health.event.OnDamagedEvent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.hud.DamagedOverlay;

@RegisterSystem(RegisterMode.CLIENT)
public class HealthClientSystem extends BaseComponentSystem {

    private static final String REMOVE_DAMAGED_OVERLAY_ACTION = "Health:RemoveDamagedOverlay";
    private static final String DAMAGED_OVERLAY = "damagedOverlay";
    private static final long DAMAGED_OVERLAY_DELAY_MS = 150L;

    @In
    private NUIManager nuiManager;

    @In
    private DelayManager delayManager;

    private boolean overlayDisplaying = false;

    @Override
    public void initialise() {
        nuiManager.getHUD().addHUDElement("healthHud");
    }

    @ReceiveEvent(components = PlayerCharacterComponent.class)
    public void onDamaged(OnDamagedEvent event, EntityRef entity) {
        if (!overlayDisplaying && event.getDamageAmount() > 0) {
            overlayDisplaying = true;
            nuiManager.addOverlay(DAMAGED_OVERLAY, DamagedOverlay.class);
            delayManager.addDelayedAction(entity, REMOVE_DAMAGED_OVERLAY_ACTION, DAMAGED_OVERLAY_DELAY_MS);
        }
    }

    @ReceiveEvent
    public void onDelayedAction(DelayedActionTriggeredEvent event, EntityRef entityRef) {
        if (event.getActionId().equals(REMOVE_DAMAGED_OVERLAY_ACTION)) {
            nuiManager.removeOverlay(DAMAGED_OVERLAY);
            overlayDisplaying = false;
        }
    }

}
