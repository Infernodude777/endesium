package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
	public static final SoundEvent END_WASTES_AMBIENT = register("ambient.end_wastes_low");
	public static final SoundEvent CHORUS_WILDS_AMBIENT = register("ambient.chorus_wilds_low");
	public static final SoundEvent SHATTERED_HIGHLANDS_AMBIENT = register("ambient.shattered_highlands_low");
	public static final SoundEvent VOID_MARSHES_AMBIENT = register("ambient.void_marshes_low");
	public static final SoundEvent LUMINOUS_GROVES_AMBIENT = register("ambient.luminous_groves_low");
	public static final SoundEvent ASHEN_EXPANSE_AMBIENT = register("ambient.ashen_expanse_low");
	public static final SoundEvent CRYSTAL_BARRENS_AMBIENT = register("ambient.crystal_barrens_low");
	public static final SoundEvent VOID_SKIRTS_AMBIENT = register("ambient.void_skirts_low");
	public static final SoundEvent VOID_CROWN_AMBIENT = register("ambient.void_crown_low");
	public static final SoundEvent UMBRAL_REACH_AMBIENT = register("ambient.umbral_reach_low");
	public static final SoundEvent RESONANCE_LENS_ACTIVATE = register("item.resonance_lens.activate");
	public static final SoundEvent RESONANCE_LENS_PULSE_LOW = register("item.resonance_lens.pulse_low");
	public static final SoundEvent RESONANCE_LENS_PULSE_HIGH = register("item.resonance_lens.pulse_high");
	public static final SoundEvent ECHO_COMPASS_USE = register("item.echo_compass.use");
	public static final SoundEvent DRAGON_TRANSFORMATION = register("event.dragon_transformation");
	public static final SoundEvent RUIN_MECHANISM_ACTIVATE = register("block.end_ruin_mechanism.activate");
	public static final SoundEvent VOID_STALKER_IDLE = register("entity.void_stalker.idle");
	public static final SoundEvent VOID_STALKER_ATTACK = register("entity.void_stalker.attack");
	public static final SoundEvent VOID_STALKER_REPOSITION = register("entity.void_stalker.reposition");
	public static final SoundEvent VOID_STALKER_HURT = register("entity.void_stalker.hurt");
	public static final SoundEvent VOID_STALKER_DEATH = register("entity.void_stalker.death");
	public static final SoundEvent DRAGON_SCREECH = register("entity.dragon.screech");
	public static final SoundEvent DRAGON_ROAR = register("entity.dragon.roar");
	public static final SoundEvent WING_SHOCKWAVE = register("entity.dragon.shockwave");
	public static final SoundEvent RESONANCE_STRIKE = register("event.resonance_strike");
	public static final SoundEvent SONIC_BOOM = register("item.resonant_wings.boom");

	public static final SoundEvent DUST_CRAWLER_IDLE = register("entity.dust_crawler.idle");
	public static final SoundEvent DUST_CRAWLER_HURT = register("entity.dust_crawler.hurt");
	public static final SoundEvent DUST_CRAWLER_DEATH = register("entity.dust_crawler.death");
	public static final SoundEvent CHORUS_STALKER_IDLE = register("entity.chorus_stalker.idle");
	public static final SoundEvent CHORUS_STALKER_HURT = register("entity.chorus_stalker.hurt");
	public static final SoundEvent CHORUS_STALKER_DEATH = register("entity.chorus_stalker.death");
	public static final SoundEvent VOID_RAY_IDLE = register("entity.void_ray.idle");
	public static final SoundEvent VOID_RAY_HURT = register("entity.void_ray.hurt");
	public static final SoundEvent VOID_RAY_DEATH = register("entity.void_ray.death");
	public static final SoundEvent MARSH_CRAWLER_IDLE = register("entity.marsh_crawler.idle");
	public static final SoundEvent MARSH_CRAWLER_HURT = register("entity.marsh_crawler.hurt");
	public static final SoundEvent MARSH_CRAWLER_DEATH = register("entity.marsh_crawler.death");
	public static final SoundEvent MARSH_CRAWLER_ATTACK = register("entity.marsh_crawler.attack");
	public static final SoundEvent LUMEN_MOTH_IDLE = register("entity.lumen_moth.idle");
	public static final SoundEvent LUMEN_MOTH_HURT = register("entity.lumen_moth.hurt");
	public static final SoundEvent LUMEN_MOTH_DEATH = register("entity.lumen_moth.death");
	public static final SoundEvent ASH_WRAITH_IDLE = register("entity.ash_wraith.idle");
	public static final SoundEvent ASH_WRAITH_HURT = register("entity.ash_wraith.hurt");
	public static final SoundEvent ASH_WRAITH_DEATH = register("entity.ash_wraith.death");
	public static final SoundEvent ASH_WRAITH_ATTACK = register("entity.ash_wraith.attack");
	public static final SoundEvent CRYSTAL_BURROWER_IDLE = register("entity.crystal_burrower.idle");
	public static final SoundEvent CRYSTAL_BURROWER_HURT = register("entity.crystal_burrower.hurt");
	public static final SoundEvent CRYSTAL_BURROWER_DEATH = register("entity.crystal_burrower.death");
	public static final SoundEvent CRYSTAL_BURROWER_ATTACK = register("entity.crystal_burrower.attack");
	public static final SoundEvent NULLWALKER_IDLE = register("entity.nullwalker.idle");
	public static final SoundEvent NULLWALKER_HURT = register("entity.nullwalker.hurt");
	public static final SoundEvent NULLWALKER_DEATH = register("entity.nullwalker.death");
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
		Endesium.LOGGER.info("Registered Endesium sound events");
	}
}
