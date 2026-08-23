package com.infernodude777.endesium.item;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side abilities shared by the Void armor set and its tools.
 *
 * Each armor piece grants a passive bonus while worn (helmet: Void Sight,
 * chestplate: Gravitic Guard, leggings: Void Channeling, boots: Anchor).
 * Wearing the boots also unlocks the axe dash, the leggings unlock the
 * pickaxe resonance, and the full set unlocks the sword's singularity,
 * so each piece is meaningful on its own and the set is stronger together.
 */
public final class VoidEquipmentAbilities {
	private static final ResourceLocation BOOTS_KNOCKBACK_ID = Endesium.id("void_boots_knockback");
	private static final AttributeModifier BOOTS_KNOCKBACK = new AttributeModifier(
			BOOTS_KNOCKBACK_ID, 0.25D, AttributeModifier.Operation.ADD_VALUE);

	private VoidEquipmentAbilities() {
	}

	/** Checks if the entity is wearing the complete Void armor set. */
	public static boolean isFullVoidArmor(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.VOID_HELMET)
				&& entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VOID_CHESTPLATE)
				&& entity.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.VOID_LEGGINGS)
				&& entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.VOID_BOOTS);
	}

	/**
	 * Called every server tick for each player. Applies passive armor bonuses
	 * and held-tool effects. Effects are reapplied each tick with a short
	 * duration so they never stack and always expire when the piece comes off.
	 */
	public static void tick(ServerPlayer player) {
		ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
		ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
		ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
		ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

		// Helmet: Void Sight — Night Vision + Water Breathing
		if (helmet.is(ModItems.VOID_HELMET)) {
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 45, 0, true, false, true));
			player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 45, 0, true, false, true));
		}

		// Chestplate: Gravitic Guard — Resistance I + emergency absorption
		// below half health, refreshed every 10 seconds while worn.
		if (chestplate.is(ModItems.VOID_CHESTPLATE)) {
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 45, 0, true, false, true));
			if (player.tickCount % 200 == 0 && player.getHealth() <= player.getMaxHealth() * 0.5F) {
				player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0, true, true, true));
			}
		}

		// Leggings: Void Channeling — Haste II
		if (leggings.is(ModItems.VOID_LEGGINGS)) {
			player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 45, 1, true, false, true));
		}

		// Boots: Anchor — knockback resistance. Transient attribute so the
		// bonus is applied and removed cleanly as the boots come on and off.
		AttributeInstance knockback = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		if (boots.is(ModItems.VOID_BOOTS)) {
			if (knockback != null && !knockback.hasModifier(BOOTS_KNOCKBACK_ID)) {
				knockback.addTransientModifier(BOOTS_KNOCKBACK);
			}
		} else if (knockback != null && knockback.hasModifier(BOOTS_KNOCKBACK_ID)) {
			knockback.removeModifier(BOOTS_KNOCKBACK_ID);
		}

		// Passives for the held tool — the sword's strength, the pickaxe's
		// dig speed, the axe's reach and the shovel/hoe's sturdy grip.
		applyToolEffect(player, player.getMainHandItem());
	}

	/** Applies the passive effect for the currently held Void tool. */
	private static void applyToolEffect(ServerPlayer player, ItemStack held) {
		if (held.is(ModItems.VOID_SWORD)) {
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 25, 0, true, false, true));
		} else if (held.is(ModItems.VOID_PICKAXE)) {
			player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 25, 1, true, false, true));
		} else if (held.is(ModItems.VOID_AXE)) {
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 25, 1, true, false, true));
		} else if (held.is(ModItems.VOID_SHOVEL)) {
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 0, true, false, true));
		} else if (held.is(ModItems.VOID_HOE)) {
			player.addEffect(new MobEffectInstance(MobEffects.JUMP, 25, 0, true, false, true));
		}
	}

	/** Counts how many Void pieces the entity is wearing (0-4). */
	public static int countVoidPieces(LivingEntity entity) {
		int count = 0;
		if (entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.VOID_HELMET)) count++;
		if (entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VOID_CHESTPLATE)) count++;
		if (entity.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.VOID_LEGGINGS)) count++;
		if (entity.getItemBySlot(EquipmentSlot.FEET).is(ModItems.VOID_BOOTS)) count++;
		return count;
	}

	/** Used by the black hole and the pickaxe blast to protect full-set wearers. */
	public static boolean isProtectedFromBlackHole(LivingEntity entity) {
		return isFullVoidArmor(entity);
	}
}