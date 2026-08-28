package com.infernodude777.endesium.mixin;

import com.infernodude777.endesium.dragon.DragonAssaultHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Crystal aegis for Endesium dragons: surviving pillar crystals reduce
 * incoming damage, and the window after the last pillar falls staggers the
 * dragon into taking bonus damage.
 *
 * <p>In 1.21.1 {@code hurt(DamageSource, float)} is declared on {@link Entity}
 * (not on LivingEntity), and every entity - including EnderDragon via its
 * part-hits - funnels damage through it. This gate therefore lives here.</p>
 */
@Mixin(Entity.class)
abstract class EntityMixin {
	@ModifyVariable(method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
			at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float endesium$aegisDragonDamage(float amount) {
		Entity self = (Entity) (Object) this;
		if (self instanceof EnderDragon
				&& self.level() instanceof ServerLevel server) {
			return DragonAssaultHandler.modifyDragonDamage(server, amount);
		}
		return amount;
	}
}
