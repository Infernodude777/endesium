package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.item.AshwalkerBootsItem;
import com.infernodude777.endesium.item.DragonWingsItem;
import com.infernodude777.endesium.item.AshenArmorMaterials;
import com.infernodude777.endesium.item.EndgearMaterials;
import com.infernodude777.endesium.item.EndgearTools;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Registry for the Luminous, Ash, and Null gear lines plus the Dragon Wings.
 * Every texture follows the Void set's model: same silhouettes, recolored
 * palettes (luminous gold-cyan, ash gray-ember, null violet-void).
 */
public final class ModEndgear {
	private static Item register(String name, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, Endesium.id(name), item);
	}

	private static Item.Properties gearProps() {
		return new Item.Properties().stacksTo(1).rarity(Rarity.RARE);
	}

	// --- Luminous armor ---
	public static final Item LUMINOUS_HELMET = register("luminous_helmet",
			new ArmorItem(EndgearMaterials.LUMINOUS, ArmorItem.Type.HELMET, gearProps()));
	public static final Item LUMINOUS_CHESTPLATE = register("luminous_chestplate",
			new ArmorItem(EndgearMaterials.LUMINOUS, ArmorItem.Type.CHESTPLATE, gearProps()));
	public static final Item LUMINOUS_LEGGINGS = register("luminous_leggings",
			new ArmorItem(EndgearMaterials.LUMINOUS, ArmorItem.Type.LEGGINGS, gearProps()));
	public static final Item LUMINOUS_BOOTS = register("luminous_boots",
			new ArmorItem(EndgearMaterials.LUMINOUS, ArmorItem.Type.BOOTS, gearProps()));

	// --- Ash armor ---
	public static final Item ASH_HELMET = register("ash_helmet",
			new ArmorItem(AshenArmorMaterials.ASHEN, ArmorItem.Type.HELMET, gearProps()));
	public static final Item ASH_CHESTPLATE = register("ash_chestplate",
			new ArmorItem(AshenArmorMaterials.ASHEN, ArmorItem.Type.CHESTPLATE, gearProps()));
	public static final Item ASH_LEGGINGS = register("ash_leggings",
			new ArmorItem(AshenArmorMaterials.ASHEN, ArmorItem.Type.LEGGINGS, gearProps()));
	// The Ashwalker Boots remain the ash line's boots piece.

	// --- Null armor ---
	public static final Item NULL_HELMET = register("null_helmet",
			new ArmorItem(EndgearMaterials.NULL, ArmorItem.Type.HELMET, gearProps()));
	public static final Item NULL_CHESTPLATE = register("null_chestplate",
			new ArmorItem(EndgearMaterials.NULL, ArmorItem.Type.CHESTPLATE, gearProps()));
	public static final Item NULL_LEGGINGS = register("null_leggings",
			new ArmorItem(EndgearMaterials.NULL, ArmorItem.Type.LEGGINGS, gearProps()));
	public static final Item NULL_BOOTS = register("null_boots",
			new ArmorItem(EndgearMaterials.NULL, ArmorItem.Type.BOOTS, gearProps()));

	// --- Luminous tools ---
	public static final Item LUMINOUS_SWORD = register("luminous_sword",
			new EndgearTools.LuminousSword(EndgearMaterials.LuminousTool.INSTANCE,
					gearProps().attributes(SwordItem.createAttributes(EndgearMaterials.LuminousTool.INSTANCE, 3, -2.4F))));
	public static final Item LUMINOUS_PICKAXE = register("luminous_pickaxe",
			new EndgearTools.LuminousPickaxe(EndgearMaterials.LuminousTool.INSTANCE,
					gearProps().attributes(PickaxeItem.createAttributes(EndgearMaterials.LuminousTool.INSTANCE, 1.0F, -2.8F))));
	public static final Item LUMINOUS_AXE = register("luminous_axe",
			new EndgearTools.LuminousAxe(EndgearMaterials.LuminousTool.INSTANCE,
					gearProps().attributes(AxeItem.createAttributes(EndgearMaterials.LuminousTool.INSTANCE, 5.0F, -3.0F))));
	public static final Item LUMINOUS_SHOVEL = register("luminous_shovel",
			new EndgearTools.LuminousShovel(EndgearMaterials.LuminousTool.INSTANCE,
					gearProps().attributes(ShovelItem.createAttributes(EndgearMaterials.LuminousTool.INSTANCE, 1.5F, -3.0F))));
	public static final Item LUMINOUS_HOE = register("luminous_hoe",
			new EndgearTools.LuminousHoe(EndgearMaterials.LuminousTool.INSTANCE,
					gearProps().attributes(HoeItem.createAttributes(EndgearMaterials.LuminousTool.INSTANCE, -3.0F, 0.0F))));

	// --- Ash tools ---
	public static final Item ASH_SWORD = register("ash_sword",
			new EndgearTools.AshSword(EndgearMaterials.AshTool.INSTANCE,
					gearProps().attributes(SwordItem.createAttributes(EndgearMaterials.AshTool.INSTANCE, 3, -2.4F))));
	public static final Item ASH_PICKAXE = register("ash_pickaxe",
			new EndgearTools.AshPickaxe(EndgearMaterials.AshTool.INSTANCE,
					gearProps().attributes(PickaxeItem.createAttributes(EndgearMaterials.AshTool.INSTANCE, 1.0F, -2.8F))));
	public static final Item ASH_AXE = register("ash_axe",
			new EndgearTools.AshAxe(EndgearMaterials.AshTool.INSTANCE,
					gearProps().attributes(AxeItem.createAttributes(EndgearMaterials.AshTool.INSTANCE, 5.0F, -3.0F))));
	public static final Item ASH_SHOVEL = register("ash_shovel",
			new EndgearTools.AshShovel(EndgearMaterials.AshTool.INSTANCE,
					gearProps().attributes(ShovelItem.createAttributes(EndgearMaterials.AshTool.INSTANCE, 1.5F, -3.0F))));
	public static final Item ASH_HOE = register("ash_hoe",
			new EndgearTools.AshHoe(EndgearMaterials.AshTool.INSTANCE,
					gearProps().attributes(HoeItem.createAttributes(EndgearMaterials.AshTool.INSTANCE, -3.0F, 0.0F))));

	// --- Null tools ---
	public static final Item NULL_SWORD = register("null_sword",
			new EndgearTools.NullSword(EndgearMaterials.NullTool.INSTANCE,
					gearProps().attributes(SwordItem.createAttributes(EndgearMaterials.NullTool.INSTANCE, 3, -2.4F))));
	public static final Item NULL_PICKAXE = register("null_pickaxe",
			new EndgearTools.NullPickaxe(EndgearMaterials.NullTool.INSTANCE,
					gearProps().attributes(PickaxeItem.createAttributes(EndgearMaterials.NullTool.INSTANCE, 1.0F, -2.8F))));
	public static final Item NULL_AXE = register("null_axe",
			new EndgearTools.NullAxe(EndgearMaterials.NullTool.INSTANCE,
					gearProps().attributes(AxeItem.createAttributes(EndgearMaterials.NullTool.INSTANCE, 5.0F, -3.0F))));
	public static final Item NULL_SHOVEL = register("null_shovel",
			new EndgearTools.NullShovel(EndgearMaterials.NullTool.INSTANCE,
					gearProps().attributes(ShovelItem.createAttributes(EndgearMaterials.NullTool.INSTANCE, 1.5F, -3.0F))));
	public static final Item NULL_HOE = register("null_hoe",
			new EndgearTools.NullHoe(EndgearMaterials.NullTool.INSTANCE,
					gearProps().attributes(HoeItem.createAttributes(EndgearMaterials.NullTool.INSTANCE, -3.0F, 0.0F))));

	// --- Dragon Wings: elytra rules (unbreaking/mending only), iron
	// chestplate defense, permanent slowness while worn (see GearAbilities).
	public static final Item DRAGON_WINGS = register("dragon_wings",
			new DragonWingsItem(gearProps().durability(432)
					.attributes(ItemAttributeModifiers.builder()
							.add(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, new net.minecraft.world.entity.ai.attributes.AttributeModifier(
									Endesium.id("dragon_wings_armor"), 6.0D,
									net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.CHEST)
							.build())));

	private ModEndgear() {
	}
}
