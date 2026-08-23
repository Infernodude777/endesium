package com.infernodude777.endesium.item;
import net.minecraft.world.item.TooltipFlag;

import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Void Axe: a strong combat axe that grants a forward dash on right-click
 * when the wearer has Void Boots equipped. The dash launches the player in
 * their look direction with a small upward boost and a 1-second cooldown.
 */
public final class VoidAxeItem extends AxeItem {
	private static final int DASH_COOLDOWN_TICKS = 20;
	private static final double DASH_HORIZONTAL = 1.35D;
	private static final double DASH_VERTICAL = 0.18D;

	public VoidAxeItem(Tier tier, Properties properties) {
		super(tier, properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
			return InteractionResultHolder.pass(stack);
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResultHolder.pass(stack);
		}
		if (!serverPlayer.getItemBySlot(EquipmentSlot.FEET).is(com.infernodude777.endesium.registry.ModItems.VOID_BOOTS)) {
			serverPlayer.displayClientMessage(
					Component.literal("The Void Boots must answer before the axe can dash")
							.withStyle(ChatFormatting.DARK_GRAY), true);
			return InteractionResultHolder.fail(stack);
		}
		if (serverPlayer.getCooldowns().isOnCooldown(this)) {
			return InteractionResultHolder.fail(stack);
		}

		Vec3 look = serverPlayer.getLookAngle();
		Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
		if (horizontal.lengthSqr() < 0.0001D) {
			return InteractionResultHolder.fail(stack);
		}
		horizontal = horizontal.normalize();
		// Anti-wall-clip: check 2-block ahead is not solid
		BlockPos ahead = BlockPos.containing(serverPlayer.position().add(horizontal.scale(2.0D)).add(0, 1, 0));
		if (!level.getBlockState(ahead).isAir() && level.getBlockState(ahead).isSolidRender(level, ahead)) {
			serverPlayer.displayClientMessage(Component.literal("Not enough space to dash").withStyle(ChatFormatting.GRAY), true);
			return InteractionResultHolder.fail(stack);
		}
		serverPlayer.setDeltaMovement(
				serverPlayer.getDeltaMovement().add(horizontal.scale(DASH_HORIZONTAL)).add(0.0D, DASH_VERTICAL, 0.0D));
		serverPlayer.hurtMarked = true;
		serverPlayer.getCooldowns().addCooldown(this, DASH_COOLDOWN_TICKS);

		ServerLevel sLevel = serverPlayer.serverLevel();
		sLevel.playSound(null, BlockPos.containing(serverPlayer.position()),
				ModSounds.RESONANCE_STRIKE, SoundSource.PLAYERS, 0.8F, 1.2F);
		sLevel.sendParticles(ModParticles.VOID_STALKER_TRACE,
				serverPlayer.getX(), serverPlayer.getY() + 0.35D, serverPlayer.getZ(),
				10, 0.35D, 0.15D, 0.35D, 0.04D);

		return InteractionResultHolder.success(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Requires Void Boots: right-click to dash forward").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("1s cooldown").withStyle(ChatFormatting.GRAY));
	}
}