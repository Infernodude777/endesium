package com.infernodude777.endesium.mixin;

import com.infernodude777.endesium.world.EndesiumWorldgenSeeds;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the world seed when RandomState is created. TheEndBiomeSource is
 * constructed without the seed, but RandomState is created before any biome
 * sampling happens, so this is the reliable place to observe it.
 */
@Mixin(RandomState.class)
abstract class RandomStateMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void endesium$captureSeed(NoiseGeneratorSettings settings,
			HolderGetter<NormalNoise.NoiseParameters> noises, long seed, CallbackInfo ci) {
		EndesiumWorldgenSeeds.capture(seed);
	}
}
