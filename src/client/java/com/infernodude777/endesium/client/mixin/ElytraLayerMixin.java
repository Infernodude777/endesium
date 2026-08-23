package com.infernodude777.endesium.client.mixin;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lets the Resonant Wings render like an elytra. Vanilla's {@link ElytraLayer}
 * returns early unless the chest item is exactly {@link Items#ELYTRA}, and it
 * always uses the vanilla wing texture; this mixin accepts the Endesium elytra
 * and swaps in its own wing texture.
 */
@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin {
	@Shadow
	@Final
	private static ResourceLocation WINGS_LOCATION;

	@Unique
	private static final ResourceLocation RESONANT_WINGS_TEXTURE =
			Endesium.id("textures/entity/resonant_wings.png");

	@Unique
	private LivingEntity endesium$renderingEntity;

	/** Only the generic render overload carries the elytra logic; the bridge
	 * overload just delegates, so it must be excluded from every injection. */
	private static final String RENDER = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V";

	@Redirect(method = RENDER, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
	private ItemStack endesium$captureEntity(LivingEntity entity, EquipmentSlot slot) {
		endesium$renderingEntity = entity;
		return entity.getItemBySlot(slot);
	}

	@Redirect(method = RENDER, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	private boolean endesium$allowResonantWings(ItemStack stack, Item item) {
		return stack.is(item) || (item == Items.ELYTRA && stack.is(ModItems.RESONANT_WINGS));
	}

	// A static field GET redirect takes no handler arguments; the vanilla value
	// is read through the shadowed WINGS_LOCATION instead.
	@Redirect(method = RENDER, at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/renderer/entity/layers/ElytraLayer;WINGS_LOCATION:Lnet/minecraft/resources/ResourceLocation;"))
	private ResourceLocation endesium$swapWingTexture() {
		if (endesium$renderingEntity != null) {
			ItemStack chest = endesium$renderingEntity.getItemBySlot(EquipmentSlot.CHEST);
			if (chest.is(ModItems.RESONANT_WINGS)) {
				return RESONANT_WINGS_TEXTURE;
			}
		}
		return WINGS_LOCATION;
	}
}
