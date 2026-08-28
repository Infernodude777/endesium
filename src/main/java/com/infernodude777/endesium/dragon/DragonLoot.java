package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Code-driven Dragon material drops. The first kill guarantees the Resonant
 * Elytra bundle (4 Resonant Dragon Scales + supporting materials); later
 * kills grant reduced quantities and rolls. Looting never applies: the drops
 * are granted on the death hook, not through the vanilla loot table.
 */
public final class DragonLoot {
    private DragonLoot() { }

    /**
     * Grants curated drops based on whether this is the first dragon kill.
     * First kills are deterministic; subsequent kills are probabilistic.
     */
    public static void grantDrops(EnderDragon dragon, ServerLevel level, boolean firstKill) {
        RandomSource random = level.random;

        if (firstKill) {
            drop(dragon, ModItems.RESONANT_DRAGON_SCALE, 4);
            drop(dragon, ModItems.DRAGONBONE, 2 + random.nextInt(2));
            drop(dragon, ModItems.ENDER_ESSENCE, 2);
            drop(dragon, ModItems.ECHO_SHARD, 1);
            drop(dragon, ModItems.PROGRESSION_GUIDE, 1);
            drop(dragon, ModItems.ARCHIVE_FRAGMENT, 1);
        } else {
            if (random.nextFloat() < 0.67F) {
                drop(dragon, ModItems.RESONANT_DRAGON_SCALE, 1 + random.nextInt(2));
            }
            drop(dragon, ModItems.DRAGONBONE, 1 + random.nextInt(2));
            if (random.nextFloat() < 0.6F) {
                drop(dragon, ModItems.ENDER_ESSENCE, 1);
            }
        }

        roll(dragon, random, ModItems.DRAGON_FANG, firstKill ? 0.25F : 0.12F);
        roll(dragon, random, ModItems.VOID_PEARL, firstKill ? 0.20F : 0.10F);
        roll(dragon, random, ModItems.ABYSSAL_THREAD, firstKill ? 0.30F : 0.15F);
        roll(dragon, random, ModItems.RESONANCE_CORE, firstKill ? 0.20F : 0.10F);
        if (random.nextFloat() < (firstKill ? 0.10F : 0.04F)) {
            drop(dragon, ModItems.DRAGON_HEART, 1);
        }
    }

    private static void roll(EnderDragon dragon, RandomSource random, Item item, float chance) {
        if (random.nextFloat() < chance) {
            drop(dragon, item, 1);
        }
    }

    private static void drop(EnderDragon dragon, Item item, int count) {
        dragon.spawnAtLocation(new ItemStack(item, count));
    }
}
