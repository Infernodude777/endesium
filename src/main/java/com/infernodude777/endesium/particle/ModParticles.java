package com.infernodude777.endesium.particle;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModParticles {
	public static final SimpleParticleType END_WASTES_MOTE = register("end_wastes_mote");
	public static final SimpleParticleType RESONANCE_PULSE = register("resonance_pulse");
	public static final SimpleParticleType RESONANCE_ACTIVE = register("resonance_active");
	public static final SimpleParticleType RUIN_GOLD_CONTACT = register("ruin_gold_contact");
	public static final SimpleParticleType VOID_STALKER_TRACE = register("void_stalker_trace");
	public static final SimpleParticleType CHORUS_SPORE = register("chorus_spore");
	/** Short-lived white particles used to draw the Lens-to-mechanism signal line. */
	public static final SimpleParticleType RESONANCE_BEAM = register("resonance_beam");

	// Biome ambience for the ecology overhaul.
	public static final SimpleParticleType HIGHLAND_WIND = register("highland_wind");
	public static final SimpleParticleType MARSH_MIST = register("marsh_mist");
	public static final SimpleParticleType LUMEN_MOTE = register("lumen_mote");
	public static final SimpleParticleType ASH_MOTE = register("ash_mote");
	public static final SimpleParticleType CRYSTAL_MOTE = register("crystal_mote");

	public static final SimpleParticleType NULL_DISTORTION = register("null_distortion");

	// Void biome ambience.
	public static final SimpleParticleType VOID_SKIRT_MOTE = register("void_skirt_mote");
	public static final SimpleParticleType VOID_CROWN_MOTE = register("void_crown_mote");
	public static final SimpleParticleType UMBRAL_MOTE = register("umbral_mote");
	private ModParticles() {
	}

	private static SimpleParticleType register(String path) {
		return Registry.register(BuiltInRegistries.PARTICLE_TYPE, Endesium.id(path), new ModParticleType(false));
	}

	/** SimpleParticleType's boolean constructor is protected; expose it for registration. */
	private static final class ModParticleType extends SimpleParticleType {
		private ModParticleType(boolean bl) {
			super(bl);
		}
	}

	public static void register() {
		Endesium.LOGGER.info("Registered Endesium particle types");
	}
}
