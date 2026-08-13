package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
	private ModSounds() {
	}

	public static SoundEvent register(String path) {
		return Registry.register(
				BuiltInRegistries.SOUND_EVENT,
				Endesium.id(path),
				SoundEvent.createVariableRangeEvent(Endesium.id(path))
		);
	}

	public static void register() {
		Endesium.LOGGER.info("Endesium sound registry ready");
	}
}
