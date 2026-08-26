package com.infernodude777.endesium.registry;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.item.AshwalkerBootsItem;
import com.infernodude777.endesium.item.DragonWingsItem;
import com.infernodude777.endesium.item.GearArmorItem;
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
 *
 * <p>Per-piece armor powers (see GearAbilities):
 * <ul>
 *   <li>Luminous: Gleamsight (night vision), Radiant Aegis (foe glows), Lightspeed, Lumen Leap (jump).</li>
 *   <li>Ash: Ember Crown (fire res), Searing Plate (foe ignites), Magma Blood (regen while burning).</li>
 *   <li>Null: Erased Mind (levitation/darkness purge), Weightless (no fall damage), Null Step (step height).</li>
 * </ul>
 * Full sets amplify these with a capstone retaliation / sustain bonus.
 */
public final class ModEndgear {
	private static Item register(String name, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, Endesium.id(name), item);
	}

	private static Item.Properties gearProps() {
		return new Item.Properties().stacksTo(1).rarity(Rarity.RARE);
	}

	private static Item.Properties gearProps(int durability) {
		return new Item.Properties().stacksTo(1).durability(durability).rarity(Rarity.RARE);
	}

	// --- Luminous armor ---
	public static final Item LUMINOUS_HELMET = register("luminous_helmet",
			new GearArmorItem(EndgearMaterials.LUMINOUS, ArmorItem.Type.HELMET, gearProps(480),
					"Gleamsight: Night Vision while worn"));
	public static final Item LUMINOUS_CHESTPLATE = register("luminous_chestplate",
			new GearArmorItem(EndgearMaterials.LUMINOUS, ArmorItem.Type.CHESTPLATE, gearProps(640),
					"Radiant Aegis: attackers get GLOWING 8s",
					"Full set: attackers also get DARKNESS + 3 magic burn-back (Prism Ward)"));
	public static final Item LUMINOUS_LEGGINGS = register("luminous_leggings",
			new GearArmorItem(EndgearMaterials.LUMINOUS, ArmorItem.Type.LEGGINGS, gearProps(600),
					"Lightspeed: Speed I while worn (Speed II with full set)"));
	public static final Item LUMINOUS_BOOTS = register("luminous_boots",
			new GearArmorItem(EndgearMaterials.LUMINOUS, ArmorItem.Type.BOOTS, gearProps(520),
					"Lumen Leap: Jump Boost II while worn"));

	// --- Ash armor ---
	public static final Item ASH_HELMET = register("ash_helmet",
			new GearArmorItem(AshenArmorMaterials.ASHEN, ArmorItem.Type.HELMET, gearProps(520),
					"Ember Crown: Fire Resistance while worn"));
	public static final Item ASH_CHESTPLATE = register("ash_chestplate",
			new GearArmorItem(AshenArmorMaterials.ASHEN, ArmorItem.Type.CHESTPLATE, gearProps(680),
					"Searing Plate: attackers that hit you catch fire 4s",
					"When burning/in lava: Strength I",
					"Full ash set: permanent Strength I (Volcanic Heart)"));
	public static final Item ASH_LEGGINGS = register("ash_leggings",
			new GearArmorItem(AshenArmorMaterials.ASHEN, ArmorItem.Type.LEGGINGS, gearProps(640),
					"Magma Blood: Regeneration I while burning or in lava"));
	// The Ashwalker Boots remain the ash line's boots piece (see AshwalkerBootsItem).

	// --- Null armor ---
	public static final Item NULL_HELMET = register("null_helmet",
			new GearArmorItem(EndgearMaterials.NULL, ArmorItem.Type.HELMET, gearProps(680),
					"Erased Mind: removes Levitation, Darkness, and Nausea",
					"   while worn (the void does not move or cloud you)"));
	public static final Item NULL_CHESTPLATE = register("null_chestplate",
			new GearArmorItem(EndgearMaterials.NULL, ArmorItem.Type.CHESTPLATE, gearProps(880),
					"Erased Wound: when hit, gain Absorption 5s"));
	public static final Item NULL_LEGGINGS = register("null_leggings",
			new GearArmorItem(EndgearMaterials.NULL, ArmorItem.Type.LEGGINGS, gearProps(820),
					"Weightless: no fall damage while worn"));
	public static final Item NULL_BOOTS = register("null_boots",
			new GearArmorItem(EndgearMaterials.NULL, ArmorItem.Type.BOOTS, gearProps(720),
					"Null Step: walk up full blocks without jumping",
					"Full set: 25 percent incoming projectiles simply vanish (Void Body)"));

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

	public static void register() {
		// Trigger static init: registers all 27 gear items + Dragon Wings.
		// Must be called during onInitialize before the registry freezes.
	}

	private ModEndgear() {
	}
}
