package com.infernodude777.endesium.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The dragon's main body, wings, head and tail are separate
 * {@link EnderDragonPart} entities, and a right-click on a large dragon almost
 * always lands on one of them. A part is a plain {@link Entity} with no
 * interaction logic, so without this the click vanishes and Ember can never be
 * mounted. We inject into {@code Entity.interact} - not into the part (which
 * inherits it and would risk weaving into every entity) - and forward only
 * when the target is actually a dragon part. {@code Mob} overrides
 * {@code interact} as final, so boss dragons and all mobs are untouched, and
 * any other non-mob entity falls straight through to its own behaviour.
 */
@Mixin(Entity.class)
abstract class EnderDragonPartMixin {
	@Inject(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
			at = @At("HEAD"), cancellable = true)
	private void endesium$forwardDragonPartInteract(Player player, InteractionHand hand,
			CallbackInfoReturnable<InteractionResult> cir) {
		if ((Object) this instanceof EnderDragonPart part && part.parentMob != null) {
			cir.setReturnValue(part.parentMob.interact(player, hand));
		}
	}
}