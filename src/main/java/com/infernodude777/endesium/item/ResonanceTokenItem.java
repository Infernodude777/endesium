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
 * A discovery item recovered from the Sunken Archive. It has no crafting use:
 * its value is knowledge. Using it whispers one of the recurring Endesium
 * motifs so the player can connect the symbol language across structures.
 */
public class ResonanceTokenItem extends Item {
	public ResonanceTokenItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			player.displayClientMessage(Component.literal(
					"The token hums. Three rings. The eye above the ring. The spire between them."), true);
			serverLevel.playSound(null, player.blockPosition(), ModSounds.RESONANCE_LENS_PULSE_LOW,
					SoundSource.PLAYERS, 0.4F, 0.9F);
			serverLevel.sendParticles(ModParticles.RESONANCE_PULSE, player.getX(), player.getY(0.8D), player.getZ(),
					4, 0.2D, 0.2D, 0.2D, 0.01D);
			player.getCooldowns().addCooldown(this, 30);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Proof that a mechanism was woken."));
		tooltip.add(Component.literal("It hums when pointed at the broken places."));
		tooltip.add(Component.literal("Used to craft the Echo Compass."));
	}
}
