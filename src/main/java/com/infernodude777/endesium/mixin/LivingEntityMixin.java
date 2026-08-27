package com.infernodude777.endesium.mixin;

import com.infernodude777.endesium.dragon.DragonAssaultHandler;
import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Resonant Elytra movement layer.
 *
 * <p><b>Void Grace:</b> while wearing the wings, knockback strength is
 * reduced by 35% — a modest improvement to aerial control.</p>
 *
 * <p><b>Resonant Cruise:</b> while gliding with the wings, the elytra is
 * gently accelerated each tick up to a soft speed cap of roughly twice the
 * vanilla top glide speed, so crossing the wastes takes half the time without
 * becoming uncontrollable or tripping server movement checks.</p>
 *
 * <p><b>Resonant Cushioning:</b> fall damage taken while wearing the wings is
 * halved, so a rough landing after a long glide rarely kills.</p>
 *
 * <p>Flight permission for the wings is handled via {@code EntityElytraEvents.CUSTOM}
 * in {@link com.infernodude777.endesium.Endesium}. The cruise injection runs on
 * both sides on purpose: elytra motion for players is simulated client-side,
 * and mirroring it server-side keeps the two simulations in agreement.</p>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	/** Per-tick velocity multiplier while gliding (compounds against drag). */
	@Unique
	private static final double CRUISE_GAIN = 1.03D;
	/** Soft cap: 2.6 blocks/tick ≈ 52 m/s ≈ twice vanilla's fast glide. */
	@Unique
	private static final double CRUISE_SPEED_CAP_SQR = 2.6D * 2.6D;

	@ModifyVariable(method = "knockback", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double endesium$voidGrace(double strength) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player player
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_WINGS)) {
			return strength * 0.65D;
		}
		return strength;
	}

	@Inject(method = "travel(Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"))
	private void endesium$resonantCruise(Vec3 travelVector, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (!self.isFallFlying()
				|| !self.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_WINGS)) {
			return;
		}
		Vec3 velocity = self.getDeltaMovement();
		double speedSqr = velocity.lengthSqr();
		if (!Double.isFinite(speedSqr) || speedSqr >= CRUISE_SPEED_CAP_SQR) {
			return; // at the soft cap (or NaN-safe): vanilla physics take over
		}
		self.setDeltaMovement(velocity.scale(CRUISE_GAIN));
	}

	@Inject(method = "calculateFallDamage(FF)I", at = @At("RETURN"), cancellable = true)
	private void endesium$resonantCushioning(float fallDistance, float multiplier,
			CallbackInfoReturnable<Integer> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_WINGS)) {
			cir.setReturnValue(Math.max(0, cir.getReturnValue() / 2));
		}
	}

	/**
	 * Crystal aegis for Endesium dragons: surviving pillar crystals reduce
	 * incoming damage, and the window after the last pillar falls staggers the
	 * dragon into taking bonus damage. EnderDragon does not override
	 * {@code hurt}, so the LivingEntity hook is the single damage gate.
	 */
	@ModifyVariable(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
			at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private float endesium$aegisDragonDamage(float amount) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof EnderDragon
				&& self.level() instanceof ServerLevel server) {
			return DragonAssaultHandler.modifyDragonDamage(server, amount);
		}
		return amount;
	}
}
