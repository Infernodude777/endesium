package com.infernodude777.endesium.resonance;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Server-side registry of loaded mechanism signals. Signals stay bounded and
 * qualitative: the client (and even the player) never learns exact source
 * positions through the Lens. Band and an eight-way direction bucket are the
 * only information that ever leaves the server. Falloff is computed from each
 * source's own detection radius, so the far-reaching SPIRE_CORE signal of the
 * Shattered Spire can be felt across the wastes while ruin signals stay local.
 */
public final class ResonanceManager {
	private static final Map<ServerLevel, ResonanceManager> INSTANCES = new WeakHashMap<>();
	private static final int MAX_SEARCH_RADIUS = 512;
	private static final String[] CARDINALS = { "east", "southeast", "south", "southwest",
			"west", "northwest", "north", "northeast" };

	private final ServerLevel level;
	private final Map<BlockPos, ResonanceSource> sources = new HashMap<>();

	private ResonanceManager(ServerLevel level) {
		this.level = level;
	}

	public static ResonanceManager get(ServerLevel level) {
		return INSTANCES.computeIfAbsent(level, ResonanceManager::new);
	}

	public void registerSource(BlockPos position, ResonanceType type, int radius, float strength, boolean active) {
		BlockPos key = position.immutable();
		sources.compute(key, (ignored, existing) -> {
			if (existing == null) {
				return new ResonanceSource(key, type, Math.min(radius, MAX_SEARCH_RADIUS), strength, active);
			}
			existing.update(type, Math.min(radius, MAX_SEARCH_RADIUS), strength, active);
			return existing;
		});
	}

	public void unregisterSource(BlockPos position) {
		sources.remove(position);
	}

	/**
	 * Samples only sources in this End dimension and returns the strongest
	 * visible signal. The source position never leaves the server through this
	 * class's player feedback path. Sources whose chunk is not loaded are
	 * skipped so stale entries can never produce phantom signals.
	 */
	public Signal sample(ServerPlayer player) {
		if (player.level().dimension() != Level.END) return Signal.none();

		Signal strongest = Signal.none();
		java.util.Iterator<ResonanceSource> iterator = sources.values().iterator();
		while (iterator.hasNext()) {
			ResonanceSource source = iterator.next();
			// A source whose chunk is no longer loaded is a stale cache entry
			// (crash, unload without removal). Its block entity re-registers on
			// the first server tick after the chunk reloads, so pruning here
			// keeps the registry bounded without ever losing live signals.
			if (!level.isLoaded(source.position())) {
				iterator.remove();
				continue;
			}
			double distanceSqr = source.position().distSqr(player.blockPosition());
			if (distanceSqr > (double) source.detectionRadius() * source.detectionRadius()) continue;

			double distance = Math.sqrt(distanceSqr);
			float visibility = visibilityFactor(player, source);
			float strength = (float) Math.clamp((source.detectionRadius() - distance) / 96.0D, 0.0D, 1.0D)
					* source.strength() * visibility;
			strength = Math.clamp(strength, 0.0F, 1.0F);
			Band band = distance <= 16 ? Band.VERY_CLOSE
					: distance <= 32 ? Band.CLOSE
					: distance <= 64 ? Band.MEDIUM
					: Band.FAR;
			Signal candidate = new Signal(band, strength, source.position(), source.active(),
					directionBucket(player, source.position()), source.type());
			if (candidate.strength() > strongest.strength()) strongest = candidate;
		}
		return strongest;
	}

