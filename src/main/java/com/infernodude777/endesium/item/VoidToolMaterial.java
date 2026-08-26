package com.infernodude777.endesium.item;

import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * The Void tool tier. Sits between diamond and netherite: fast, tough, and
 * highly enchantable, but not a band-aid that invalidates vanilla gear. It
 * repairs with Void Ingots, keeping the material in the loop.
 */
public enum VoidToolMaterial implements Tier {
	INSTANCE;

	@Override
	public int getUses() {
		return 3040;
	}

	@Override
	public float getSpeed() {
		return 9.2F;
	}

	@Override
	public float getAttackDamageBonus() {
		return 4.2F;
	}

	@Override
	public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
		return net.minecraft.tags.BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
	}

	@Override
	public int getEnchantmentValue() {
		return 16;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.of(ModItems.VOID_INGOT);
	}
}
