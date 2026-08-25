package com.infernodude777.endesium.world;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.entity.CrownSentinelEntity;
import com.infernodude777.endesium.entity.NullwalkerEntity;
import com.infernodude777.endesium.registry.ModEndgear;
import com.infernodude777.endesium.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Miniboss guards for the four flagship builds. Each cathedral or castle
 * spawns its own named honor guard - region mobs wearing pieces of the new
 * gear lines, buffed well past their wild cousins, and guaranteed to drop
 * what they wear. The gear lines enter the world as trophies before they
 * enter the crafting table.
 */
public final class StructureMinibosses {
	private StructureMinibosses() {
	}

	/**
	 * Spawns the honor guard for a flagship at {@code origin}. Called from the
	 * flagship build path right after the hand-authored builders finish, so
	 * guards stand inside the finished structure.
	 */
	public static void spawn(ServerLevel level, BlockPos origin, int region) {
		List<GuardSpec> guards = guardsFor(region);
		int index = 0;
		for (GuardSpec spec : guards) {
			BlockPos at = spec.spot(origin, level, index);
			index++;
			Mob guard = spec.type.create(level);
			if (guard == null) {
				continue;
			}
			guard.moveTo(at.getX() + 0.5D, at.getY() + 0.5D, at.getZ() + 0.5D, 0.0F, 0.0F);
			guard.setCustomName(Component.literal(spec.name));
			guard.setCustomNameVisible(true);
			guard.setPersistenceRequired();
			var health = guard.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
			if (health != null) {
				health.setBaseValue(health.getBaseValue() + 60.0D);
				guard.setHealth(guard.getMaxHealth());
			}
			var damage = guard.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
			if (damage != null) {
				damage.setBaseValue(damage.getBaseValue() + 4.0D);
			}
			guard.addEffect(new net.minecraft.world.effect.MobEffectInstance(
					net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION, 0,
					false, false, true));
			equip(guard, spec.mainhand, spec.chest, spec.boots);
			level.addFreshEntity(guard);
			Endesium.LOGGER.info("Flagship honor guard [{}] spawned at {}", spec.name, at.toShortString());
		}
	}

	private static void equip(Mob guard, Item mainhand, Item chest, Item boots) {
		if (mainhand != null) {
			guard.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(mainhand));
			guard.setDropChance(EquipmentSlot.MAINHAND, 1.0F);
		}
		if (chest != null) {
			guard.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest));
			guard.setDropChance(EquipmentSlot.CHEST, 1.0F);
		}
		if (boots != null) {
			guard.setItemSlot(EquipmentSlot.FEET, new ItemStack(boots));
			guard.setDropChance(EquipmentSlot.FEET, 1.0F);
		}
	}

	private record GuardSpec(String name, EntityType<? extends Mob> type,
			Item mainhand, Item chest, Item boots) {
		/** Guard spots ring the flagship's anchor: front-left, back-right. */
		private BlockPos spot(BlockPos origin, ServerLevel level, int index) {
			int dx = index % 2 == 0 ? 6 : -6;
			int dz = index < 2 ? 6 : -6;
			int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, origin.getX() + dx, origin.getZ() + dz);
			return new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
		}
	}

	private static List<GuardSpec> guardsFor(int region) {
		List<GuardSpec> guards = new ArrayList<>();
		switch (region) {
			case EndesiumRegions.END_WASTES -> {
				guards.add(new GuardSpec("Cathedral Custodian", ModEntities.NULLWALKER,
						ModEndgear.NULL_SWORD, ModEndgear.NULL_CHESTPLATE, ModEndgear.NULL_BOOTS));
				guards.add(new GuardSpec("Dust Sacristan", ModEntities.NULLWALKER,
						ModEndgear.NULL_SWORD, null, com.infernodude777.endesium.registry.ModItems.ASHWALKER_BOOTS));
			}
			case EndesiumRegions.SHATTERED_HIGHLANDS -> {
				guards.add(new GuardSpec("Skyrend Honor Guard", ModEntities.CROWN_SENTINEL,
						ModEndgear.ASH_SWORD, ModEndgear.ASH_CHESTPLATE, com.infernodude777.endesium.registry.ModItems.ASHWALKER_BOOTS));
				guards.add(new GuardSpec("Gale Warden", ModEntities.CROWN_SENTINEL,
						ModEndgear.ASH_AXE, null, ModEndgear.ASH_HELMET));
			}
			case EndesiumRegions.VOID_MARSHES -> {
				guards.add(new GuardSpec("Drowned Cantor", ModEntities.NULLWALKER,
						ModEndgear.NULL_SWORD, ModEndgear.NULL_LEGGINGS, ModEndgear.NULL_BOOTS));
				guards.add(new GuardSpec("Tide Sacristan", ModEntities.MARSH_CRAWLER,
						ModEndgear.NULL_HOE, ModEndgear.NULL_HELMET, null));
			}
			case EndesiumRegions.LUMINOUS_GROVES -> {
				guards.add(new GuardSpec("Prism Acolyte", ModEntities.CROWN_SENTINEL,
						ModEndgear.LUMINOUS_SWORD, ModEndgear.LUMINOUS_CHESTPLATE, ModEndgear.LUMINOUS_BOOTS));
				guards.add(new GuardSpec("Light Sexton", ModEntities.CROWN_SENTINEL,
						ModEndgear.LUMINOUS_AXE, null, ModEndgear.LUMINOUS_HELMET));
			}
			default -> {
				guards.add(new GuardSpec("Vault Sentinel", ModEntities.CROWN_SENTINEL,
						ModEndgear.NULL_SWORD, ModEndgear.NULL_CHESTPLATE, ModEndgear.NULL_BOOTS));
			}
		}
		return guards;
	}
}
