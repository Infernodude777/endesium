package com.infernodude777.endesium.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@link LivingEntity#jumping} - the server-side mirror of the Space
 * key that the client streams in every tick via
 * {@code ServerboundPlayerInputPacket}. Vanilla keeps the field protected and
 * offers no getter, so the companion dragon reads it through this accessor to
 * drive its charge shot.
 */
@Mixin(LivingEntity.class)
public interface LivingEntityJumpAccessor {
	@Accessor("jumping")
	boolean endesium$isJumping();
}
