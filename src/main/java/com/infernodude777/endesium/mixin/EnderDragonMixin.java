package com.infernodude777.endesium.mixin;

import com.infernodude777.endesium.dragon.DragonFightController;
import com.infernodude777.endesium.dragon.DragonLoot;
import com.infernodude777.endesium.state.PostDragonState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Endesium's Dragon combat layer. The four-phase fight controller lives in
 * {@link DragonFightController}; this mixin stays a thin delegate that also
 * handles the scale fix (vanilla hard-forces the Dragon's scale to 1.0) and
 * the code-driven death drops.
 */
@Mixin(EnderDragon.class)
abstract class EnderDragonMixin {
	@Unique
	private DragonFightController.State endesium$fightState;

	@Unique
	private DragonFightController.State endesium$state() {
		if (endesium$fightState == null) {
			endesium$fightState = new DragonFightController.State();
		}
		return endesium$fightState;
	}

	/** Allow the SCALE attribute to actually resize the Dragon. */
	@Inject(method = "sanitizeScale", at = @At("HEAD"), cancellable = true)
	private void endesium$respectScale(float scale, CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(scale);
	}

	/**
	 * Fires at the start of the death animation to grant the Dragon's material
	 * drops. The world-level transformation is intentionally not marked here:
	 * {@link com.infernodude777.endesium.mixin.EndDragonFightMixin} owns the
	 * later vanilla {@code setDragonKilled} transition and its announcement.
	 */
	@Inject(method = "tickDeath", at = @At("HEAD"))
	private void endesium$onDeathStarted(CallbackInfo ci) {
		EnderDragon dragon = (EnderDragon) (Object) this;
		if (dragon.level().isClientSide() || dragon.dragonDeathTime != 0) return;
		ServerLevel level = (ServerLevel) dragon.level();
		PostDragonState state = PostDragonState.get(level);
		endesium$state().clearZones();
		// Do not activate the world state here. Vanilla calls
		// EndDragonFight.setDragonKilled after the death hook; that mixin is the
		// single authoritative transition point and fires the announcement.
		// Marking here first made the later idempotence check suppress the event.
		DragonLoot.grantDrops(dragon, level, !state.isDragonDefeated());
	}

	@Inject(method = "aiStep", at = @At("HEAD"))
	private void endesium$aiStep(CallbackInfo ci) {
		EnderDragon dragon = (EnderDragon) (Object) this;
		if (dragon.level().isClientSide()) return;
		DragonFightController.tick(dragon, (ServerLevel) dragon.level(), endesium$state());
	}

	/**
	 * The fight state is a runtime-only mixin field; persisting it keeps a
	 * server restart from resetting the phase, replaying the Final Roar, or
	 * re-applying the transformation buff (which would heal the Dragon).
	 * 1.21.1's EnderDragon still uses the single-argument save/load
	 * signatures; the data-component variants arrived in later versions.
	 */
	@Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void endesium$saveFightState(CompoundTag tag, CallbackInfo ci) {
		endesium$state().save(tag);
	}

	@Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
	private void endesium$loadFightState(CompoundTag tag, CallbackInfo ci) {
		endesium$state().load(tag);
	}
}
