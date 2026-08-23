package com.infernodude777.endesium.state;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Persistent, server-side world state for the Endesium post-Dragon
 * transformation. Dragon defeat is a world-level event, so it is stored in the
 * world's data storage (never on a player). It survives server restarts and is
 * identical for every player on a dedicated server.
 *
 * <p>Vanilla may respawn the Dragon and have it killed again; Endesium only
 * performs the transformation once. {@link #markDragonDefeated()} is
 * idempotent: it returns {@code true} exactly once per world.</p>
 */
public final class PostDragonState extends SavedData {
	public static final String FILE_NAME = "endesium_post_dragon";
	public static final int CURRENT_VERSION = 1;

	private static final SavedData.Factory<PostDragonState> FACTORY =
			new SavedData.Factory<>(PostDragonState::new, PostDragonState::load,
					DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private boolean dragonDefeated;
	private boolean transformationActive;
	private int transformationVersion = CURRENT_VERSION;

	public PostDragonState() {
	}

	public static PostDragonState load(CompoundTag tag, HolderLookup.Provider registries) {
		PostDragonState state = new PostDragonState();
		state.dragonDefeated = tag.getBoolean("DragonDefeated");
		state.transformationActive = tag.getBoolean("TransformationActive");
		state.transformationVersion = tag.getInt("TransformationVersion");
		if (state.transformationVersion == 0) {
			state.transformationVersion = CURRENT_VERSION;
		}
		return state;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		tag.putBoolean("DragonDefeated", dragonDefeated);
		tag.putBoolean("TransformationActive", transformationActive);
		tag.putInt("TransformationVersion", transformationVersion);
		return tag;
	}

	public boolean isDragonDefeated() {
		return dragonDefeated;
	}

	public boolean isTransformationActive() {
		return transformationActive;
	}

	public int transformationVersion() {
		return transformationVersion;
	}

	/**
	 * Records the Dragon defeat and activates the transformation. Returns
	 * {@code true} only for the world's first activation so callers can fire
	 * the one-time transformation event; respawned-Dragon re-kills return
	 * {@code false} and never reset or duplicate the state.
	 */
	public boolean markDragonDefeated() {
		if (transformationActive) {
			return false;
		}
		dragonDefeated = true;
		transformationActive = true;
		setDirty();
		Endesium.LOGGER.info("Endesium post-Dragon transformation activated (version {})", transformationVersion);
		return true;
	}

	/** Development-only reset used by the op-gated testing command. */
	public void resetForTesting() {
		dragonDefeated = false;
		transformationActive = false;
		setDirty();
	}

	public static PostDragonState get(ServerLevel endLevel) {
		return endLevel.getServer().overworld().getDataStorage()
				.computeIfAbsent(FACTORY, FILE_NAME);
	}

	public static PostDragonState get(MinecraftServer server) {
		return server.overworld().getDataStorage()
				.computeIfAbsent(FACTORY, FILE_NAME);
	}
}
