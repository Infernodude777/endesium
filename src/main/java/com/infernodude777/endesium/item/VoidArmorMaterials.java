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
import java.util.EnumMap; import java.util.List;
public final class VoidArmorMaterials {
    public static final Holder<ArmorMaterial> VOID = Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, Endesium.id("void"), new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), m->{m.put(ArmorItem.Type.BOOTS,3); m.put(ArmorItem.Type.LEGGINGS,6); m.put(ArmorItem.Type.CHESTPLATE,8); m.put(ArmorItem.Type.HELMET,3);}), 16, SoundEvents.ARMOR_EQUIP_NETHERITE, ()->Ingredient.of(ModItems.VOID_INGOT),
        // One layer with an empty suffix: Layer#texture already appends
        // "_layer_1"/"_layer_2", so passing those as the suffix resolved to
        // nonexistent void_layer_1_layer_1.png files (missing-texture checkers).
        List.of(new ArmorMaterial.Layer(Endesium.id("void"))), 3.0F, 0.12F));
    private VoidArmorMaterials(){}
}
