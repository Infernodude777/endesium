package com.infernodude777.endesium.item;

import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The first post-Dragon discovery: an ancient seal recovered from the Resonant
 * Archive. It is the proof that the End woke, and the first key of the layer
 * beneath. Using it whispers the archive's line so the player can connect it to
 * the symbol language established by the ruins.
 */
public class ArchiveSigilItem extends Item {
	public ArchiveSigilItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			player.displayClientMessage(Component.literal(
					"The sigil hums with the memory of the awakening."), true);
			serverLevel.playSound(null, player.blockPosition(), ModSounds.RESONANCE_LENS_PULSE_HIGH,
					SoundSource.PLAYERS, 0.4F, 0.8F);
			serverLevel.sendParticles(ModParticles.RESONANCE_PULSE, player.getX(), player.getY(0.8D), player.getZ(),
					5, 0.2D, 0.2D, 0.2D, 0.01D);
			player.getCooldowns().addCooldown(this, 30);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("It remembers the moment the End woke."));
		tooltip.add(Component.literal("Proof of the Resonant Archive."));
	}
}
