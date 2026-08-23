package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModEntities;
import com.infernodude777.endesium.world.EndBiomeProfiles;
import com.infernodude777.endesium.world.EndesiumRegions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.item.ItemStack;
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
 * The regional miniboss: one ancient warden construct per flagship, attuned
 * to whichever Endesium region it was forged in. Its body language, its
 * silhouette (region accessory bones), and its signature attack all change
 * with that attunement.
 *
 * <p>Fight contract: it periodically RAISES GUARD - frontal damage is almost
 * nullified while the guard is up, so flanking or patience is mandatory. At
 * two-thirds health it calls its biome's lesser kin. Below half health it
 * enrages: faster movement and halved special cooldowns. It guards the vault
 * loot and always carries a Warden Sigil keyed to its region.</p>
 */
public class EndWardenEntity extends Monster implements GeoEntity {
	private static final EntityDataAccessor<Byte> DATA_FLAGS =
			SynchedEntityData.defineId(EndWardenEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> DATA_REGION_ACCESSOR =
			SynchedEntityData.defineId(EndWardenEntity.class, EntityDataSerializers.BYTE);

	private static final byte FLAG_CASTING = 1;
	private static final byte FLAG_GUARDING = 2;

	/** Players closer than this see the boss bar; beyond it the bar retracts. */
	private static final double BOSS_BAR_RANGE = 64.0D;

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	private final ServerBossEvent bossBar = new ServerBossEvent(
			Component.translatable("entity.endesium.end_warden"),
			BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_6);

	private int specialCooldown = 120;
	private int guardCooldown = 200;
	private int guardTicks;
	private boolean minionsCalled;
	private boolean enragedAnnounced;

	public EndWardenEntity(EntityType<? extends EndWardenEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 80.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.28D)
				.add(Attributes.ATTACK_DAMAGE, 9.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
				.add(Attributes.ARMOR, 10.0D)
				.add(Attributes.FOLLOW_RANGE, 32.0D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_FLAGS, (byte) 0);
		builder.define(DATA_REGION_ACCESSOR, (byte) -1);
	}

	/** Region index this warden is attuned to, or -1 before first resolve. */
	public int getRegion() {
		return getEntityData().get(DATA_REGION_ACCESSOR);
	}

	private void setRegion(int region) {
		getEntityData().set(DATA_REGION_ACCESSOR, (byte) region);
		if (level() instanceof ServerLevel server) {
			bossBar.setColor(bossBarColorFor(region));
			bossBar.setName(Component.translatable("entity.endesium.end_warden"));
		}
	}

	public static BossEvent.BossBarColor bossBarColorFor(int region) {
		return switch (Math.max(0, region)) {
			case EndesiumRegions.END_WASTES -> BossEvent.BossBarColor.GREEN;
			case EndesiumRegions.SHATTERED_HIGHLANDS -> BossEvent.BossBarColor.WHITE;
			case EndesiumRegions.VOID_MARSHES -> BossEvent.BossBarColor.BLUE;
			case EndesiumRegions.CHORUS_WILDS -> BossEvent.BossBarColor.PURPLE;
			case EndesiumRegions.LUMINOUS_GROVES -> BossEvent.BossBarColor.YELLOW;
			case EndesiumRegions.ASHEN_EXPANSE -> BossEvent.BossBarColor.RED;
			case EndesiumRegions.CRYSTAL_BARRENS -> BossEvent.BossBarColor.PURPLE;
			case EndesiumRegions.VOID_SKIRTS -> BossEvent.BossBarColor.BLUE;
			case EndesiumRegions.VOID_CROWN -> BossEvent.BossBarColor.YELLOW;
			default -> BossEvent.BossBarColor.WHITE; // umbral reach
		};
	}

	public boolean isCasting() {
		return (getEntityData().get(DATA_FLAGS) & FLAG_CASTING) != 0;
	}

	private void setCasting(boolean casting) {
		byte flags = getEntityData().get(DATA_FLAGS);
		getEntityData().set(DATA_FLAGS, casting ? (byte) (flags | FLAG_CASTING) : (byte) (flags & ~FLAG_CASTING));
	}

	public boolean isGuarding() {
		return (getEntityData().get(DATA_FLAGS) & FLAG_GUARDING) != 0;
	}

	private void setGuarding(boolean guarding) {
		byte flags = getEntityData().get(DATA_FLAGS);
		getEntityData().set(DATA_FLAGS, guarding ? (byte) (flags | FLAG_GUARDING) : (byte) (flags & ~FLAG_GUARDING));
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.05D, true));
		goalSelector.addGoal(2, new RegionalSpecialGoal(this));
		goalSelector.addGoal(3, new GuardStanceGoal(this));
		goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.5D));
		goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
		goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		targetSelector.addGoal(1, new HurtByTargetGoal(this));
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void tick() {
		super.tick();
		if (level() instanceof ServerLevel server) {
			if (getRegion() < 0) {
				// Retry attunement on a slow clock until the biome resolves;
				// a warden spawned before its chunk's biome data was readable
				// used to stay stuck at -1 with a white bar forever.
				if (tickCount % 20 == 0) {
					int region = EndBiomeProfiles.regionOf(level().getBiome(blockPosition()));
					if (region >= 0) setRegion(region);
				}
			} else if (tickCount % 10 == 0) {
				updateBossBarAudience(server);
			}
			if (guardTicks > 0 && --guardTicks == 0) {
				setGuarding(false);
				playSound(SoundEvents.SHIELD_BLOCK, 0.8F, 0.8F);
			}
			// A guarding warden plants its feet.
			if (isGuarding()) {
				getNavigation().stop();
			}
			bossBar.setProgress((float) java.lang.Math.clamp(getHealth() / getMaxHealth(), 0.0D, 1.0D));
			maybeCallMinions(server);
		}
	}

	/**
	 * Shows the boss bar only to players actually near the fight and retracts
	 * it from everyone else, so a flagship warden no longer pins a permanent
	 * bar onto every player in the dimension.
	 */
	private void updateBossBarAudience(ServerLevel server) {
		for (ServerPlayer player : server.players()) {
			if (player.distanceToSqr(this) <= BOSS_BAR_RANGE * BOSS_BAR_RANGE) {
				bossBar.addPlayer(player);
			} else if (!player.isSpectator()) {
				bossBar.removePlayer(player);
			}
		}
	}

	/** Once per fight, at two-thirds health, the warden summons its biome's kin. */
	private void maybeCallMinions(ServerLevel server) {
		if (minionsCalled || !isAlive()) return;
		if (getHealth() > getMaxHealth() * (2.0D / 3.0D)) return;
		minionsCalled = true;
		playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.7F);
		server.sendParticles(ParticleTypes.REVERSE_PORTAL,
				getX(), getY() + 1.5D, getZ(), 30, 1.0D, 1.0D, 1.0D, 0.06D);
		for (Vec3 spot : minionSpots()) {
			Mob minion = createRegionalMinion(server);
			if (minion == null) continue;
			minion.moveTo(spot.x, getY(), spot.z, getYRot(), 0.0F);
			server.addFreshEntity(minion);
			server.sendParticles(ParticleTypes.REVERSE_PORTAL,
					spot.x, getY() + 1.0D, spot.z, 14, 0.3D, 0.8D, 0.3D, 0.04D);
		}
	}

	private Vec3[] minionSpots() {
		return new Vec3[]{
				position().add(3.5D, 0, 0),
				position().add(-3.5D, 0, 0)
		};
	}

	private Mob createRegionalMinion(ServerLevel server) {
		int region = Math.max(0, getRegion());
		EntityType<? extends Mob> type = switch (region) {
			case EndesiumRegions.END_WASTES -> ModEntities.DUST_CRAWLER;
			case EndesiumRegions.SHATTERED_HIGHLANDS -> ModEntities.VOID_RAY;
			case EndesiumRegions.VOID_MARSHES -> ModEntities.MARSH_CRAWLER;
			case EndesiumRegions.CHORUS_WILDS -> ModEntities.CHORUS_STALKER;
			case EndesiumRegions.LUMINOUS_GROVES -> ModEntities.LUMEN_MOTH;
			case EndesiumRegions.ASHEN_EXPANSE -> ModEntities.ASH_WRAITH;
			case EndesiumRegions.CRYSTAL_BARRENS -> ModEntities.CRYSTAL_BURROWER;
			case EndesiumRegions.VOID_SKIRTS -> ModEntities.VOID_WISP;
			case EndesiumRegions.VOID_CROWN -> ModEntities.CROWN_SENTINEL;
			default -> ModEntities.NULLWALKER;
		};
		Mob minion = type.create(server);
		if (minion != null && getTarget() != null) {
			minion.setTarget(getTarget());
		}
		return minion;
	}

	public boolean isEnraged() {
		return getHealth() > 0.0D && getHealth() < getMaxHealth() * 0.5D;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "behavior", 5, this::animate));
	}

	private <E extends EndWardenEntity> PlayState animate(AnimationState<E> state) {
		if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().thenPlay("animation.end_warden.death"));
		if (isCasting()) return state.setAndContinue(RawAnimation.begin().thenPlay("animation.end_warden.cast"));
		if (isGuarding()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.end_warden.guard"));
		if (swinging) return state.setAndContinue(RawAnimation.begin().thenPlay("animation.end_warden.attack"));
		if (state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.end_warden.walk"));
		return state.setAndContinue(RawAnimation.begin().thenLoop("animation.end_warden.idle"));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return animationCache;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		// Constructs never suffocate: vault geometry must never kill them.
		if (source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) return false;
		// Constructs shrug off a fifth of blast damage; fight it, don't bomb it.
		if (source.is(DamageTypeTags.IS_EXPLOSION)) amount *= 0.8F;
		// Raised guard: near-immune from the front, forcing flanks.
		if (isGuarding() && isFrontal(source)) {
			amount *= 0.25F;
			if (level() instanceof ServerLevel server && random.nextInt(3) == 0) {
				server.sendParticles(ParticleTypes.CRIT,
						getX() + getLookAngle().x, getY() + 1.4D, getZ() + getLookAngle().z,
						6, 0.4D, 0.4D, 0.4D, 0.05D);
				playSound(SoundEvents.SHIELD_BLOCK, 0.7F, 1.1F);
			}
		}
		return super.hurt(source, amount);
	}

	private boolean isFrontal(DamageSource source) {
		Entity attacker = source.getEntity();
		if (attacker == null) {
			// Direct projectiles keep their owner as immediate source.
			attacker = source.getDirectEntity();
		}
		if (attacker == null) return false;
		Vec3 toAttacker = attacker.position().subtract(position());
		Vec3 look = getLookAngle();
		double dot = toAttacker.normalize().dot(new Vec3(look.x, 0, look.z).normalize());
		return dot > -0.35D; // roughly a 140-degree frontal cone
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		if (player.distanceToSqr(this) <= BOSS_BAR_RANGE * BOSS_BAR_RANGE) {
			bossBar.addPlayer(player);
		}
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossBar.removePlayer(player);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level() instanceof ServerLevel server) {
			// The enrage moment gets one loud announcement instead of none.
			if (isEnraged() && !enragedAnnounced) {
				enragedAnnounced = true;
				announceEnrage(server);
			} else if (!isEnraged()) {
				enragedAnnounced = false;
			}
			if (isEnraged() && tickCount % 8 == 0) {
				server.sendParticles(ParticleTypes.FLAME,
						getX(), getY() + 1.6D, getZ(), 1, 0.3D, 0.4D, 0.3D, 0.01D);
			}
		}
	}

	/** A crack of resonance light and a horn blast mark the enrage threshold. */
	private void announceEnrage(ServerLevel server) {
		playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 0.6F);
		server.sendParticles(ModParticles.RESONANCE_PULSE,
				getX(), getY() + 1.8D, getZ(), 24, 1.2D, 0.8D, 1.2D, 0.05D);
		server.sendParticles(ParticleTypes.FLAME,
				getX(), getY() + 1.2D, getZ(), 20, 0.9D, 0.4D, 0.9D, 0.04D);
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (level() instanceof ServerLevel server) {
			// The sigil's farewell beam marks where the region's warden fell.
			server.sendParticles(ParticleTypes.END_ROD,
					getX(), getY() + 1.0D, getZ(), 60, 0.4D, 4.0D, 0.4D, 0.02D);
			server.sendParticles(ModParticles.RESONANCE_ACTIVE,
					getX(), getY() + 1.0D, getZ(), 30, 1.0D, 2.5D, 1.0D, 0.05D);
			playSound(SoundEvents.BEACON_ACTIVATE, 1.0F, 1.4F);
			openNearbyVaults(server);
		}
	}

	/** The warden's fall retracts nearby vault bars: loot rooms open on death. */
	private void openNearbyVaults(ServerLevel server) {
		BlockPos center = blockPosition();
		int opened = 0;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-24, -16, -24), center.offset(24, 24, 24))) {
			if (!server.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.IRON_BARS)) continue;
			server.removeBlock(pos.immutable(), false);
			server.sendParticles(ParticleTypes.POOF,
					pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
					2, 0.15D, 0.15D, 0.15D, 0.01D);
			if (++opened >= 128) break;
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.IRON_GOLEM_REPAIR;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.IRON_GOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.IRON_GOLEM_DEATH;
	}

	@Override
	protected ResourceKey<LootTable> getDefaultLootTable() {
		return ResourceKey.create(Registries.LOOT_TABLE, Endesium.id("entities/end_warden"));
	}

	@Override
	protected int getBaseExperienceReward() {
		return 35;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, source, recentlyHit);
		// The warden's bonded sigil: keyed to the region it guarded.
		ItemStack sigil = new ItemStack(com.infernodude777.endesium.registry.ModItems.WARDEN_SIGIL);
		com.infernodude777.endesium.item.WardenSigilItem.writeRegion(sigil, Math.max(0, getRegion()));
		spawnAtLocation(sigil);
		if (isEnraged()) {
			// An enraged kill proves mastery: bonus cache.
			spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.AMETHYST_SHARD, 6 + random.nextInt(6)));
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("EndesiumWardenRegion", getRegion());
		tag.putBoolean("EndesiumWardenMinionsCalled", minionsCalled);
		tag.putBoolean("EndesiumWardenEnraged", enragedAnnounced);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setRegion(tag.getInt("EndesiumWardenRegion"));
		minionsCalled = tag.getBoolean("EndesiumWardenMinionsCalled");
		enragedAnnounced = tag.getBoolean("EndesiumWardenEnraged");
		if (hasCustomName()) bossBar.setName(getDisplayName());
		// Restore the bar to the health it had before unload.
		bossBar.setProgress((float) java.lang.Math.clamp(getHealth() / getMaxHealth(), 0.0D, 1.0D));
	}

	/**
	 * Raises guard: plants both feet, front almost immune, waiting out the
	 * storm or baiting a flank.
	 */
	private static final class GuardStanceGoal extends Goal {
		private final EndWardenEntity warden;

		GuardStanceGoal(EndWardenEntity warden) {
			this.warden = warden;
		}

		@Override
		public boolean canUse() {
			if (warden.guardCooldown > 0 || warden.isGuarding()) return false;
			LivingEntity target = warden.getTarget();
			if (target == null) return false;
			return warden.distanceToSqr(target) <= 100.0D;
		}

		@Override
		public void start() {
			warden.guardCooldown = warden.isEnraged() ? 240 : 400;
			warden.guardTicks = 80;
			warden.setGuarding(true);
			warden.getNavigation().stop();
			warden.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.6F);
			if (warden.level() instanceof ServerLevel server) {
				server.sendParticles(ParticleTypes.POOF,
						warden.getX(), warden.getY() + 1.2D, warden.getZ(),
						10, 0.6D, 0.4D, 0.6D, 0.02D);
			}
		}
	}

	/**
	 * The signature attack, switched by region attunement. Each variant keeps
	 * the same contract: telegraphed, ranged-or-area, and dodgeable.
	 */
	private static final class RegionalSpecialGoal extends Goal {
		private final EndWardenEntity warden;
		private int windup;

		RegionalSpecialGoal(EndWardenEntity warden) {
			this.warden = warden;
		}

		@Override
		public boolean canUse() {
			if (windup > 0 || warden.specialCooldown > 0 || warden.isGuarding()) return false;
			LivingEntity target = warden.getTarget();
			if (target == null) return false;
			double dist = warden.distanceToSqr(target);
			return dist <= 400.0D && warden.hasLineOfSight(target);
		}

		@Override
		public void start() {
			windup = 20;
			warden.setCasting(true);
			if (warden.level() instanceof ServerLevel server) {
				server.sendParticles(ModParticles.RESONANCE_PULSE,
						warden.getX(), warden.getY() + 1.8D, warden.getZ(),
						10, 0.6D, 0.4D, 0.6D, 0.01D);
			}
		}

		@Override
		public boolean canContinueToUse() {
			return windup > 0;
		}

		@Override
		public void tick() {
			windup--;
			LivingEntity target = warden.getTarget();
			if (target != null) warden.getLookControl().setLookAt(target);
			if (windup == 0 && target != null) {
				performRegionalAttack(target);
				warden.specialCooldown = warden.isEnraged() ? 90 : 180;
			}
		}

		@Override
		public void stop() {
			warden.setCasting(false);
		}

		private void performRegionalAttack(LivingEntity target) {
			ServerLevel server = (ServerLevel) warden.level();
			int region = warden.getRegion();
			switch (region < 0 ? EndesiumRegions.END_WASTES : region) {
				case EndesiumRegions.END_WASTES -> fireShards(server, target, 4); // dust bolt fan
				case EndesiumRegions.CHORUS_WILDS -> blinkBehind(server, target); // flank blink
				case EndesiumRegions.SHATTERED_HIGHLANDS -> galeSlam(server, target); // knockback burst
				case EndesiumRegions.VOID_MARSHES -> mireGrasp(server, target); // pull + slow
				case EndesiumRegions.LUMINOUS_GROVES -> lumenFlash(server, target); // blind + self heal
				case EndesiumRegions.ASHEN_EXPANSE -> emberNova(server, target); // ignite ring
				case EndesiumRegions.CRYSTAL_BARRENS -> fireShards(server, target, 3); // homing shards
				case EndesiumRegions.VOID_SKIRTS -> mireGrasp(server, target); // drag toward edge
				case EndesiumRegions.VOID_CROWN -> galeSlam(server, target); // seal slam
				default -> suppress(server, target); // umbral null suppression
			}
			warden.swing(InteractionHand.MAIN_HAND);
		}

		private void fireShards(ServerLevel server, LivingEntity target, int count) {
			for (int i = 0; i < count; i++) {
				net.minecraft.world.entity.projectile.ShulkerBullet shard =
						new net.minecraft.world.entity.projectile.ShulkerBullet(
								warden.level(), warden, target,
								warden.getDirection().getAxis());
				shard.setPos(warden.getX(), warden.getY() + 2.0D, warden.getZ());
				warden.level().addFreshEntity(shard);
			}
			server.sendParticles(ModParticles.CRYSTAL_MOTE,
					warden.getX(), warden.getY() + 2.0D, warden.getZ(),
					12, 0.5D, 0.4D, 0.5D, 0.06D);
		}

		private void blinkBehind(ServerLevel server, LivingEntity target) {
			Vec3 dest = target.position().add(target.getLookAngle().scale(-3.0D));
			BlockPos below = BlockPos.containing(dest).below();
			if (!warden.level().getBlockState(below).isSolidRender(warden.level(), below)) return;
			server.sendParticles(ParticleTypes.PORTAL,
					warden.getX(), warden.getY() + 1.0D, warden.getZ(),
					14, 0.3D, 0.6D, 0.3D, 0.05D);
			warden.teleportTo(dest.x, dest.y, dest.z);
			target.hurt(warden.damageSources().mobAttack(warden), 6.0F);
		}

		private void galeSlam(ServerLevel server, LivingEntity target) {
			server.sendParticles(ParticleTypes.POOF,
					warden.getX(), warden.getY() + 0.5D, warden.getZ(),
					24, 2.2D, 0.3D, 2.2D, 0.05D);
			for (Player p : server.getEntitiesOfClass(Player.class,
					warden.getBoundingBox().inflate(5.0D), Player::isAlive)) {
				Vec3 kb = p.position().subtract(warden.position()).normalize();
				p.setDeltaMovement(p.getDeltaMovement().add(kb.x * 1.3D, 0.65D, kb.z * 1.3D));
				p.hurtMarked = true;
				p.hurt(warden.damageSources().mobAttack(warden), 5.0F);
			}
			warden.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.9F, 0.8F);
		}

		private void mireGrasp(ServerLevel server, LivingEntity target) {
			Vec3 pull = warden.position().subtract(target.position()).normalize().scale(1.1D);
			target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.15D, pull.z));
			target.hurtMarked = true;
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
			server.sendParticles(ParticleTypes.SQUID_INK,
					target.getX(), target.getY() + 0.5D, target.getZ(),
					10, 0.3D, 0.3D, 0.3D, 0.02D);
		}

		private void lumenFlash(ServerLevel server, LivingEntity target) {
			server.sendParticles(ParticleTypes.END_ROD,
					warden.getX(), warden.getY() + 1.5D, warden.getZ(),
					20, 1.0D, 0.8D, 1.0D, 0.08D);
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
			warden.heal(8.0F);
			warden.playSound(SoundEvents.ALLAY_ITEM_GIVEN, 1.0F, 0.6F);
		}

		private void emberNova(ServerLevel server, LivingEntity target) {
			server.sendParticles(ParticleTypes.FLAME,
					warden.getX(), warden.getY() + 0.6D, warden.getZ(),
					40, 2.5D, 0.4D, 2.5D, 0.1D);
			for (Player p : server.getEntitiesOfClass(Player.class,
					warden.getBoundingBox().inflate(4.5D), Player::isAlive)) {
				p.hurt(warden.damageSources().mobAttack(warden), 5.0F);
				p.igniteForSeconds(5);
			}
		}

		private void suppress(ServerLevel server, LivingEntity target) {
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
			target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 140, 0));
			target.hurt(warden.damageSources().mobAttack(warden), 4.0F);
			server.sendParticles(ModParticles.NULL_DISTORTION,
					target.getX(), target.getY() + 1.0D, target.getZ(),
					14, 0.4D, 0.6D, 0.4D, 0.02D);
		}
	}
}
