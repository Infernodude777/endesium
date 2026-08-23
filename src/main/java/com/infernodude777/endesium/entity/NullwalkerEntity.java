package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import java.util.List;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * An extremely rare, watchful presence. It observes from a distance and
 * vanishes when approached too quickly, fighting back only when attacked.
 * It exists to unsettle, not to be farmed.
 */
public class NullwalkerEntity extends Monster implements GeoEntity {
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.nullwalker.idle");
	private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.nullwalker.walk");
	private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.nullwalker.attack");
	private static final RawAnimation HURT_ANIM = RawAnimation.begin().thenPlay("animation.nullwalker.hurt");
	private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.nullwalker.death");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
	/** Ticks until the next sound-mimicry attempt; refreshed after each use. */
	private int mimicryTicks = 100;

	public NullwalkerEntity(EntityType<? extends NullwalkerEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 40.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.27D)
				.add(Attributes.ATTACK_DAMAGE, 6.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
				.add(Attributes.FOLLOW_RANGE, 40.0D);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1D, true));
		goalSelector.addGoal(2, new VanishGoal(this));
		goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 24.0F));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide() && isAlive()) {
			if (mimicryTicks > 0) {
				mimicryTicks--;
			} else {
				performSoundMimicry((ServerLevel) level());
				mimicryTicks = 160 + random.nextInt(280);
			}
			// Null Suppression: the archive's wardens dampen mortal vigor
			// nearby — tools slow, blows weaken. Telegraphed by distortion.
			if (tickCount % 60 == 0 && hasTargetNearby()) {
				ServerLevel server = (ServerLevel) level();
				List<Player> suppressed = server.getEntitiesOfClass(Player.class,
						getBoundingBox().inflate(8.0D), Player::isAlive);
				if (!suppressed.isEmpty()) {
					server.sendParticles(ModParticles.NULL_DISTORTION,
							getX(), getY() + 1.2D, getZ(), 10, 1.2D, 0.6D, 1.2D, 0.01D);
					for (Player p : suppressed) {
						p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
						p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 120, 0));
					}
					playSound(ModSounds.NULLWALKER_IDLE, 0.7F, 0.5F);
				}
			}
		}
	}

	private boolean hasTargetNearby() {
		return level().getNearestPlayer(this, 8.0D) != null;
	}

	// Sound Mimicry: echoes the ambient call of a nearby creature to unsettle players.
	private void performSoundMimicry(ServerLevel level) {
		List<Mob> nearby = level.getEntitiesOfClass(Mob.class,
				this.getBoundingBox().inflate(24.0D), other -> other != this && other.isAlive());
		if (nearby.isEmpty()) return;
		Mob mimic = nearby.get(level.random.nextInt(nearby.size()));
		// Vanilla keeps Mob.getAmbientSound() protected, so mirror the End's
		// signature voices directly instead of reflecting into it.
		SoundEvent mimicked = null;
		float volume = 0.8F;
		if (mimic.getType() == net.minecraft.world.entity.EntityType.ENDERMAN) {
			mimicked = net.minecraft.sounds.SoundEvents.ENDERMAN_AMBIENT;
		} else if (mimic instanceof DustCrawlerEntity) {
			mimicked = ModSounds.DUST_CRAWLER_IDLE;
		} else if (mimic instanceof ChorusStalkerEntity) {
			mimicked = ModSounds.CHORUS_STALKER_IDLE;
		} else if (mimic instanceof VoidRayEntity) {
			mimicked = ModSounds.VOID_RAY_IDLE;
		} else if (mimic instanceof MarshCrawlerEntity) {
			mimicked = ModSounds.MARSH_CRAWLER_IDLE;
		} else if (mimic instanceof LumenMothEntity) {
			mimicked = ModSounds.LUMEN_MOTH_IDLE;
		} else if (mimic instanceof AshWraithEntity) {
			mimicked = ModSounds.ASH_WRAITH_IDLE;
		} else if (mimic instanceof CrystalBurrowerEntity) {
			mimicked = ModSounds.CRYSTAL_BURROWER_IDLE;
		}
		if (mimicked == null) return;
		playSound(mimicked, volume, getVoicePitch());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("EndesiumMimicryTicks", mimicryTicks);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		mimicryTicks = tag.getInt("EndesiumMimicryTicks");
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends NullwalkerEntity> PlayState animate(AnimationState<E> state) {
		if (isDeadOrDying()) return state.setAndContinue(DEATH);
		if (hurtTime > 0) return state.setAndContinue(HURT_ANIM);
		if (swinging) return state.setAndContinue(ATTACK);
		if (state.isMoving()) return state.setAndContinue(WALK);
		return state.setAndContinue(IDLE);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return animationCache;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.NULLWALKER_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return ModSounds.NULLWALKER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return ModSounds.NULLWALKER_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/nullwalker"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 8;
	}

	/** Vanishes to a nearby spot in a puff of distortion when approached. */
	private static final class VanishGoal extends Goal {
		private final NullwalkerEntity walker;

		VanishGoal(NullwalkerEntity walker) {
			this.walker = walker;
		}

		@Override
		public boolean canUse() {
			Player player = walker.level().getNearestPlayer(walker, 10.0D);
			if (player == null) return false;
			return walker.getTarget() == null && walker.getRandom().nextInt(12) == 0;
		}

		@Override
		public void start() {
			if (walker.level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ModParticles.NULL_DISTORTION,
						walker.getX(), walker.getY() + 1.0D, walker.getZ(),
						12, 0.3D, 0.3D, 0.3D, 0.01D);
			}
			// Only take the vanish step when the destination is actually safe:
			// solid footing below and room for the whole bounding box. A blind
			// same-Y teleport could bury the Nullwalker in a wall or hang it
			// over the void.
			for (int attempt = 0; attempt < 8; attempt++) {
				Vec3 candidate = walker.position().add(
						(walker.getRandom().nextDouble() - 0.5D) * 24.0D,
						0.0D,
						(walker.getRandom().nextDouble() - 0.5D) * 24.0D);
				BlockPos below = BlockPos.containing(candidate).below();
				if (!walker.level().getBlockState(below).isSolidRender(walker.level(), below)) continue;
				if (!walker.level().noCollision(walker,
						walker.getBoundingBox().move(candidate.x - walker.getX(), 0.0D, candidate.z - walker.getZ()))) {
					continue;
				}
				walker.teleportTo(candidate.x, walker.getY(), candidate.z);
				break;
			}
		}
	}
}
