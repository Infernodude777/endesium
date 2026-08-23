package com.infernodude777.endesium.client.particle;

import com.infernodude777.endesium.particle.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class ResonanceMoteParticle extends TextureSheetParticle {
	protected ResonanceMoteParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, SimpleParticleType type) {
		super(level, x, y, z, 0.0D, -0.02D, 0.0D);
		this.setSpriteFromAge(sprites);
		this.lifetime = type == ModParticles.RESONANCE_BEAM ? 8 : 24 + this.random.nextInt(16);
		this.quadSize = type == ModParticles.RESONANCE_BEAM
				? 0.075F
				: 0.05F + this.random.nextFloat() * 0.05F;
		this.gravity = 0.0F;
		float[] tint = tint(type);
		this.setColor(tint[0], tint[1], tint[2]);
	}

	private static float[] tint(SimpleParticleType type) {
		if (type == ModParticles.RESONANCE_PULSE || type == ModParticles.RESONANCE_ACTIVE) {
			return new float[] { 0.66F, 0.9F, 0.87F };
		}
		if (type == ModParticles.RUIN_GOLD_CONTACT) {
			return new float[] { 0.78F, 0.66F, 0.35F };
		}
		if (type == ModParticles.VOID_STALKER_TRACE) {
			return new float[] { 0.31F, 0.28F, 0.36F };
		}
		if (type == ModParticles.CHORUS_SPORE) {
			return new float[] { 0.77F, 0.72F, 0.84F };
		}
		if (type == ModParticles.RESONANCE_BEAM) {
			return new float[] { 1.0F, 1.0F, 1.0F };
		}
		// Region ambience: desaturated, region-keyed tints.
		if (type == ModParticles.END_WASTES_MOTE) {
			return new float[] { 0.62F, 0.58F, 0.55F };
		}
		if (type == ModParticles.HIGHLAND_WIND) {
			return new float[] { 0.72F, 0.74F, 0.78F };
		}
		if (type == ModParticles.MARSH_MIST) {
			return new float[] { 0.45F, 0.52F, 0.50F };
		}
		if (type == ModParticles.LUMEN_MOTE) {
			return new float[] { 0.85F, 0.92F, 0.70F };
		}
		if (type == ModParticles.ASH_MOTE) {
			return new float[] { 0.80F, 0.60F, 0.38F };
		}
		if (type == ModParticles.CRYSTAL_MOTE) {
			return new float[] { 0.68F, 0.75F, 0.88F };
		}
		if (type == ModParticles.NULL_DISTORTION) {
			return new float[] { 0.22F, 0.20F, 0.26F };
		}
		if (type == ModParticles.VOID_SKIRT_MOTE) {
			return new float[] { 0.52F, 0.50F, 0.62F };
		}
		if (type == ModParticles.VOID_CROWN_MOTE) {
			return new float[] { 0.66F, 0.62F, 0.72F };
		}
		if (type == ModParticles.UMBRAL_MOTE) {
			return new float[] { 0.34F, 0.32F, 0.40F };
		}
		return new float[] { 0.55F, 0.5F, 0.58F };
	}

	@Override
	public void tick() {
		super.tick();
		this.alpha = 1.0F - (float) this.age / (float) this.lifetime;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Factory(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new ResonanceMoteParticle(level, x, y, z, this.sprites, type);
		}
	}
}