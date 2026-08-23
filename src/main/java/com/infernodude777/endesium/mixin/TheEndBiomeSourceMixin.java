package com.infernodude777.endesium.mixin;

import com.infernodude777.endesium.world.EndesiumBiomeHolders;
import com.infernodude777.endesium.world.EndesiumBiomes;
import com.infernodude777.endesium.world.EndesiumRegions;
import com.infernodude777.endesium.world.EndesiumWorldgenSeeds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Extends the vanilla End biome source with ten large-scale Endesium biomes.
 *
 * <p>Vanilla biome selection (central island, highlands/midlands/barrens/islands
 * rings) runs first using the same thresholds as Minecraft. Eligible highlands
 * and midlands columns are then deterministically assigned to one of the ten
 * Endesium regions via {@link EndesiumRegions}. The central island, the small
 * end islands, and the barrens remain vanilla so the Dragon fight and vanilla
 * End traversal are unchanged.</p>
 */
@Mixin(TheEndBiomeSource.class)
abstract class TheEndBiomeSourceMixin {
	@Shadow
	@Final
	private Holder<Biome> end;
	@Shadow
	@Final
	private Holder<Biome> highlands;
	@Shadow
	@Final
	private Holder<Biome> midlands;
	@Shadow
	@Final
	private Holder<Biome> islands;
	@Shadow
	@Final
	private Holder<Biome> barrens;

	@Unique
	private Holder<Biome> endesium$forRegion(int region) {
		return EndesiumBiomeHolders.forRegion(region);
	}

	@Inject(method = "create", at = @At("RETURN"))
	private static void endesium$captureBiomes(HolderGetter<Biome> biomeGetter,
			CallbackInfoReturnable<TheEndBiomeSource> cir) {
		List<ResourceKeyHolder> keys = new ArrayList<>();
		keys.add(new ResourceKeyHolder(EndesiumBiomes.END_WASTES));
		keys.add(new ResourceKeyHolder(EndesiumBiomes.SHATTERED_HIGHLANDS));
		keys.add(new ResourceKeyHolder(EndesiumBiomes.VOID_MARSHES));
		keys.add(new ResourceKeyHolder(EndesiumBiomes.CHORUS_WILDS));
		keys.add(new ResourceKeyHolder(EndesiumBiomes.LUMINOUS_GROVES));
		keys.add(new ResourceKeyHolder(EndesiumBiomes.ASHEN_EXPANSE));
		keys.add(new ResourceKeyHolder(EndesiumBiomes.CRYSTAL_BARRENS));
		keys.add(new ResourceKeyHolder(EndesiumBiomes.VOID_SKIRTS));
		keys.add(new ResourceKeyHolder(EndesiumBiomes.VOID_CROWN));
		keys.add(new ResourceKeyHolder(EndesiumBiomes.UMBRAL_REACH));

		Holder<Biome>[] resolved = new Holder[EndesiumRegions.COUNT];
		boolean complete = true;
		for (int i = 0; i < EndesiumRegions.COUNT; i++) {
			var found = biomeGetter.get(keys.get(i).key);
			if (found.isPresent()) {
				resolved[i] = found.get();
			} else {
				complete = false;
				break;
			}
		}
		if (complete) {
			EndesiumBiomeHolders.set(resolved);
		}
	}

	@Inject(method = "collectPossibleBiomes", at = @At("RETURN"), cancellable = true)
	private void endesium$addPossibleBiomes(CallbackInfoReturnable<Stream<Holder<Biome>>> cir) {
		List<Holder<Biome>> endesium = new ArrayList<>();
		for (int i = 0; i < EndesiumRegions.COUNT; i++) {
			Holder<Biome> holder = endesium$forRegion(i);
			if (holder != null) {
				endesium.add(holder);
			}
		}
		if (endesium.size() == EndesiumRegions.COUNT) {
			cir.setReturnValue(Stream.concat(cir.getReturnValue(), endesium.stream()));
		}
	}

	@Inject(method = "getNoiseBiome", at = @At("HEAD"), cancellable = true)
	private void endesium$getNoiseBiome(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
		int blockX = QuartPos.toBlock(x);
		int blockY = QuartPos.toBlock(y);
		int blockZ = QuartPos.toBlock(z);
		int sectionX = SectionPos.blockToSectionCoord(blockX);
		int sectionZ = SectionPos.blockToSectionCoord(blockZ);

		if ((long) sectionX * sectionX + (long) sectionZ * sectionZ <= 4096L) {
			cir.setReturnValue(end);
			return;
		}

		int erosionX = QuartPos.toBlock(x);
		int erosionZ = QuartPos.toBlock(z);
		double erosion = sampler.erosion().compute(
				new DensityFunction.SinglePointContext(erosionX, blockY, erosionZ));
		Holder<Biome> vanilla;
		if (erosion > 0.25D) {
			vanilla = highlands;
		} else if (erosion >= -0.0625D) {
			vanilla = midlands;
		} else if (erosion < -0.21875D) {
			vanilla = islands;
		} else {
			vanilla = barrens;
		}

		if (vanilla != highlands && vanilla != midlands) {
			cir.setReturnValue(vanilla);
			return;
		}

		long seed = EndesiumWorldgenSeeds.get();
		if (!EndesiumWorldgenSeeds.isCaptured()) {
			cir.setReturnValue(vanilla);
			return;
		}

		int region = EndesiumRegions.regionAt(seed, blockX, blockZ);
		Holder<Biome> holder = endesium$forRegion(region);
		if (holder == null) {
			cir.setReturnValue(vanilla);
			return;
		}
		cir.setReturnValue(holder);
	}

	/** Tiny holder so the capture injector can keep the key list readable. */
	@Unique
	private static final class ResourceKeyHolder {
		final net.minecraft.resources.ResourceKey<Biome> key;

		ResourceKeyHolder(net.minecraft.resources.ResourceKey<Biome> key) {
			this.key = key;
		}
	}
}
