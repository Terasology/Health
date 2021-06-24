// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.health.systems;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.particles.components.ParticleDataSpriteComponent;
import org.terasology.engine.particles.components.generators.TextureOffsetGeneratorComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.rendering.world.selection.BlockSelectionRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.block.BlockAppearance;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockPart;
import org.terasology.engine.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.regions.ActAsBlockComponent;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.engine.world.block.tiles.WorldAtlas;
import org.terasology.math.TeraMath;
import org.terasology.module.health.components.HealthComponent;
import org.terasology.module.health.events.OnDamagedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This system visualizes damaged blocks by rendering a damage overlay.
 * <p>
 * The system derives a damage effect level between 0 (full health / no damage) and 10 (0 health / full damage). Starting from level 1 to
 * level 10 the damage overlay effect is taken from the {@code CoreAssets:blockDamageEffects} texture atlas.
 * <p>
 * To change the default damage effects the texture can be overridden.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BlockDamageRenderer extends BaseComponentSystem implements RenderSystem {

    private BlockSelectionRenderer blockSelectionRenderer;
    private Random random = new FastRandom();

    @In
    private EntityManager entityManager;
    @In
    private WorldAtlas worldAtlas;

    @Override
    public void renderOverlay() {
        if (blockSelectionRenderer == null) {
            Texture texture = Assets.getTextureRegion("CoreAssets:blockDamageEffects#1").get().getTexture();
            blockSelectionRenderer = new BlockSelectionRenderer(texture);
        }
        // group the entities into what texture they will use so that there is less recreating meshes (changing a
        // texture region on the BlockSelectionRenderer will recreate the mesh to use the different UV coordinates).
        Multimap<Integer, Vector3i> groupedEntitiesByEffect = ArrayListMultimap.create();

        for (EntityRef entity : entityManager.getEntitiesWith(HealthComponent.class, BlockComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health.currentHealth == health.maxHealth) {
                continue;
            }
            BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
            groupedEntitiesByEffect.put(getDamageEffectsNumber(health), blockComponent.getPosition(new Vector3i()));
        }
        for (EntityRef entity : entityManager.getEntitiesWith(HealthComponent.class, BlockRegionComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health.currentHealth == health.maxHealth) {
                continue;
            }
            BlockRegionComponent blockRegion = entity.getComponent(BlockRegionComponent.class);
            for (Vector3ic blockPos : blockRegion.region) {
                groupedEntitiesByEffect.put(getDamageEffectsNumber(health), new Vector3i(blockPos));
            }
        }

        // Bind the texture already as we know that the texture will be the same for each block effect, just different UV coordinates.
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

    /**
     * Compute the damage effect number as linear mapping from damage percentage to the range [0..10].
     * <p>
     * A value of 0 denotes no damage, while 10 denotes a current health of 0.
     *
     * @return the effect number in [0..10] linear to damage percentage
     */
    int getDamageEffectsNumber(HealthComponent health) {
        Preconditions.checkArgument(health.currentHealth >= 0);
        Preconditions.checkArgument(health.maxHealth > 0);

        float damagePercentage = 1f - Math.clamp(0f, 1f, (float) health.currentHealth / health.maxHealth);
        return Math.round(damagePercentage * 10);
    }

    @ReceiveEvent
    public void onDamaged(OnDamagedEvent event, EntityRef blockEntity, BlockComponent blockComponent, LocationComponent locComp) {
        onDamagedCommon(event, blockComponent.getBlock().getBlockFamily(), locComp.getWorldPosition(new Vector3f()));
    }

    @ReceiveEvent
    public void onDamaged(OnDamagedEvent event, EntityRef entity, ActAsBlockComponent blockComponent, LocationComponent locComp) {
        if (blockComponent.block != null) {
            onDamagedCommon(event, blockComponent.block, locComp.getWorldPosition(new Vector3f()));
        }
    }

    private void onDamagedCommon(OnDamagedEvent event, BlockFamily blockFamily, Vector3fc location) {
        BlockDamageModifierComponent blockDamageSettings = event.getType().getComponent(BlockDamageModifierComponent.class);
        boolean skipDamageEffects = blockDamageSettings != null && blockDamageSettings.skipPerBlockEffects;

        if (!skipDamageEffects) {
            //TODO: move this to BlockDamageRenderer
            createBlockParticleEffect(blockFamily, location);
        }
    }

    /**
     * Creates a new entity for the block damage particle effect.
     *
     * If the terrain texture of the damaged block is available, the particles will have the block texture. Otherwise,
     * the default sprite (smoke) is used.
     *
     * @param family the {@link BlockFamily} of the damaged block
     * @param location the location of the damaged block
     */
    private void createBlockParticleEffect(BlockFamily family, Vector3fc location) {
        EntityBuilder builder = entityManager.newBuilder("CoreAssets:defaultBlockParticles");
        builder.getComponent(LocationComponent.class).setWorldPosition(location);

        Optional<Texture> terrainTexture = Assets.getTexture("engine:terrain");
        if (terrainTexture.isPresent() && terrainTexture.get().isLoaded()) {
            final BlockAppearance blockAppearance = family.getArchetypeBlock().getPrimaryAppearance();

            final float relativeTileSize = worldAtlas.getRelativeTileSize();
            final float particleScale = 0.25f;

            final float spriteSize = relativeTileSize * particleScale;

            ParticleDataSpriteComponent spriteComponent = builder.getComponent(ParticleDataSpriteComponent.class);
            spriteComponent.texture = terrainTexture.get();
            spriteComponent.textureSize.set(spriteSize, spriteSize);

            final List<Vector2f> offsets = computeOffsets(blockAppearance, particleScale);

            TextureOffsetGeneratorComponent textureOffsetGeneratorComponent = builder.getComponent(TextureOffsetGeneratorComponent.class);
            textureOffsetGeneratorComponent.validOffsets.addAll(offsets);
        }

        builder.build();
    }

    /**
     * Computes n random offset values for each block part texture.
     *
     * @param blockAppearance the block appearance information to generate offsets from
     * @param scale the scale of the texture area (should be in 0 < scale <= 1.0)
     *
     * @return a list of random offsets sampled from all block parts
     */
    private List<Vector2f> computeOffsets(BlockAppearance blockAppearance, float scale) {
        final float relativeTileSize = worldAtlas.getRelativeTileSize();
        final int absoluteTileSize = worldAtlas.getTileSize();
        final float pixelSize = relativeTileSize / absoluteTileSize;
        final int spriteWidth = TeraMath.ceilToInt(scale * absoluteTileSize);

        final Stream<Vector2fc> baseOffsets =
                Arrays.stream(BlockPart.sideValues()).map(blockAppearance::getTextureAtlasPos);

        return baseOffsets.flatMap(baseOffset ->
                IntStream.range(0, 8).boxed()
                        .map(i ->
                                new Vector2f(baseOffset)
                                        .add(random.nextInt(absoluteTileSize - spriteWidth) * pixelSize,
                                                random.nextInt(absoluteTileSize - spriteWidth) * pixelSize)
                        )
        ).collect(Collectors.toList());
    }
}