	private float visibilityFactor(ServerPlayer player, ResonanceSource source) {
		Vec3 from = player.getEyePosition();
		Vec3 to = Vec3.atCenterOf(source.position());
		BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().distSqr(source.position()) > 1.0D) {
			return 0.35F;
		}
		return 1.0F;
	}

	public void emitLensFeedback(ServerPlayer player) {
		Signal signal = sample(player);
		if (signal.band() == Band.NONE) {
			player.displayClientMessage(Component.literal("The lens is quiet."), true);
			return;
		}

		player.displayClientMessage(Component.literal(describe(signal)), true);
		boolean strong = signal.band() == Band.VERY_CLOSE;
		Vec3 audioOffset = directionOffset(signal.directionBucket());
		BlockPos audioPos = BlockPos.containing(player.position().add(audioOffset));
		level.playSound(null, audioPos,
				strong ? ModSounds.RESONANCE_LENS_PULSE_HIGH : ModSounds.RESONANCE_LENS_PULSE_LOW,
				SoundSource.PLAYERS, 0.25F + signal.strength() * 0.4F, 0.65F + signal.strength() * 0.4F);
		int count = switch (signal.band()) {
			case FAR -> 1;
			case MEDIUM -> 2;
			case CLOSE -> 3;
			case VERY_CLOSE -> 4;
			case NONE -> 0;
		};
		level.sendParticles(ModParticles.RESONANCE_PULSE, player.getX(), player.getY(0.7D), player.getZ(), count,
				0.25D, 0.25D, 0.25D, 0.01D);
	}

	/**
	 * Bounded, qualitative readout. Distant strong relics read differently
	 * from ordinary dormant mechanisms so the player learns to distinguish
	 * "something here" from "something out there" without ever seeing a
	 * coordinate or a waypoint.
	 */
	/** Maps an eight-way direction bucket to a readable cardinal name. */
	public static String cardinal(int bucket) {
		if (bucket < 0 || bucket >= CARDINALS.length) return "the void";
		return CARDINALS[bucket];
	}

	private static String describe(Signal signal) {
		String message = switch (signal.band()) {
			case FAR -> switch (signal.sourceType()) {
				case STRONG_RELIC -> "A strong resonance answers from the wastes.";
				case SPIRE_CORE -> "The lens is almost overwhelmed.";
				case AWAKENED_ARCHIVE -> "A deep resonance answers from beyond the wastes.";
				default -> "The lens is listening.";
			};
			case MEDIUM -> switch (signal.sourceType()) {
				case STRONG_RELIC -> "A strong resonance answers from the wastes.";
				case SPIRE_CORE -> "The lens is almost overwhelmed.";
				case AWAKENED_ARCHIVE -> "A deep resonance answers from beyond the wastes.";
				default -> "A distant resonance answers.";
			};
			case CLOSE -> "The resonance is close.";
			case VERY_CLOSE -> "The lens is answering.";
			case NONE -> "The lens is quiet.";
		};
		if (signal.directionBucket() >= 0 && signal.band() != Band.FAR) {
			message = message + " It pulls toward the " + cardinal(signal.directionBucket()) + ".";
		}
		return message;
	}

	public boolean activate(BlockPos position, ServerPlayer player) {
		if (player.level() != level || player.distanceToSqr(Vec3.atCenterOf(position)) > 16.0D) return false;

		ResonanceSource source = sources.get(position);
		if (source == null) {
			registerSource(position, ResonanceType.ACTIVE_MECHANISM, 96, 1.35F, true);
			source = sources.get(position);
		} else {
			source.update(ResonanceType.ACTIVE_MECHANISM, 96, 1.35F, true);
		}
		level.sendParticles(ModParticles.RESONANCE_ACTIVE, position.getX() + 0.5D, position.getY() + 1.0D,
				position.getZ() + 0.5D, 12, 0.35D, 0.25D, 0.35D, 0.04D);
		level.sendParticles(ModParticles.RUIN_GOLD_CONTACT, position.getX() + 0.5D, position.getY() + 0.5D,
				position.getZ() + 0.5D, 4, 0.25D, 0.15D, 0.25D, 0.02D);
		level.playSound(null, position, ModSounds.RUIN_MECHANISM_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.7F);
		Endesium.LOGGER.info("Activated Endesium resonance source at {} for {}", position, player.getGameProfile().getName());
		return true;
	}

	private static int directionBucket(ServerPlayer player, BlockPos source) {
		double dx = source.getX() + 0.5D - player.getX();
		double dz = source.getZ() + 0.5D - player.getZ();
		// atan2 uses +X=east and +Z=south, which matches CARDINALS[0..7].
		// Adding PI here rotated every Lens and Compass direction by 180 degrees.
		int bucket = (int) Math.floor(Math.atan2(dz, dx) / (Math.PI * 2.0D) * 8.0D + 0.5D);
		return Math.floorMod(bucket, 8);
	}

	private static Vec3 directionOffset(int bucket) {
		if (bucket < 0) return Vec3.ZERO;
		double angle = (bucket / 8.0D) * Math.PI * 2.0D;
		return new Vec3(Math.cos(angle) * 2.0D, 0.0D, Math.sin(angle) * 2.0D);
	}

	public enum Band { NONE, FAR, MEDIUM, CLOSE, VERY_CLOSE }

	public record Signal(Band band, float strength, BlockPos sourcePosition, boolean active,
			int directionBucket, ResonanceType sourceType) {
		public static Signal none() {
			return new Signal(Band.NONE, 0.0F, BlockPos.ZERO, false, -1, ResonanceType.DORMANT_RELIC);
		}
	}
}
