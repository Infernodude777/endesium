package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.resonance.ResonanceType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * The three End Ruin variants that share one generation feature, plus the
 * landmark-only SPIRE variant. Each ruin variant has a reason to exist: INTACT
 * is the common introductory station, FRACTURED carries a stronger resonance
 * and better rewards, and SUNKEN hides the most meaningful clue behind a
 * buried floor. SPIRE is never picked by ruin generation; the Shattered Spire
 * landmark sets it directly on its own mechanism.
 */
public enum EndRuinVariant {
	INTACT(5, ResonanceType.DORMANT_RELIC, 96, 1.0F, "end_ruin"),
	FRACTURED(3, ResonanceType.STRONG_RELIC, 128, 1.5F, "end_ruin_fractured"),
	SUNKEN(2, ResonanceType.DORMANT_RELIC, 96, 1.1F, "end_ruin_sunken"),
	SPIRE(0, ResonanceType.SPIRE_CORE, 384, 1.6F, "end_spire"),
	/** The post-Dragon archive landmark; never picked by ruin generation. */
	ARCHIVE(0, ResonanceType.AWAKENED_ARCHIVE, 512, 1.8F, "end_archive"),
	// Named biome landmarks. These are never selected by ordinary End Ruins;
	// their zero weights keep the generator from masquerading as a different
	// discovery while still giving each landmark its own persistent signal.
	RIFT_OBSERVATORY(0, ResonanceType.STRONG_RELIC, 160, 1.3F, "highland_observatory"),
	WINDSCAR_LIFT(0, ResonanceType.STRONG_RELIC, 144, 1.25F, "windscar_lift"),
	TIDE_BELL(0, ResonanceType.STRONG_RELIC, 128, 1.2F, "marsh_tide_bell"),
	MIRE_RELIQUARY(0, ResonanceType.STRONG_RELIC, 128, 1.25F, "mire_reliquary"),
	BLOOM_CONSERVATORY(0, ResonanceType.STRONG_RELIC, 144, 1.3F, "bloom_conservatory"),
	PRISM_CANOPY(0, ResonanceType.STRONG_RELIC, 144, 1.3F, "prism_canopy"),
	CROWN_OBSERVATORY(0, ResonanceType.STRONG_RELIC, 192, 1.4F, "crown_observatory"),
	CROWNSTEP(0, ResonanceType.STRONG_RELIC, 176, 1.35F, "crownstep_procession"),
	NULL_ARCHIVE(0, ResonanceType.STRONG_RELIC, 160, 1.4F, "null_archive"),
	HOLLOW_THRESHOLD(0, ResonanceType.STRONG_RELIC, 176, 1.4F, "hollow_threshold"),
	/** Mini-mechanism placed by RegionLandmarkFeature; never randomly generated. */
	LANDMARK_BEACON(0, ResonanceType.DORMANT_RELIC, 96, 1.0F, "biome_landmark");

	private final int weight;
	private final ResonanceType resonanceType;
	private final int resonanceRadius;
	private final float resonanceStrength;
	private final ResourceKey<LootTable> lootTable;

	EndRuinVariant(int weight, ResonanceType resonanceType, int resonanceRadius,
			float resonanceStrength, String lootTableId) {
		this.weight = weight;
		this.resonanceType = resonanceType;
		this.resonanceRadius = resonanceRadius;
		this.resonanceStrength = resonanceStrength;
		this.lootTable = ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("chests/" + lootTableId));
	}

	public int weight() {
		return weight;
	}

	public ResonanceType resonanceType() {
		return resonanceType;
	}

	public int resonanceRadius() {
		return resonanceRadius;
	}

	public float resonanceStrength() {
		return resonanceStrength;
	}

	public ResourceKey<LootTable> lootTable() {
		return lootTable;
	}

	/**
	 * Weighted pick over the three ruin variants only. SPIRE and ARCHIVE have
	 * weight zero and are deliberately excluded so End Ruins can never
	 * masquerade as the landmark or the post-Dragon archive.
	 */
	public static EndRuinVariant pick(RandomSource random) {
		int roll = random.nextInt(INTACT.weight + FRACTURED.weight + SUNKEN.weight);
		if (roll < INTACT.weight) {
			return INTACT;
		}
		if (roll < INTACT.weight + FRACTURED.weight) {
			return FRACTURED;
		}
		return SUNKEN;
	}
}
