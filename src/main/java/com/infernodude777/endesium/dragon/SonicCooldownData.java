package com.infernodude777.endesium.dragon;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent, server-side Sonic Boom cooldowns keyed by player UUID. Because
 * the state lives in world data rather than on the item or the player, the
 * cooldown cannot be bypassed by swapping items, reconnecting, or dying. Old
 * entries are pruned so the map stays bounded.
 */
public final class SonicCooldownData extends SavedData {
	public static final String FILE_NAME = "endesium_sonic_cooldowns";

	private static final SavedData.Factory<SonicCooldownData> FACTORY =
			new SavedData.Factory<>(SonicCooldownData::new, SonicCooldownData::load,
					DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private final Map<UUID, Long> readyAtGameTime = new HashMap<>();

	public SonicCooldownData() {
	}

	public static SonicCooldownData load(CompoundTag tag, HolderLookup.Provider registries) {
		SonicCooldownData data = new SonicCooldownData();
		ListTag list = tag.getList("Cooldowns", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			try {
				data.readyAtGameTime.put(entry.getUUID("U"), entry.getLong("T"));
			} catch (IllegalArgumentException ignored) {
				// Malformed or stale entry; skip.
			}
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag list = new ListTag();
		for (Map.Entry<UUID, Long> entry : readyAtGameTime.entrySet()) {
			CompoundTag item = new CompoundTag();
			item.putUUID("U", entry.getKey());
			item.putLong("T", entry.getValue());
			list.add(item);
		}
		tag.put("Cooldowns", list);
		return tag;
	}

	public boolean onCooldown(UUID player, long gameTime) {
		Long readyAt = readyAtGameTime.get(player);
		return readyAt != null && readyAt > gameTime;
	}

	public void setCooldown(UUID player, long readyGameTime) {
		readyAtGameTime.put(player, readyGameTime);
		if (readyAtGameTime.size() > 64) {
			readyAtGameTime.entrySet().removeIf(entry -> entry.getValue() <= readyGameTime - 3600L);
		}
		setDirty();
	}

	public static SonicCooldownData get(MinecraftServer server) {
		return server.overworld().getDataStorage()
				.computeIfAbsent(FACTORY, FILE_NAME);
	}
}
