package com.infernodude777.endesium.item;

import com.infernodude777.endesium.Endesium;
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
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The crystallized engine-heart of the End Golem. While carried it hums with
 * borrowed armor; absorbed — permanently — it grants two hearts and a lasting
 * edge to every blow. Ten golems' worth of cores is the ceiling; the fight is
 * the only source, and the reward is forever.
 */
public final class GolemCoreItem extends Item {
	private static final double MAX_HEALTH_BONUS = 20.0D;
	private static final double MAX_DAMAGE_BONUS = 4.0D;
	private static final ResourceLocation HEALTH_ID = Endesium.id("golem_core_health");
	private static final ResourceLocation DAMAGE_ID = Endesium.id("golem_core_might");
	private static final AtomicLong SEQUENCE = new AtomicLong();

	public GolemCoreItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("\u00A7bCarried: Resistance I while in the End\u00A7r"));
		tooltip.add(Component.literal("\u00A76Use: absorb permanently \u00A7c+1 heart \u00A76and \u00A7b+0.25 attack damage\u00A7r"));
		tooltip.add(Component.literal("\u00A77(max +10 hearts, +4 damage)\u00A7r"));
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity,
			int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (level.isClientSide() || !(entity instanceof Player player)) return;
		if (!player.isAlive() || player.tickCount % 80 != 10) return;
		if (player.level().dimension() != Level.END) return;
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0, false, false, true));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		AttributeInstance health = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
		AttributeInstance damage = serverPlayer.getAttribute(Attributes.ATTACK_DAMAGE);
		double healthBonus = WardenSigilItem.sumBonus(health, HEALTH_ID.getPath());
		double damageBonus = WardenSigilItem.sumBonus(damage, DAMAGE_ID.getPath());
		boolean maxedHealth = healthBonus >= MAX_HEALTH_BONUS;
		boolean maxedDamage = damageBonus >= MAX_DAMAGE_BONUS;
		if (maxedHealth && maxedDamage) {
			serverPlayer.displayClientMessage(Component.literal(
					"\u00A77You are already more than the golem ever was."), true);
			return InteractionResultHolder.fail(stack);
		}
		long seq = SEQUENCE.incrementAndGet();
		if (health != null && !maxedHealth) {
			health.addPermanentModifier(new AttributeModifier(
					HEALTH_ID.withSuffix("_" + seq), 2.0D, AttributeModifier.Operation.ADD_VALUE));
		}
		if (damage != null && !maxedDamage) {
			damage.addPermanentModifier(new AttributeModifier(
					DAMAGE_ID.withSuffix("_" + seq), 0.25D, AttributeModifier.Operation.ADD_VALUE));
		}
		stack.shrink(1);
		serverPlayer.heal(6.0F);
		level.playSound(null, serverPlayer.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL,
				SoundSource.PLAYERS, 1.0F, 0.6F);
		serverPlayer.displayClientMessage(Component.literal(
				"\u00A75The core collapses into your chest. You feel heavier, and stronger."), true);

		// Resolve tracking: ten absorbed cores unlock the once-per-day save.
		com.infernodude777.endesium.state.AttunementState state =
				com.infernodude777.endesium.state.AttunementState.get(serverPlayer.server);
		if (state.recordCoreAbsorbed(serverPlayer)) {
			var resolve = serverPlayer.server.getAdvancements().get(Endesium.id("golems_resolve"));
			if (resolve != null) {
				serverPlayer.getAdvancements().award(resolve, "resolved");
			}
			serverPlayer.displayClientMessage(Component.literal(
					"\u00A75GOLEM'S RESOLVE \u2014 death itself will hesitate. Once a day."), false);
			serverPlayer.level().playSound(null, serverPlayer.blockPosition(),
					SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 0.8F);
		}

		var holder = serverPlayer.server.getAdvancements().get(Endesium.id("core_absorbed"));
		if (holder != null) {
			serverPlayer.getAdvancements().award(holder, "core_absorbed");
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
