package com.infernodude777.endesium.item;

import com.infernodude777.endesium.Endesium;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * A carved effigy of the End Golem. Using it in the End re-awakens the
 * colossus nearby if no golem already walks. The only renewable road to
 * more Golem Cores, priced at the cores it cost to carve.
 */
public final class GolemEffigyItem extends Item {
	public GolemEffigyItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("\u00A75Use in the End: wake a new End Golem\u00A7r"));
		tooltip.add(Component.literal("\u00A77Fails if a golem already walks.\u00A7r"));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		ServerLevel serverLevel = serverPlayer.serverLevel();
		if (serverLevel.dimension() != Level.END) {
			serverPlayer.displayClientMessage(Component.translatable(
					"endesium.effigy.wrong_dimension").withStyle(net.minecraft.ChatFormatting.GRAY), true);
			return InteractionResultHolder.fail(stack);
		}
		// One golem at a time, across the whole dimension.
		double radius = 512.0D;
		var existing = serverLevel.getEntitiesOfClass(
				com.infernodude777.endesium.entity.EndGolemEntity.class,
				player.getBoundingBox().inflate(radius),
				g -> g.isAlive());
		if (!existing.isEmpty()) {
			serverPlayer.displayClientMessage(Component.translatable(
					"endesium.effigy.golem_exists").withStyle(net.minecraft.ChatFormatting.GRAY), true);
			return InteractionResultHolder.fail(stack);
		}
		var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(Endesium.id("end_golem"));
		if (!(type instanceof EntityType<?> entityType)) return InteractionResultHolder.fail(stack);
		var golem = entityType.create(serverLevel);
		if (!(golem instanceof net.minecraft.world.entity.Mob mob)) return InteractionResultHolder.fail(stack);
		mob.setPersistenceRequired();

		Vec3 facing = player.getLookAngle();
		double x = player.getX() + facing.x * 12.0D;
		double z = player.getZ() + facing.z * 12.0D;
		boolean settled = com.infernodude777.endesium.entity.BossPlacement.settleOnGround(mob, serverLevel, x, z);
		if (!settled) {
			// Fall back to the caller's own footing before refusing outright:
			// the player is standing on legal ground by definition.
			settled = com.infernodude777.endesium.entity.BossPlacement.settleOnGround(
					mob, serverLevel, player.getX(), player.getZ());
		}
		if (!settled) {
			// No open ground anywhere nearby: refuse WITHOUT consuming the
			// effigy instead of spawning an engine embedded in stone.
			serverPlayer.displayClientMessage(Component.translatable(
					"endesium.effigy.no_ground").withStyle(net.minecraft.ChatFormatting.GRAY), true);
			return InteractionResultHolder.fail(stack);
		}
		serverLevel.addFreshEntity(mob);

		double gx = mob.getX();
		double gy = mob.getY();
		double gz = mob.getZ();
		stack.shrink(1);

		serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, gx, gy + 3.0D, gz, 2, 1.0D, 1.0D, 1.0D, 0.0D);
		serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, gx, gy + 3.0D, gz, 60, 2.0D, 2.5D, 2.0D, 0.08D);
		serverLevel.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN,
				SoundSource.HOSTILE, 1.4F, 0.6F);
		serverPlayer.displayClientMessage(Component.translatable(
				"endesium.effigy.wakes").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE), true);

		var holder = serverPlayer.server.getAdvancements().get(Endesium.id("effigy_ignited"));
		if (holder != null) {
			serverPlayer.getAdvancements().award(holder, "effigy_used");
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
