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
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

/**
 * The Ashen armor material, built for the Ashwalker Boots. Slightly lighter
 * than the Void set but with strong fire affinity, so it pairs naturally with
 * the volcano loot that produces it.
 */
public final class AshenArmorMaterials {
	public static final Holder<ArmorMaterial> ASHEN = Registry.registerForHolder(
			BuiltInRegistries.ARMOR_MATERIAL,
			Endesium.id("ashen"),
			new ArmorMaterial(
					Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
						map.put(ArmorItem.Type.BOOTS, 3);
						map.put(ArmorItem.Type.LEGGINGS, 5);
						map.put(ArmorItem.Type.CHESTPLATE, 7);
						map.put(ArmorItem.Type.HELMET, 2);
					}),
					14,
					SoundEvents.ARMOR_EQUIP_LEATHER,
					() -> Ingredient.of(ModItems.ASHEN_EMBER),
					// One layer with an empty suffix: Layer#texture already
					// appends "_layer_1"/"_layer_2", so passing those as the
					// suffix resolved to nonexistent ashen_layer_1_layer_1.png.
					List.of(new ArmorMaterial.Layer(Endesium.id("ashen"))),
					0.0F,
					0.0F
			)
	);

	private AshenArmorMaterials() {
	}
}
