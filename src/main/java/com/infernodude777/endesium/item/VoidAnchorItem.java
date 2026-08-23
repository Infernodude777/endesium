package com.infernodude777.endesium.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * A single-use End-only void anchor. Sneak-right-click binds a temporary point
 * for 60 seconds; right-click recalls the player to it and consumes the anchor.
 * It is a deliberate escape tool, not a cross-dimension fast-travel network.
 */
public final class VoidAnchorItem extends Item {
	private static final String TAG_BOUND = "bound";
	private static final String TAG_X = "x";
	private static final String TAG_Y = "y";
	private static final String TAG_Z = "z";
	private static final String TAG_BOUND_TIME = "bound_time";
	private static final long LIFETIME_TICKS = 1200L;


	public VoidAnchorItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResultHolder.sidedSuccess(stack, true);
		}
		if (level.dimension() != Level.END) {
			player.displayClientMessage(Component.literal("The void anchor only answers beneath the End sky")
					.withStyle(ChatFormatting.GRAY), true);
			return InteractionResultHolder.fail(stack);
		}
		ServerLevel server = (ServerLevel) level;
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (player.isShiftKeyDown()) {
			tag.putBoolean(TAG_BOUND, true);
			tag.putInt(TAG_X, player.getBlockX());
			tag.putInt(TAG_Y, player.getBlockY());
			tag.putInt(TAG_Z, player.getBlockZ());
			tag.putLong(TAG_BOUND_TIME, server.getGameTime());
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			player.displayClientMessage(Component.literal("Anchor bound").withStyle(ChatFormatting.DARK_AQUA), true);
			level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
					SoundSource.PLAYERS, 0.7F, 1.2F);
			return InteractionResultHolder.sidedSuccess(stack, true);
		}
		if (!tag.getBoolean(TAG_BOUND)) {
			player.displayClientMessage(Component.literal("Sneak-use to bind this anchor")
					.withStyle(ChatFormatting.GRAY), true);
			return InteractionResultHolder.sidedSuccess(stack, true);
		}
		long boundAt = tag.getLong(TAG_BOUND_TIME);
		// Anchors created before the timestamp field was introduced are treated
		// as expired rather than silently becoming permanent bookmarks.
		if (boundAt <= 0L || server.getGameTime() - boundAt > LIFETIME_TICKS) {
			tag.remove(TAG_BOUND);
			tag.remove(TAG_BOUND_TIME);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			player.displayClientMessage(Component.literal("The anchor's point has faded")
					.withStyle(ChatFormatting.GRAY), true);
			return InteractionResultHolder.sidedSuccess(stack, true);
		}
		BlockPos bound = new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
		if (server.isLoaded(bound)) {
			player.teleportTo(bound.getX() + 0.5D, bound.getY(), bound.getZ() + 0.5D);
			level.playSound(null, bound, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
			level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
		} else {
			player.displayClientMessage(Component.literal("Anchor point is unloaded")
					.withStyle(ChatFormatting.RED), true);
		}
		return InteractionResultHolder.sidedSuccess(stack, true);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("End-only. Sneak-use: bind your position for 60s")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Use: return to the bound point; consumed")
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Expired or unloaded points fade without recall")
				.withStyle(ChatFormatting.DARK_GRAY));
	}
}
