package com.infernodude777.endesium.item;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

/**
 * Materials for the Luminous, Ash, and Null gear lines. Each armor material
 * registers its own layer textures (luminous_layer_*, null_layer_*) following
 * the exact Void set model; the Ash line reuses the existing ashen layers so
 * the Ashwalker Boots and the new pieces match perfectly.
 */
public final class EndgearMaterials {
	/** Luminous: grove-light plate. Glassy, bright, enchant-friendly. */
	public static final Holder<ArmorMaterial> LUMINOUS = Registry.registerForHolder(
			BuiltInRegistries.ARMOR_MATERIAL,
			Endesium.id("luminous"),
			new ArmorMaterial(
					Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
						map.put(ArmorItem.Type.BOOTS, 3);
						map.put(ArmorItem.Type.LEGGINGS, 6);
						map.put(ArmorItem.Type.CHESTPLATE, 8);
						map.put(ArmorItem.Type.HELMET, 3);
					}),
					20,
					SoundEvents.ARMOR_EQUIP_DIAMOND,
					() -> Ingredient.of(ModItems.LUMEN_DUST),
					List.of(new ArmorMaterial.Layer(Endesium.id("luminous"))),
					1.0F,
					0.08F
			)
	);

	/** Null: deleted matter worn as plate. Void-tier defense, hungrier enchant cost. */
	public static final Holder<ArmorMaterial> NULL = Registry.registerForHolder(
			BuiltInRegistries.ARMOR_MATERIAL,
			Endesium.id("null"),
			new ArmorMaterial(
					Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
						map.put(ArmorItem.Type.BOOTS, 3);
						map.put(ArmorItem.Type.LEGGINGS, 6);
						map.put(ArmorItem.Type.CHESTPLATE, 8);
						map.put(ArmorItem.Type.HELMET, 3);
					}),
					18,
					SoundEvents.ARMOR_EQUIP_NETHERITE,
					() -> Ingredient.of(ModItems.UMBRAL_SHARD),
					List.of(new ArmorMaterial.Layer(Endesium.id("null"))),
					2.5F,
					0.1F
			)
	);

	private EndgearMaterials() {
	}

	/** Luminous tools: fast, glassy, highly enchantable. */
	public enum LuminousTool implements Tier {
		INSTANCE;

		@Override
		public int getUses() {
			return 1750;
		}

		@Override
		public float getSpeed() {
			return 9.5F;
		}

		@Override
		public float getAttackDamageBonus() {
			return 3.5F;
		}

		@Override
		public int getEnchantmentValue() {
			return 18;
		}

		@Override
		public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
			return net.minecraft.tags.BlockTags.INCORRECT_FOR_IRON_TOOL;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.of(ModItems.LUMEN_DUST);
		}
	}

	/** Ash tools: heat-tempered, modest durability, sets what it strikes alight. */
	public enum AshTool implements Tier {
		INSTANCE;

		@Override
		public int getUses() {
			return 1400;
		}

		@Override
		public float getSpeed() {
			return 7.5F;
		}

		@Override
		public float getAttackDamageBonus() {
			return 4.0F;
		}

		@Override
		public int getEnchantmentValue() {
			return 12;
		}

		@Override
		public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
			return net.minecraft.tags.BlockTags.INCORRECT_FOR_IRON_TOOL;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.of(ModItems.ASHEN_EMBER);
		}
	}

	/** Null tools: void-tier edge that deletes what it touches. */
	public enum NullTool implements Tier {
		INSTANCE;

		@Override
		public int getUses() {
			return 2031;
		}

		@Override
		public float getSpeed() {
			return 9.0F;
		}

		@Override
		public float getAttackDamageBonus() {
			return 4.5F;
		}

		@Override
		public int getEnchantmentValue() {
			return 15;
		}

		@Override
		public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
			return net.minecraft.tags.BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.of(ModItems.UMBRAL_SHARD);
		}
	}

	public static void register() {
		// Trigger static init: registers LUMINOUS and NULL armor materials.
		// Must be called during onInitialize before the registry freezes.
	}
}
