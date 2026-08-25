package com.infernodude777.endesium.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The Luminous, Ash, and Null tool lines. Each set has one signature active
 * ability (right-click) plus on-hit riders, so every set plays differently
 * instead of being a stat reskin:
 *
 * <ul>
 *   <li><b>Luminous</b> - light control: the sword flashes a prism burst that
 *       blinds and marks, the pickaxe places a lumen lantern (your light
 *       panel), the axe drinks light as healing, the shovel speeds you, and
 *       the hoe cleanses blindness.</li>
 *   <li><b>Ash</b> - firebending: the sword breathes a cone of flame, the
 *       pickaxe erupts a burning ring, the axe and shovel ignite on hit, and
 *       the hoe wraps you in heat haze.</li>
 *   <li><b>Null</b> - deletion: the sword pulses a gravity well, the pickaxe
 *       void-steps you forward, the axe withers, the shovel slows, and the
 *       hoe deletes your negative effects.</li>
 * </ul>
 *
 * Cooldowns are tracked by the vanilla item-cooldown system so the HUD shows
 * them for free.
 */
public final class EndgearTools {
	private EndgearTools() {
	}

	// =====================================================================
	// LUMINOUS
	// =====================================================================

	public static class LuminousSword extends SwordItem {
		public LuminousSword(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
			return super.hurtEnemy(stack, target, attacker);
		}

		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel server) {
				for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class,
						player.getBoundingBox().inflate(8.0D),
						e -> e != player && e.isAlive() && !(e instanceof Player))) {
					e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
					e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
					e.hurt(player.damageSources().magic(), 4.0F);
				}
				server.sendParticles(ParticleTypes.END_ROD,
						player.getX(), player.getY() + 1.5D, player.getZ(),
						60, 4.0D, 1.5D, 4.0D, 0.1D);
				server.playSound(null, player.blockPosition(),
						SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.4F);
				player.getCooldowns().addCooldown(this, 100);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
	}

	public static class LuminousPickaxe extends PickaxeItem {
		public LuminousPickaxe(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel server) {
				BlockPos at = BlockPos.containing(player.position()).above();
				if (level.getBlockState(at).isAir()) {
					level.setBlock(at, com.infernodude777.endesium.registry.ModBlocks.VOID_LAMP.defaultBlockState(), 3);
					server.playSound(null, at, SoundEvents.LANTERN_PLACE,
							SoundSource.PLAYERS, 1.0F, 1.2F);
				}
				player.getCooldowns().addCooldown(this, 40);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
	}

	public static class LuminousAxe extends AxeItem {
		public LuminousAxe(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			attacker.heal(2.0F);
			return super.hurtEnemy(stack, target, attacker);
		}
	}

	public static class LuminousShovel extends ShovelItem {
		public LuminousShovel(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, true));
			return super.hurtEnemy(stack, target, attacker);
		}
	}

	public static class LuminousHoe extends HoeItem {
		public LuminousHoe(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel server) {
				player.removeEffect(MobEffects.BLINDNESS);
				player.removeEffect(MobEffects.DARKNESS);
				player.removeEffect(MobEffects.WITHER);
				server.sendParticles(ParticleTypes.END_ROD,
						player.getX(), player.getY() + 1.0D, player.getZ(),
						20, 0.6D, 0.8D, 0.6D, 0.02D);
				player.getCooldowns().addCooldown(this, 200);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
	}

	// =====================================================================
	// ASH - firebending
	// =====================================================================

	public static class AshSword extends SwordItem {
		public AshSword(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			target.igniteForSeconds(4);
			return super.hurtEnemy(stack, target, attacker);
		}

		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel server) {
				Vec3 look = player.getLookAngle();
				Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
				for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class,
						player.getBoundingBox().inflate(7.0D),
						e -> e != player && e.isAlive()
								&& e.position().subtract(player.position()).normalize().dot(flat) > 0.5D)) {
					e.igniteForSeconds(6);
					e.hurt(player.damageSources().playerAttack(player), 3.0F);
				}
				for (int i = 1; i <= 6; i++) {
					Vec3 p = player.position().add(look.scale(i));
					server.sendParticles(ParticleTypes.FLAME,
							p.x, p.y + 1.0D, p.z, 6, 0.4D, 0.4D, 0.4D, 0.02D);
				}
				server.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
						SoundSource.PLAYERS, 1.0F, 0.8F);
				player.getCooldowns().addCooldown(this, 60);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
	}

	public static class AshPickaxe extends PickaxeItem {
		public AshPickaxe(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel server) {
				server.sendParticles(ParticleTypes.FLAME,
						player.getX(), player.getY() + 0.4D, player.getZ(),
						50, 2.5D, 0.3D, 2.5D, 0.08D);
				for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class,
						player.getBoundingBox().inflate(4.5D),
						e -> e != player && e.isAlive())) {
					e.igniteForSeconds(5);
					Vec3 kb = e.position().subtract(player.position()).normalize();
					e.setDeltaMovement(e.getDeltaMovement().add(kb.x * 0.8D, 0.4D, kb.z * 0.8D));
					e.hurtMarked = true;
				}
				server.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
						SoundSource.PLAYERS, 0.9F, 1.1F);
				player.getCooldowns().addCooldown(this, 100);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
	}

	public static class AshAxe extends AxeItem {
		public AshAxe(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			target.igniteForSeconds(5);
			return super.hurtEnemy(stack, target, attacker);
		}
	}

	public static class AshShovel extends ShovelItem {
		public AshShovel(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			target.igniteForSeconds(3);
			return super.hurtEnemy(stack, target, attacker);
		}
	}

	public static class AshHoe extends HoeItem {
		public AshHoe(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel server) {
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1, false, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, false, true));
				server.sendParticles(ParticleTypes.FLAME,
						player.getX(), player.getY() + 1.0D, player.getZ(),
						16, 0.5D, 0.8D, 0.5D, 0.02D);
				player.getCooldowns().addCooldown(this, 200);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
	}

	// =====================================================================
	// NULL - deletion
	// =====================================================================

	public static class NullSword extends SwordItem {
		public NullSword(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
			return super.hurtEnemy(stack, target, attacker);
		}

		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel server) {
				Vec3 center = player.position();
				for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class,
						player.getBoundingBox().inflate(7.0D),
						e -> e != player && e.isAlive())) {
					Vec3 pull = center.subtract(e.position()).normalize().scale(0.55D);
					e.setDeltaMovement(e.getDeltaMovement().add(pull.x, 0.1D, pull.z));
					e.hurtMarked = true;
					e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
				}
				server.sendParticles(ParticleTypes.SQUID_INK,
						center.x, center.y + 1.0D, center.z, 40, 2.0D, 1.0D, 2.0D, 0.08D);
				server.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
						SoundSource.PLAYERS, 1.0F, 0.5F);
				player.getCooldowns().addCooldown(this, 100);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
	}

	public static class NullPickaxe extends PickaxeItem {
		public NullPickaxe(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel server) {
				Vec3 look = player.getLookAngle();
				Vec3 dest = player.position().add(look.x * 8.0D, 0.5D, look.z * 8.0D);
				server.sendParticles(ParticleTypes.PORTAL,
						player.getX(), player.getY() + 1.0D, player.getZ(),
						24, 0.4D, 0.8D, 0.4D, 0.1D);
				player.teleportTo(dest.x, dest.y, dest.z);
				player.fallDistance = 0.0F;
				server.sendParticles(ParticleTypes.REVERSE_PORTAL,
						dest.x, dest.y + 1.0D, dest.z, 24, 0.4D, 0.8D, 0.4D, 0.1D);
				server.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
						SoundSource.PLAYERS, 1.0F, 1.0F);
				player.getCooldowns().addCooldown(this, 60);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
	}

	public static class NullAxe extends AxeItem {
		public NullAxe(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
			return super.hurtEnemy(stack, target, attacker);
		}
	}

	public static class NullShovel extends ShovelItem {
		public NullShovel(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
			return super.hurtEnemy(stack, target, attacker);
		}
	}

	public static class NullHoe extends HoeItem {
		public NullHoe(Tier tier, Properties properties) {
			super(tier, properties);
		}

		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel server) {
				List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());
				for (MobEffectInstance effect : effects) {
					if (!effect.getEffect().value().isBeneficial()) {
						player.removeEffect(effect.getEffect());
					}
				}
				server.sendParticles(ParticleTypes.SMOKE,
						player.getX(), player.getY() + 1.0D, player.getZ(),
						16, 0.5D, 0.8D, 0.5D, 0.02D);
				player.getCooldowns().addCooldown(this, 200);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
	}
}
