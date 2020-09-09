// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.health.logic;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.rendering.world.selection.BlockSelectionRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.math.geom.Vector3i;

import java.util.Optional;

/**
 * This system renders damage damaged blocks using the BlockSelectionRenderer.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BlockDamageRenderer extends BaseComponentSystem implements RenderSystem {

    private BlockSelectionRenderer blockSelectionRenderer;

    @In
    private EntityManager entityManager;

    @Override
    public void renderOverlay() {
        if (blockSelectionRenderer == null) {
            Texture texture = Assets.getTextureRegion("CoreAssets:blockDamageEffects#1").get().getTexture();
            blockSelectionRenderer = new BlockSelectionRenderer(texture);
        }
        // group the entities into what texture they will use so that there is less recreating meshes (changing a
        // texture region on the BlockSelectionRenderer
        // will recreate the mesh to use the different UV coordinates).  Also this allows
        Multimap<Integer, Vector3i> groupedEntitiesByEffect = ArrayListMultimap.create();

        for (EntityRef entity : entityManager.getEntitiesWith(HealthComponent.class, BlockComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health.currentHealth == health.maxHealth) {
                continue;
            }
            BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
            groupedEntitiesByEffect.put(getEffectsNumber(health), blockComponent.position);
        }
        for (EntityRef entity : entityManager.getEntitiesWith(BlockRegionComponent.class, HealthComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health.currentHealth == health.maxHealth) {
                continue;
            }
            BlockRegionComponent blockRegion = entity.getComponent(BlockRegionComponent.class);
            for (Vector3i blockPos : blockRegion.region) {
                groupedEntitiesByEffect.put(getEffectsNumber(health), blockPos);
            }
        }

        // we know that the texture will be the same for each block effect,  just different UV coordinates.
        // Bind the texture already
        blockSelectionRenderer.beginRenderOverlay();

        for (Integer effectsNumber : groupedEntitiesByEffect.keySet()) {
            Optional<TextureRegionAsset> texture =
                    Assets.getTextureRegion("CoreAssets:blockDamageEffects#" + effectsNumber);
            if (texture.isPresent()) {
                blockSelectionRenderer.setEffectsTexture(texture.get());
                for (Vector3i position : groupedEntitiesByEffect.get(effectsNumber)) {
                    blockSelectionRenderer.renderMark(position);
                }
            }
        }

        blockSelectionRenderer.endRenderOverlay();
    }

    private Integer getEffectsNumber(HealthComponent health) {
        return Math.round((1f - (float) health.currentHealth / health.maxHealth) * 10.0f);
    }


    @Override
    public void renderShadows() {
    }

    @Override
    public void renderOpaque() {
    }

    @Override
    public void renderAlphaBlend() {
    }
}
