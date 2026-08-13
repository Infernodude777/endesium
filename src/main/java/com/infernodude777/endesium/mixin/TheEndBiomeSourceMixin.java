package com.infernodude777.endesium.mixin;

import com.infernodude777.endesium.world.EndesiumBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TheEndBiomeSource.class)
abstract class TheEndBiomeSourceMixin {
	@Unique
	private Holder<Biome> endesium$endWastes;

	@Inject(method = "create", at = @At("RETURN"))
	private static void endesium$captureEndWastes(HolderGetter<Biome> biomeGetter,
			CallbackInfoReturnable<TheEndBiomeSource> cir) {
		if (System.getProperty("fabric-api.datagen") != null) {
			return;
		}
		TheEndBiomeSourceMixin source = (TheEndBiomeSourceMixin) (Object) cir.getReturnValue();
		biomeGetter.get(EndesiumBiomes.END_WASTES).ifPresent(holder -> source.endesium$endWastes = holder);
	}

	@Inject(method = "getNoiseBiome", at = @At("RETURN"))
	private void endesium$addEndWastes(int x, int y, int z, Climate.Sampler sampler,
			CallbackInfoReturnable<Holder<Biome>> cir) {
		Holder<Biome> vanilla = cir.getReturnValue();
		if (endesium$endWastes == null
				|| (!vanilla.is(Biomes.END_HIGHLANDS) && !vanilla.is(Biomes.END_MIDLANDS))) {
			return;
		}

		long hash = (long) x * 341873128712L + (long) z * 132897987541L;
		if (((hash ^ (hash >>> 32)) & 31L) != 0L) {
			return;
		}

		cir.setReturnValue(endesium$endWastes);
	}
}
