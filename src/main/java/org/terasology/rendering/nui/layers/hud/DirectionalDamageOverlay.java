// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.nui.layers.hud;

import org.terasology.engine.Time;
import org.terasology.math.Direction;
import org.terasology.nui.Canvas;
import org.terasology.nui.widgets.UIImage;
import org.terasology.registry.In;

import java.util.HashMap;
import java.util.Map;

public class DirectionalDamageOverlay extends CoreHudWidget {

    @In
    private Time time;

    private UIImage bottom;
    private UIImage top;
    private UIImage left;
    private UIImage right;

    private Map<Direction, TimingInformation> activeIndicators = new HashMap<>();

    @Override
    public void initialise() {
        bottom = find("damageBottom", UIImage.class);
        top = find("damageTop", UIImage.class);
        left = find("damageLeft", UIImage.class);
        right = find("damageRight", UIImage.class);
    }

    private UIImage imageForDirection(Direction direction) {
        switch (direction) {
            case UP:
            case FORWARD:
                return top;
            case RIGHT:
                return right;
            case LEFT:
                return left;
            case DOWN:
            case BACKWARD:
                return bottom;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        float currentTime = time.getGameTime();
        for (Direction direction : Direction.values()) {
            // update state: update active indicators
            if (activeIndicators.containsKey(direction) && activeIndicators.get(direction).end <= currentTime) {
                activeIndicators.remove(direction);
            }
            // render state: render active indicators
            UIImage indicator = imageForDirection(direction);
            indicator.setVisible(activeIndicators.containsKey(direction));
        }
        super.onDraw(canvas);
    }

    public void show(Direction damageDirection, float durationInSeconds) {
        float currentTime = time.getGameTime();
        activeIndicators.put(damageDirection, new TimingInformation(currentTime, currentTime + durationInSeconds));
    }
}
