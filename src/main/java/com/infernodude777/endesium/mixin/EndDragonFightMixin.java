package com.infernodude777.endesium.mixin;

import com.infernodude777.endesium.state.PostDragonEvents;
import com.infernodude777.endesium.state.PostDragonState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Detects the actual completion of the Ender Dragon fight. Vanilla calls
 * {@code setDragonKilled} exactly when a Dragon is killed; restoring a
 * previously-killed world goes through {@code scanState} and never calls this
 * method, so world loading can never false-trigger the transformation.
 * {@link PostDragonState#markDragonDefeated()} is idempotent, so a respawned
 * Dragon killed again cannot duplicate or reset the transformation.
 */
@Mixin(EndDragonFight.class)
abstract class EndDragonFightMixin {
	@Shadow
	@Final
	private ServerLevel level;

	@Inject(method = "setDragonKilled", at = @At("HEAD"))
	private void endesium$onDragonKilled(EnderDragon dragon, CallbackInfo ci) {
		if (level == null || level.isClientSide()) {
			return;
		}
		if (PostDragonState.get(level).markDragonDefeated()) {
			PostDragonEvents.fireTransformation(level, dragon.blockPosition());
		}
	}
}
