// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.nui.layers.hud;

import org.terasology.nui.widgets.UIImage;

public class DamageDirection extends CoreHudWidget {

    private UIImage bottom;
    private UIImage top;
    private UIImage left;
    private UIImage right;

    @Override
    public void initialise() {
        bottom = find("damageBottom", UIImage.class);
        top = find("damageTop", UIImage.class);
        left = find("damageLeft", UIImage.class);
        right = find("damageRight", UIImage.class);
    }

    public void clearAll() {
        setBottom(false);
        setTop(false);
        setLeft(false);
        setRight(false);
    }

    public void setBottom(boolean visible) {
        bottom.setVisible(visible);
    }

    public void setTop(boolean visible) {
        top.setVisible(visible);
    }

    public void setLeft(boolean visible) {
        left.setVisible(visible);
    }

    public void setRight(boolean visible) {
        right.setVisible(visible);
    }
}
