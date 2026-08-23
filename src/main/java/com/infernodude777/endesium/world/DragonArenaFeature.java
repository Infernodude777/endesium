package com.infernodude777.endesium.world;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Central-island-only feature. The arena is much larger than one chunk, so it
 * runs in every chunk of the 25x25 region around the world origin and each
 * invocation draws only the pieces that fall inside its own chunk column. This
 * keeps every write inside the generating chunk (no cross-chunk setBlock).
 */
public final class DragonArenaFeature extends Feature<NoneFeatureConfiguration> {
	public DragonArenaFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		ChunkPos chunk = new ChunkPos(context.origin());
		if (Math.abs(chunk.x) > 12 || Math.abs(chunk.z) > 12) {
			return false;
		}
		DragonArenaBuilder.build(context.level(), chunk, context.random());
		return true;
	}
}
