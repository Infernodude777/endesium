package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A rare Endesium mobility item: a short-range, safe teleport. The destination
 * is clamped to 12 blocks, must be passable with solid ground below, and the
 * item carries a 10-second cooldown — a controlled reposition, never an
 * infinite teleport exploit.
 *
 * <p>The pearl is a boss relic: it drops only from the End Golem, carries
 * finite durability that each teleport consumes, and accepts Unbreaking and
 * Mending so a kept pearl grows with its owner instead of running out.</p>
 */
public final class VoidPearlItem extends Item {
	private static final double MAX_RANGE = 12.0D;
	private static final int COOLDOWN_TICKS = 200;
	private static final int DURABILITY_PER_TELEPORT = 1;

	public VoidPearlItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}

	@Override
	public int getEnchantmentValue() {
		return 15;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}

		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(MAX_RANGE + 4.0D)),
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		double distance = hit.getType() == HitResult.Type.BLOCK
				? Math.max(2.0D, hit.getLocation().distanceTo(eye) - 1.0D)
				: MAX_RANGE;
		distance = Math.min(distance, MAX_RANGE);

		Vec3 destination = eye.add(look.scale(distance));
		BlockPos destinationPos = BlockPos.containing(destination);
		if (!validDestination(level, destinationPos)) {
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
		}

		net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) level;
		serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(),
				24, 0.5D, 1.0D, 0.5D, 0.02D);
		player.teleportTo(destination.x, destination.y, destination.z);
		player.resetFallDistance();
		serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(),
				24, 0.5D, 1.0D, 0.5D, 0.02D);
		serverLevel.playSound(null, destinationPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
		player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

		EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
		stack.hurtAndBreak(DURABILITY_PER_TELEPORT, player, slot);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	private static boolean validDestination(Level level, BlockPos pos) {
		if (!level.getBlockState(pos).isAir() && !level.getBlockState(pos).is(Blocks.SHORT_GRASS)) {
			return false;
		}
		if (!level.getBlockState(pos.above()).isAir()) {
			return false;
		}
		return level.getBlockState(pos.below()).isSolidRender(level, pos.below());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Use: safe teleport up to 12 blocks (ground-checked)").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("10s cooldown; wears down; accepts Unbreaking and Mending").withStyle(ChatFormatting.GRAY));
	}
}