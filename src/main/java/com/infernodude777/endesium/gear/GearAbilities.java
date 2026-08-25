package com.infernodude777.endesium.gear;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModEndgear;
import com.infernodude777.endesium.registry.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Passive gear abilities, ticked on the server. Each full armor set carries a
 * permanent signature, and the Dragon Wings exact their price:
 *
 * <ul>
 *   <li><b>Luminous</b> full set: infinite Night Vision plus a personal
 *       glow - the grove's light lives in the wearer.</li>
 *   <li><b>Ash</b> full set: permanent fire immunity - the ashwalker's
 *       birthright, extended from the boots to the whole plate.</li>
 *   <li><b>Null</b> full set: permanent Slow Falling - deleted gravity.</li>
 *   <li><b>Dragon Wings</b> (any piece worn): permanent Slowness I - the
 *       weight of the dragon's flight.</li>
 * </ul>
 *
 * Effects refresh with a 60-tick buffer under their 40-tick reapplication
 * cadence so there is never an unprotected tick, and all refreshes are
 * silent (no particles, no icons flickering).
 */
public final class GearAbilities {
	private static final int REFRESH = 40;
	private static final int BUFFER = 120;

	private GearAbilities() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(GearAbilities::tick);
		Endesium.LOGGER.info("Registered gear ability layer (luminous, ash, null, dragon wings)");
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % REFRESH != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			applySet(player, ModEndgear.LUMINOUS_HELMET, ModEndgear.LUMINOUS_CHESTPLATE,
					ModEndgear.LUMINOUS_LEGGINGS, ModEndgear.LUMINOUS_BOOTS, () -> {
						player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, BUFFER, 0, false, false, true));
						player.addEffect(new MobEffectInstance(MobEffects.GLOWING, BUFFER, 0, false, false, true));
					});
			applySet(player, ModEndgear.ASH_HELMET, ModEndgear.ASH_CHESTPLATE,
					ModEndgear.ASH_LEGGINGS, ModItems.ASHWALKER_BOOTS, () ->
							player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, BUFFER, 0, false, false, true)));
			applySet(player, ModEndgear.NULL_HELMET, ModEndgear.NULL_CHESTPLATE,
					ModEndgear.NULL_LEGGINGS, ModEndgear.NULL_BOOTS, () ->
							player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, BUFFER, 0, false, false, true)));
			if (player.getItemBySlot(EquipmentSlot.CHEST).is(ModEndgear.DRAGON_WINGS)) {
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BUFFER, 0, false, false, true));
			}
		}
	}

	private static void applySet(ServerPlayer player, Item helmet, Item chest, Item legs, Item boots,
			Runnable effect) {
		if (player.getItemBySlot(EquipmentSlot.HEAD).is(helmet)
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(chest)
				&& player.getItemBySlot(EquipmentSlot.LEGS).is(legs)
				&& player.getItemBySlot(EquipmentSlot.FEET).is(boots)) {
			effect.run();
		}
	}
}
