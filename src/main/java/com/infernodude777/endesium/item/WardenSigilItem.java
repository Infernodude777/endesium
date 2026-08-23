package com.infernodude777.endesium.item;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A warden's bonded sigil, keyed to the region it guarded. Carrying one in
 * the End keeps a slow regeneration running; consuming it — "attuning" —
 * permanently binds two extra hearts to the attuner's soul. Permanent power
 * is the point: every sigil is a decision you keep.
 */
public final class WardenSigilItem extends Item {
	/** Hard cap on attuned bonus hearts so the buff stays meaningful, not infinite. */
	private static final double MAX_BONUS_HEALTH = 20.0D;
	private static final ResourceLocation MODIFIER_ID = Endesium.id("warden_attunement");
	private static final String REGION_KEY = "endesium:region";

	public WardenSigilItem(Properties properties) {
		super(properties);
	}

	private static int regionOf(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null ? data.copyTag().getInt(REGION_KEY) : 0;
	}

	public static void writeRegion(ItemStack stack, int region) {
		var tag = new net.minecraft.nbt.CompoundTag();
		tag.putInt(REGION_KEY, region);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("\u00A7bCarried: slow regeneration while in the End\u00A7r"));
		tooltip.add(Component.literal("\u00A76Use: permanently attune \u00A7c+1 heart\u00A76 (max +10)\u00A7r"));
		int region = regionOf(stack);
		String[] names = {"End Wastes", "Shattered Highlands", "Void Marshes", "Chorus Wilds",
				"Luminous Groves", "Ashen Expanse", "Crystal Barrens", "Void Skirts",
				"Void Crown", "Umbral Reach"};
		tooltip.add(Component.literal("Attuned to the " + names[region % names.length] + "."));
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity,
			int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (level.isClientSide() || !(entity instanceof Player player)) return;
		if (!player.isAlive() || player.tickCount % 80 != 0) return;
		if (player.level().dimension() != Level.END) return;
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, false, false, true));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		AttributeInstance health = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
		double currentBonus = sumBonus(health);
		if (currentBonus >= MAX_BONUS_HEALTH) {
			serverPlayer.displayClientMessage(Component.translatable(
					"endesium.sigil.maxed").withStyle(net.minecraft.ChatFormatting.GRAY), true);
			return InteractionResultHolder.fail(stack);
		}
		int region = regionOf(stack);
		double amount = Math.min(2.0D, MAX_BONUS_HEALTH - currentBonus);
		if (health != null) {
			health.addPermanentModifier(new AttributeModifier(
					MODIFIER_ID.withSuffix("_" + UUID_SEQUENCE.incrementAndGet()),
					amount, AttributeModifier.Operation.ADD_VALUE));
		}
		stack.shrink(1);
		serverPlayer.heal(4.0F);
		level.playSound(null, serverPlayer.blockPosition(), SoundEvents.TOTEM_USE,
				SoundSource.PLAYERS, 0.8F, 1.2F);
		serverPlayer.displayClientMessage(Component.translatable(
				"endesium.sigil.attuned", (int) (amount / 2.0D)).withStyle(net.minecraft.ChatFormatting.GOLD), true);
		var holder = serverPlayer.server.getAdvancements().get(Endesium.id("sigil_attuned"));
		if (holder != null) {
			serverPlayer.getAdvancements().award(holder, "sigil_used");
		}

		// Ascension tracking: all ten regions attuned unlocks the aura.
		com.infernodude777.endesium.state.AttunementState state =
				com.infernodude777.endesium.state.AttunementState.get(serverPlayer.server);
		if (state.markRegionAttuned(serverPlayer, Math.max(0, region))) {
			var ascend = serverPlayer.server.getAdvancements().get(Endesium.id("warden_ascendant"));
			if (ascend != null) {
				serverPlayer.getAdvancements().award(ascend, "ascended");
			}
			serverPlayer.displayClientMessage(Component.translatable(
					"endesium.sigil.ascendant").withStyle(net.minecraft.ChatFormatting.AQUA), false);
			serverPlayer.level().playSound(null, serverPlayer.blockPosition(),
					SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.2F);
			serverPlayer.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
					serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(),
					60, 1.0D, 1.5D, 1.0D, 0.06D);
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	static double sumBonus(AttributeInstance attribute, String idPrefix) {
		if (attribute == null) return 0.0D;
		double total = 0.0D;
		for (AttributeModifier modifier : attribute.getModifiers()) {
			if (modifier.id().getNamespace().equals(Endesium.MOD_ID)
					&& modifier.id().getPath().startsWith(idPrefix)) {
				total += modifier.amount();
			}
		}
		return total;
	}

	private static double sumBonus(AttributeInstance attribute) {
		return sumBonus(attribute, MODIFIER_ID.getPath());
	}

	/** Sequential suffixes keep each attunement a distinct permanent modifier. */
	private static final java.util.concurrent.atomic.AtomicLong UUID_SEQUENCE =
			new java.util.concurrent.atomic.AtomicLong();
}
