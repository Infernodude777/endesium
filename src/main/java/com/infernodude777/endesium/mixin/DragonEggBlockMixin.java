package com.infernodude777.endesium.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The dragon egg is an Endesium boss altar, not a broken vanilla block. Where
 * vanilla lets the egg teleport away (right-click or punch), fall out and drop
 * as an item (torch under it + break its support), or be shunted by a piston,
 * this pins it in place so it sits on the exit portal and cannot be moved by
 * any of those routes - it is stationary until it summons the End Golem.
 */
@Mixin(DragonEggBlock.class)
abstract class DragonEggBlockMixin {
	/**
	 * No more teleport-on-right-click: the egg can't be "collected" that way.
	 */
	@Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
	private void endesium$noUseTeleport(BlockState state, Level level, BlockPos pos,
			Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
		cir.cancel();
	}

	/**
	 * No more jump-on-punch: cancel the original attack outright (it previously
	 * ran on, teleporting the egg). The egg never breaks and never flings away,
	 * so no new countdown can start from a relocated egg.
	 */
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void endesium$noAttackTeleport(BlockState state, Level level, BlockPos pos,
			Player player, CallbackInfo ci) {
		ci.cancel();
	}

	/**
	 * A piston cannot push the egg; it is anchored to its altar. (Piston code
	 * queries {@code BlockStateBase#getPistonPushReaction()}, and BLOCK means
	 * "cannot be moved, cannot be destroyed by the piston".)
	 */
	public PushReaction getPistonPushReaction() {
		return PushReaction.BLOCK;
	}

	/**
	 * Never start a falling block: breaking its support (torch trick) must not
	 * drop the egg as an item - it stays anchored until the summon completes.
	 */
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
		// Intentionally empty: FallingBlock would schedule a fall tick here.
	}

	public BlockState updateShape(BlockState state, net.minecraft.core.Direction dir,
			BlockState neighbor, Level level, BlockPos pos, BlockPos neighborPos) {
		return state;
	}

	public void tick(BlockState state, net.minecraft.server.level.ServerLevel level,
			BlockPos pos, net.minecraft.util.RandomSource random) {
		// Intentionally empty: the fall tick is cancelled so a torch under the
		// egg can never turn it into a falling block / item.
	}

	public void falling(net.minecraft.world.entity.item.FallingBlockEntity entity) {
		// Also neutralize the fall-motion hook for completeness.
	}

	/**
	 * The egg cannot be mined while it is an altar: survival digging can never
	 * collect or reposition it, so it truly stays stationary until the summon.
	 * (A -1 destroy progress is the bedrock-style "unbreakable" signal.)
	 */
	public float getDestroyProgress(BlockState state, Player player, Level level, BlockPos pos) {
		return -1.0F;
	}
}