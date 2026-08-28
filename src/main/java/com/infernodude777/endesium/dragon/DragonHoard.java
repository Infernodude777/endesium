package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModItems;
import com.infernodude777.endesium.state.PostDragonState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The hoard becomes a location, not a pile. When the dragon falls the assault
 * layer calls {@link #spawnHoard} at the death site: a chest is placed on the
 * island holding the curated dragon materials, and a resonance beacon - a
 * pillar of light and a pulsing base - marks the spot for minutes afterwards
 * so the kill leaves a landmark the players can walk to.
 *
 * <p>First kills pay the premium bundle; repeat kills pay reduced rolls,
 * mirroring {@link DragonLoot}.</p>
 */
public final class DragonHoard {
    /** How long the beacon stays lit (ticks). */
    private static final int BEACON_TICKS = 12000;

    private DragonHoard() { }

    public static void spawnHoard(ServerLevel level, BlockPos at) {
        BlockPos anchor = at == null ? new BlockPos(0, 64, 0) : at;
        BlockPos spot = findGround(level, anchor);
        if (spot == null) return;

        // The hoard chest: the landmark itself.
        level.setBlock(spot, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(spot) instanceof ChestBlockEntity chest) {
            boolean firstKill = !PostDragonState.get(level).isDragonDefeated();
            fill(chest, firstKill, level.random);
        }

        // The beacon: a pulsing base and a pillar of light.
        double x = spot.getX() + 0.5D;
        double y = spot.getY() + 0.5D;
        double z = spot.getZ() + 0.5D;
        AreaEffectCloud base = new AreaEffectCloud(level, x, y, z);
        base.setRadius(2.8F);
        base.setDuration(BEACON_TICKS);
        base.setWaitTime(0);
        base.setParticle(ModParticles.RESONANCE_ACTIVE);
        level.addFreshEntity(base);

        for (int k = 1; k <= 3; k++) {
            AreaEffectCloud beam = new AreaEffectCloud(level, x, y + k * 4.0D, z);
            beam.setRadius(1.4F - k * 0.25F);
            beam.setDuration(BEACON_TICKS);
            beam.setWaitTime(0);
            beam.setParticle(ParticleTypes.END_ROD);
            level.addFreshEntity(beam);
        }

        level.playSound(null, spot, SoundEvents.BEACON_ACTIVATE, SoundSource.AMBIENT, 1.5F, 1.0F);
        level.playSound(null, spot, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.AMBIENT, 2.0F, 1.0F);
    }

    /** Finds the air block just above the island surface nearest the anchor. */
    private static BlockPos findGround(ServerLevel level, BlockPos at) {
        int x = at.getX();
        int z = at.getZ();
        if (!level.isLoaded(new BlockPos(x, 0, z))) return null;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) + 1;
        BlockPos pos = new BlockPos(x, y, z);
        return level.getBlockState(pos).isAir() ? pos : null;
    }

    private static void fill(ChestBlockEntity chest, boolean firstKill, RandomSource random) {
        NonNullList<ItemStack> items =
                NonNullList.withSize(chest.getContainerSize(), ItemStack.EMPTY);
        int slot = 0;
        if (firstKill) {
            items.set(slot++, new ItemStack(ModItems.DRAGON_HEART));
            items.set(slot++, new ItemStack(ModItems.DRAGON_FANG, 3));
            items.set(slot++, new ItemStack(ModItems.DRAGONBONE, 6));
            items.set(slot++, new ItemStack(ModItems.RESONANT_DRAGON_SCALE, 2));
            items.set(slot++, new ItemStack(ModItems.ENDER_ESSENCE, 2));
        } else {
            items.set(slot++, new ItemStack(ModItems.DRAGON_FANG, 1 + random.nextInt(2)));
            items.set(slot++, new ItemStack(ModItems.DRAGONBONE, 3 + random.nextInt(3)));
            if (random.nextFloat() < 0.6F) {
                items.set(slot++, new ItemStack(ModItems.RESONANT_DRAGON_SCALE, 1));
            }
            if (random.nextFloat() < 0.5F) {
                items.set(slot++, new ItemStack(ModItems.ENDER_ESSENCE, 1));
            }
        }
        for (int i = 0; i < items.size(); i++) {
            chest.setItem(i, items.get(i));
        }
    }
}
