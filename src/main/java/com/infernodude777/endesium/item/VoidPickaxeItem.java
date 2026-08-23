package com.infernodude777.endesium.item;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Void Pickaxe: creates a damaging resonance explosion on right-click when
 * the wearer has Void Leggings equipped. Deals damage to all entities in a
 * radius, but never to players wearing the full Void set. 5-second cooldown.
 */
public final class VoidPickaxeItem extends PickaxeItem {
	private static final int EXPLOSION_COOLDOWN_TICKS = 100;
	private static final double EXPLOSION_RADIUS = 8.0D;
	private static final float EXPLOSION_DAMAGE = 14.0F;

	public VoidPickaxeItem(Tier tier, Properties properties) {
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
		if (!serverPlayer.getItemBySlot(EquipmentSlot.LEGS).is(com.infernodude777.endesium.registry.ModItems.VOID_LEGGINGS)) {
			serverPlayer.displayClientMessage(
					Component.literal("The Void Leggings must answer before the pickaxe can resonate")
							.withStyle(ChatFormatting.DARK_GRAY), true);
			return InteractionResultHolder.fail(stack);
		}
		if (serverPlayer.getCooldowns().isOnCooldown(this)) {
			return InteractionResultHolder.fail(stack);
		}

		ServerLevel sLevel = serverPlayer.serverLevel();
		sLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
				serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(),
				1, 0.0D, 0.0D, 0.0D, 0.0D);
		sLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
				serverPlayer.getX(), serverPlayer.getY() + 1.0D, serverPlayer.getZ(),
				48, 1.5D, 1.5D, 1.5D, 0.12D);
		sLevel.playSound(null, BlockPos.containing(serverPlayer.position()),
				ModSounds.RESONANCE_STRIKE, SoundSource.PLAYERS, 2.0F, 0.6F);

		AABB area = new AABB(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
				serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ()).inflate(EXPLOSION_RADIUS);
		for (Entity entity : sLevel.getEntities((Entity) null, area,
				candidate -> candidate instanceof LivingEntity && candidate.isAlive()
						&& !(candidate instanceof net.minecraft.world.entity.decoration.ArmorStand)
						&& !(candidate instanceof net.minecraft.world.entity.npc.AbstractVillager)
						&& !(candidate instanceof net.minecraft.world.entity.TamableAnimal tamable && tamable.isTame()))) {
			if (entity instanceof LivingEntity living) {
				if (VoidEquipmentAbilities.isProtectedFromBlackHole(living)) {
					continue;
				}
				living.hurt(sLevel.damageSources().magic(), EXPLOSION_DAMAGE);
			}
		}
		// Stimulate the struck ground with the pickaxe's own mote trail.
		sLevel.sendParticles(ModParticles.VOID_STALKER_TRACE,
				serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
				24, 0.5D, 0.1D, 0.5D, 0.05D);

		serverPlayer.getCooldowns().addCooldown(this, EXPLOSION_COOLDOWN_TICKS);
		return InteractionResultHolder.success(stack);
	}
}