package com.infernodude777.endesium.state;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Persistent per-player record of the boss-reward meta progression:
 * which regions' wardens have been attuned, how many golem cores have been
 * absorbed, and whether the once-per-day Golem's Resolve is available.
 *
 * <p>Stored in the overworld's data storage (like {@link PostDragonState})
 * so it survives restarts and never lives on a client.</p>
 */
public final class AttunementState extends SavedData {
	public static final String FILE_NAME = "endesium_attunements";

	/** Regions attuned required for the Warden Ascendant ascension. */
	public static final int REGIONS_FOR_ASCENSION = 10;
	/** Cores absorbed required to unlock Golem's Resolve. */
	public static final int CORES_FOR_RESOLVE = 10;

	private static final SavedData.Factory<AttunementState> FACTORY =
			new SavedData.Factory<>(AttunementState::new, AttunementState::load,
					net.minecraft.util.datafix.DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private final java.util.Map<UUID, PlayerRecord> records = new java.util.HashMap<>();

	public static class PlayerRecord {
		public final Set<Integer> attunedRegions = new HashSet<>();
		public int coresAbsorbed;
		public boolean resolveUnlocked;
		public long lastResolveDay = -1L;
	}

	public static PlayerRecord emptyRecord() {
		return new PlayerRecord();
	}

	public static AttunementState load(CompoundTag tag, HolderLookup.Provider registries) {
		AttunementState state = new AttunementState();
		CompoundTag players = tag.getCompound("Players");
		for (String key : players.getAllKeys()) {
			try {
				UUID id = UUID.fromString(key);
				CompoundTag pTag = players.getCompound(key);
				PlayerRecord rec = new PlayerRecord();
				for (int region : pTag.getIntArray("Regions")) rec.attunedRegions.add(region);
				rec.coresAbsorbed = pTag.getInt("CoresAbsorbed");
				rec.resolveUnlocked = pTag.getBoolean("ResolveUnlocked");
				rec.lastResolveDay = pTag.getLong("LastResolveDay");
				state.records.put(id, rec);
			} catch (IllegalArgumentException ignored) {
				// Corrupt key: skip rather than fail the whole save.
			}
		}
		return state;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		CompoundTag players = new CompoundTag();
		for (var entry : records.entrySet()) {
			CompoundTag pTag = new CompoundTag();
			pTag.put("Regions", new IntArrayTag(entry.getValue().attunedRegions.stream().mapToInt(Integer::intValue).toArray()));
			pTag.putInt("CoresAbsorbed", entry.getValue().coresAbsorbed);
			pTag.putBoolean("ResolveUnlocked", entry.getValue().resolveUnlocked);
			pTag.putLong("LastResolveDay", entry.getValue().lastResolveDay);
			players.put(entry.getKey().toString(), pTag);
		}
		tag.put("Players", players);
		return tag;
	}

	private PlayerRecord record(UUID id) {
		return records.computeIfAbsent(id, k -> new PlayerRecord());
	}

	/**
	 * Records a sigil attunement for the player's region. Returns true if
	 * this attunement completed the Warden Ascendant (all ten regions).
	 */
	public boolean markRegionAttuned(ServerPlayer player, int region) {
		PlayerRecord rec = record(player.getUUID());
		boolean added = rec.attunedRegions.add(region);
		if (added) setDirty();
		if (!added || rec.attunedRegions.size() < REGIONS_FOR_ASCENSION) return false;
		return true;
	}

	public boolean isAscendant(ServerPlayer player) {
		return record(player.getUUID()).attunedRegions.size() >= REGIONS_FOR_ASCENSION;
	}

	public Set<Integer> attunedRegions(ServerPlayer player) {
		return Set.copyOf(record(player.getUUID()).attunedRegions);
	}

	/** Returns true if this absorption just crossed the Resolve threshold. */
	public boolean recordCoreAbsorbed(ServerPlayer player) {
		PlayerRecord rec = record(player.getUUID());
		rec.coresAbsorbed++;
		setDirty();
		if (!rec.resolveUnlocked && rec.coresAbsorbed >= CORES_FOR_RESOLVE) {
			rec.resolveUnlocked = true;
			setDirty();
			return true;
		}
		return false;
	}

	public boolean isResolveUnlocked(ServerPlayer player) {
		return record(player.getUUID()).resolveUnlocked;
	}

	public boolean tryConsumeResolve(ServerPlayer player) {
		PlayerRecord rec = record(player.getUUID());
		if (!rec.resolveUnlocked) return false;
		long today = player.serverLevel().getDayTime() / 24000L;
		if (rec.lastResolveDay == today) return false;
		rec.lastResolveDay = today;
		setDirty();
		return true;
	}

	public static AttunementState get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(FACTORY, FILE_NAME);
	}

	/** The Ascendant's passive pulse: regeneration + visible motes, End only. */
	public static void applyAscendantPulse(ServerPlayer player) {
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, false, false, true));
		player.serverLevel().sendParticles(ParticleTypes.END_ROD,
				player.getX(), player.getY() + 1.0D, player.getZ(),
				6, 0.5D, 0.6D, 0.5D, 0.01D);
	}
}
