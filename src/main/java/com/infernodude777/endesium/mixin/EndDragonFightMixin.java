package com.infernodude777.endesium.mixin;

import com.infernodude777.endesium.dragon.DragonCompanionSystem;
import com.infernodude777.endesium.state.PostDragonEvents;
import com.infernodude777.endesium.state.PostDragonState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;

import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

	/**
	 * When the fight scans for a dragon ({@code scanState} and
	 * {@code findOrCreateDragon}) it uses {@code ServerLevel.getDragons()},
	 * which returns every EnderDragon in the dimension - including the tameable
	 * companion. Left alone, a hatched Ember would be claimed as the boss:
	 * she'd show the fight's boss bar and be driven by the boss fight. Filter
	 * her (and any companion) out, so only real boss dragons count. This is
	 * safe because the only way to obtain a dragon egg is to first defeat the
	 * boss, so the world is already in its defeated state and the fight never
	 * needs to claim the pet.
	 */
	@Redirect(method = {"scanState", "findOrCreateDragon"},
			at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getDragons()Ljava/util/List;"))
	private List<EnderDragon> endesium$onlyRealDragons(ServerLevel level) {
		return level.getEntitiesOfClass(EnderDragon.class,
				new AABB(-3.0E7, -3.0E7, -3.0E7, 3.0E7, 3.0E7, 3.0E7),
				dragon -> !(dragon instanceof DragonCompanionSystem.CompanionDragon));
	}
}
