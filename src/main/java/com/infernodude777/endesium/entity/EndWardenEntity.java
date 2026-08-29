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

import java.util.EnumSet;
import java.util.List;

/**
 * The regional miniboss: one ancient warden construct per flagship, attuned
 * to whichever Endesium region it was forged in. Its body language, its
 * silhouette (region accessory bones), and its signature attack all change
 * with that attunement.
 *
 * <p>Fight contract: it periodically RAISES GUARD - frontal damage is almost
 * nullified while the guard is up, so flanking or patience is mandatory. At
 * two-thirds and one-third health it calls its biome's lesser kin in two
 * escalating waves. Below half health it enrages permanently: harder hits,
 * faster movement, halved special cooldowns, and no mercy on the throw.</p>
 *
 * <p>Signature moves: the SKYWARD SEIZE hoists a player in its claws
 * (full GeckoLib pickup / carry / throw animation set) and hurls them across
 * the vault; the RESONANCE SLAM detonates a two-ring shockwave that must be
 * jumped; each region keeps its own telegraphed special.</p>
 */
public class EndWardenEntity extends Monster implements GeoEntity {
	private static final EntityDataAccessor<Byte> DATA_FLAGS =
			SynchedEntityData.defineId(EndWardenEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> DATA_REGION_ACCESSOR =
			SynchedEntityData.defineId(EndWardenEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Integer> DATA_GRAB_PHASE =
			SynchedEntityData.defineId(EndWardenEntity.class, EntityDataSerializers.INT);

	private static final byte FLAG_CASTING = 1;
	private static final byte FLAG_GUARDING = 2;
	private static final byte FLAG_SLAM = 4;

	/** Grab phases: 0 idle, 1 windup (pickup anim), 2 carrying, 3 throwing. */
	public static final int GRAB_IDLE = 0;
	public static final int GRAB_WINDUP = 1;
	public static final int GRAB_CARRY = 2;
	public static final int GRAB_THROW = 3;

	/** Damage that must be dealt to the warden mid-carry to force an early drop. */
	private static final float CARRY_BREAK_DAMAGE = 20.0F;

	/** Players closer than this see the boss bar; beyond it the bar retracts. */
	private static final double BOSS_BAR_RANGE = 64.0D;

	private static final RawAnimation PICKUP_ANIM = RawAnimation.begin().thenPlay("animation.end_warden.pickup");
	private static final RawAnimation CARRY_ANIM = RawAnimation.begin().thenLoop("animation.end_warden.carry");
	private static final RawAnimation THROW_ANIM = RawAnimation.begin().thenPlay("animation.end_warden.throw");
	private static final RawAnimation SLAM_ANIM = RawAnimation.begin().thenPlay("animation.end_warden.slam");

	private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

	private final ServerBossEvent bossBar = new ServerBossEvent(
			Component.translatable("entity.endesium.end_warden"),
			BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_6);

	private int specialCooldown = 120;
	private int guardCooldown = 200;
	private int seizeCooldown = 160;
	private int slamCooldown = 140;
	private int guardTicks;
	/** 0 = no waves called, 1 = first wave called, 2 = both waves called. */
	private int minionWaves;
	private boolean enragedAnnounced;

	// Grab state (server side)
	private int grabPhase;
	private int carryTicks;
	private float carryDamage;

	public EndWardenEntity(EntityType<? extends EndWardenEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		// One entry per attribute - duplicated keys would silently clobber each
		// other (last wins), which once reduced this boss's aggro range to 32.
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 400.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.33D)
				.add(Attributes.ATTACK_DAMAGE, 18.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.ARMOR, 16.0D)
				.add(Attributes.ARMOR_TOUGHNESS, 6.0D)
				.add(Attributes.ATTACK_KNOCKBACK, 1.5D)
				.add(Attributes.FOLLOW_RANGE, 64.0D)
				.add(Attributes.STEP_HEIGHT, 1.0D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_FLAGS, (byte) 0);
		builder.define(DATA_REGION_ACCESSOR, (byte) -1);
		builder.define(DATA_GRAB_PHASE, GRAB_IDLE);
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

	public boolean isSlamming() {
		return (getEntityData().get(DATA_FLAGS) & FLAG_SLAM) != 0;
	}

	private void setSlamming(boolean slamming) {
		byte flags = getEntityData().get(DATA_FLAGS);
		getEntityData().set(DATA_FLAGS, slamming ? (byte) (flags | FLAG_SLAM) : (byte) (flags & ~FLAG_SLAM));
	}

	/** Current grab phase, mirrored to the client for animation playback. */
	public int getGrabPhase() {
		return getEntityData().get(DATA_GRAB_PHASE);
	}

	private void setGrabPhase(int phase) {
		grabPhase = phase;
		getEntityData().set(DATA_GRAB_PHASE, phase);
	}

	public boolean isGrabbing() {
		return grabPhase != GRAB_IDLE;
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new SeizeGoal(this));
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, true));
		goalSelector.addGoal(3, new RegionalSpecialGoal(this));
		goalSelector.addGoal(4, new ResonanceSlamGoal(this));
		goalSelector.addGoal(5, new GuardStanceGoal(this));
		goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.5D));
		goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
		goalSelector.addGoal(8, new RandomLookAroundGoal(this));
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
			// Cooldowns tick down centrally so goal polling can never stall them.
			if (specialCooldown > 0) specialCooldown--;
			if (guardCooldown > 0) guardCooldown--;
			if (seizeCooldown > 0) seizeCooldown--;
			if (slamCooldown > 0) slamCooldown--;
			// A guarding or carrying warden plants its feet.
			if (isGuarding() || grabPhase == GRAB_CARRY) {
				getNavigation().stop();
			}
			if (grabPhase == GRAB_CARRY) {
				tickCarry(server);
			} else if (grabPhase == GRAB_THROW) {
				// Brief committed window for the throw animation to read.
				if (--carryTicks <= 0) setGrabPhase(GRAB_IDLE);
			}
			bossBar.setProgress((float) java.lang.Math.clamp(getHealth() / getMaxHealth(), 0.0D, 1.0D));
			maybeCallMinions(server);
		}
	}

	// --- Skyward Seize: grab, carry, hurl ---

	/** Attempts the seize on a validated target; returns false if ineligible. */
	private boolean beginSeize(ServerLevel server, LivingEntity target) {
		if (grabPhase != GRAB_IDLE || isGuarding() || isCasting() || isSlamming()) return false;
		if (target.isPassenger() || target.isVehicle() || !target.onGround()) return false;
		if (target instanceof Player player && player.isSpectator()) return false;
		double dist = distanceToSqr(target);
		if (dist < 2.25D || dist > 14.0D * 14.0D || !hasLineOfSight(target)) return false;

		setGrabPhase(GRAB_WINDUP);
		carryTicks = 16; // windup duration, reused as throw-committed window later
		carryDamage = 0.0F;
		getNavigation().stop();
		playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 1.4F, 0.6F);
		server.sendParticles(ModParticles.RESONANCE_PULSE,
				getX(), getY() + 2.2D, getZ(), 14, 0.7D, 0.6D, 0.7D, 0.03D);
		return true;
	}

	private void tickCarry(ServerLevel server) {
		Entity held = getFirstPassenger();
		if (held == null || !held.isAlive() || held.isRemoved()) {
			endGrab(120);
			return;
		}
		if (carryTicks-- <= 0) {
			hurlHeld(server, held);
			return;
		}
		// Telekinetic claw dust + a subtle rotation so the carry reads alive.
		if (tickCount % 3 == 0) {
			server.sendParticles(ParticleTypes.REVERSE_PORTAL,
					getX(), getY() + 2.4D, getZ(), 3, 0.8D, 0.5D, 0.8D, 0.03D);
		}
		setYRot(getYRot() + 1.4F);
		held.fallDistance = 0.0F;
	}

	private void hurlHeld(ServerLevel server, Entity held) {
		setGrabPhase(GRAB_THROW);
		carryTicks = 12; // committed throw animation window
		if (held instanceof LivingEntity living) {
			living.stopRiding();
		}
		double speed = isEnraged() ? 2.1D : 1.6D;
		double up = isEnraged() ? 1.05D : 0.85D;
		Vec3 dir = Vec3.directionFromRotation(0.0F, getYRot());
		held.setDeltaMovement(dir.x * speed, up, dir.z * speed);
		held.hurtMarked = true;
		if (held instanceof LivingEntity living) {
			living.hurt(damageSources().mobAttack(this), isEnraged() ? 12.0F : 8.0F);
			// Mercy only while calm: an enraged warden lets the fall do the work.
			if (!isEnraged()) {
				living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
			}
		}
		playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.2F, 0.9F);
		server.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + 2.2D, getZ(), 1, 0, 0, 0, 0);
	}

	/** Cancels an in-progress grab and reapplies its cooldown. */
	private void endGrab(int cooldown) {
		setGrabPhase(GRAB_IDLE);
		carryTicks = 0;
		seizeCooldown = Math.max(seizeCooldown, cooldown);
	}

	@Override
	protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
		if (grabPhase == GRAB_CARRY) {
			// Held in front at claw height, like the pickup animation ends.
			Vec3 dir = Vec3.directionFromRotation(0.0F, getYRot());
			callback.accept(passenger,
					getX() + dir.x * 1.15D,
					getY() + 2.55D,
					getZ() + dir.z * 1.15D);
			passenger.setYRot(getYRot());
			passenger.fallDistance = 0.0F;
		} else {
			super.positionRider(passenger, callback);
		}
	}

	@Override
	public void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		// A player who wriggles free (sneak-dismount, death, teleport) ends the
		// seize immediately; the warden must recommit before trying again.
		if (grabPhase == GRAB_CARRY && level() instanceof ServerLevel) {
			endGrab(100);
		}
	}

	/**
	 * Shows the boss bar only to players actually near the fight and retracts
	 * it from everyone else, so a flagship warden no longer pins a permanent
	 * bar onto every player in the dimension.
	 */
	private void updateBossBarAudience(ServerLevel server) {
		for (ServerPlayer player : server.players()) {
			if (player.distanceToSqr(this) <= BOSS_BAR_RANGE * BOSS_BAR_RANGE && !player.isSpectator()) {
				bossBar.addPlayer(player);
			} else {
				// Spectators must also lose the bar, or a once-near spectator
				// keeps it pinned to their screen forever.
				bossBar.removePlayer(player);
			}
		}
	}

	/** Two escalating minion waves: two-thirds and one-third health. */
	private void maybeCallMinions(ServerLevel server) {
		if (!isAlive()) return;
		float fraction = getHealth() / getMaxHealth();
		while (minionWaves < 2 && fraction <= (minionWaves == 0 ? 2.0D / 3.0D : 1.0D / 3.0D)) {
			callMinionWave(server, minionWaves == 0 ? 2 : 3);
			minionWaves++;
		}
	}

	private void callMinionWave(ServerLevel server, int count) {
		if (isEnraged()) count++;
		playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.7F);
		server.sendParticles(ParticleTypes.REVERSE_PORTAL,
				getX(), getY() + 1.5D, getZ(), 30, 1.0D, 1.0D, 1.0D, 0.06D);
		for (int i = 0; i < count; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double px = getX() + Math.cos(angle) * 3.5D;
			double pz = getZ() + Math.sin(angle) * 3.5D;
			Mob minion = createRegionalMinion(server);
			if (minion == null) continue;
			minion.moveTo(px, getY(), pz, getYRot(), 0.0F);
			server.addFreshEntity(minion);
			server.sendParticles(ParticleTypes.REVERSE_PORTAL,
					px, getY() + 1.0D, pz, 14, 0.3D, 0.8D, 0.3D, 0.04D);
		}
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
		int grab = getGrabPhase();
		if (grab == GRAB_WINDUP || grab == GRAB_THROW) return state.setAndContinue(PICKUP_ANIM);
		if (grab == GRAB_CARRY) return state.setAndContinue(CARRY_ANIM);
		if (isSlamming()) return state.setAndContinue(SLAM_ANIM);
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
		// Counterplay for the seize: chunky damage mid-carry forces an early drop.
		if (grabPhase == GRAB_CARRY) {
			carryDamage += amount;
			if (carryDamage >= CARRY_BREAK_DAMAGE) {
				endGrab(60);
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
		setAttributeValueSafe(Attributes.ATTACK_DAMAGE, 24.0D);
		setAttributeValueSafe(Attributes.MOVEMENT_SPEED, 0.38D);
		server.sendParticles(ModParticles.RESONANCE_PULSE,
				getX(), getY() + 1.8D, getZ(), 24, 1.2D, 0.8D, 1.2D, 0.05D);
		server.sendParticles(ParticleTypes.FLAME,
				getX(), getY() + 1.2D, getZ(), 20, 0.9D, 0.4D, 0.9D, 0.04D);
	}

	private void setAttributeValueSafe(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double value) {
		var instance = getAttribute(attribute);
		if (instance != null) instance.setBaseValue(value);
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
		return 200;
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
		tag.putInt("EndesiumWardenMinionWaves", minionWaves);
		tag.putBoolean("EndesiumWardenEnraged", enragedAnnounced);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setRegion(tag.getInt("EndesiumWardenRegion"));
		minionWaves = tag.getInt("EndesiumWardenMinionWaves");
		enragedAnnounced = tag.getBoolean("EndesiumWardenEnraged");
		// Grabs never survive a save: a reloaded warden starts clean.
		if (getFirstPassenger() instanceof LivingEntity living) {
			living.stopRiding();
		}
		setGrabPhase(GRAB_IDLE);
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
	 * SKYWARD SEIZE: telegraphed pickup, claws-up carry, then a committed hurl.
	 * Counterplay: break line of sight during the windup, sneak-dismount, or
	 * deal 20 damage while held to force an early drop.
	 */
	private static final class SeizeGoal extends Goal {
		private final EndWardenEntity warden;

		SeizeGoal(EndWardenEntity warden) {
			this.warden = warden;
			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (warden.seizeCooldown > 0 || warden.isGrabbing()) return false;
			LivingEntity target = warden.getTarget();
			if (target == null || !(warden.level() instanceof ServerLevel server)) return false;
			return warden.beginSeize(server, target);
		}

		@Override
		public boolean canContinueToUse() {
			return warden.isGrabbing();
		}

		@Override
		public void tick() {
			warden.getNavigation().stop();
			LivingEntity target = warden.getTarget();
			if (warden.grabPhase == EndWardenEntity.GRAB_WINDUP) {
				// Track the victim through the windup; if it escapes the cone
				// the grab whiffs and costs a short cooldown.
				if (target != null) {
					warden.getLookControl().setLookAt(target);
					if (warden.tickCount % 4 == 0 && warden.level() instanceof ServerLevel server) {
						server.sendParticles(ParticleTypes.REVERSE_PORTAL,
								target.getX(), target.getY() + 1.0D, target.getZ(), 4, 0.3D, 0.5D, 0.3D, 0.02D);
					}
				}
				if (--warden.carryTicks <= 0) {
					if (target != null && target.isAlive()
							&& !target.isPassenger()
							&& warden.distanceToSqr(target) <= 16.0D * 16.0D
							&& warden.hasLineOfSight(target)) {
						// LIFT OFF.
						target.startRiding(warden, true);
						warden.setGrabPhase(EndWardenEntity.GRAB_CARRY);
						warden.carryTicks = warden.isEnraged() ? 50 : 70;
						warden.carryDamage = 0.0F;
						warden.playSound(SoundEvents.SHULKER_BOX_OPEN, 1.2F, 0.6F);
						if (target instanceof ServerPlayer player) {
							player.hurt(warden.damageSources().mobAttack(warden), warden.isEnraged() ? 6.0F : 4.0F);
						}
					} else {
						warden.endGrab(120);
					}
				}
			}
		}

		@Override
		public void stop() {
			if (warden.grabPhase != EndWardenEntity.GRAB_IDLE) {
				warden.endGrab(120);
			}
		}
	}

	/**
	 * RESONANCE SLAM: a two-ring ground shockwave. The first ring punishes
	 * hugging the warden, the second catches anyone who dodged only the first.
	 * Jump timing and dashes both work - on the telegraph, not after.
	 */
	private static final class ResonanceSlamGoal extends Goal {
		private final EndWardenEntity warden;
		private int timeline; // ticks since start; impact1 at 16, impact2 at 26
		private boolean rang1;
		private boolean rang2;

		ResonanceSlamGoal(EndWardenEntity warden) {
			this.warden = warden;
		}

		@Override
		public boolean canUse() {
			if (warden.slamCooldown > 0 || warden.isSlamming() || warden.isGrabbing()) return false;
			LivingEntity target = warden.getTarget();
			if (target == null) return false;
			return warden.distanceToSqr(target) <= 18.0D * 18.0D && warden.onGround();
		}

		@Override
		public void start() {
			timeline = 16;
			rang1 = false;
			rang2 = false;
			warden.setSlamming(true);
			warden.getNavigation().stop();
			warden.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.4F, 0.5F);
		}

		@Override
		public boolean canContinueToUse() {
			return timeline > 0;
		}

		@Override
		public void stop() {
			warden.setSlamming(false);
		}

		@Override
		public void tick() {
			timeline--;
			ServerLevel server = (ServerLevel) warden.level();
			if (timeline > 0) {
				// Telegraph: two creeping telegraph rings as the warden rears up.
				if (timeline % 3 == 0) {
					server.sendParticles(ParticleTypes.CLOUD,
							warden.getX(), warden.getY() + 0.2D, warden.getZ(),
							6, 2.2D, 0.05D, 2.2D, 0.01D);
					server.sendParticles(ModParticles.RESONANCE_PULSE,
							warden.getX(), warden.getY() + 0.4D, warden.getZ(),
							3, 2.8D, 0.05D, 2.8D, 0.01D);
				}
				return;
			}
			if (!rang1) {
				rang1 = true;
				detonate(server, 4.5D, warden.isEnraged() ? 12.0F : 10.0F, 0.85D);
				warden.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.3F, 0.7F);
				timeline = 10; // second wave follows
				return;
			}
			if (!rang2) {
				rang2 = true;
				detonate(server, 7.5D, warden.isEnraged() ? 8.0F : 6.0F, 0.7D);
				warden.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.1F, 0.55F);
				warden.slamCooldown = warden.isEnraged() ? 90 : 160;
				timeline = 0;
			}
		}

		private void detonate(ServerLevel server, double radius, float damage, double launch) {
			server.sendParticles(ParticleTypes.POOF,
					warden.getX(), warden.getY() + 0.3D, warden.getZ(),
					40, radius, 0.3D, radius, 0.08D);
			server.sendParticles(ParticleTypes.EXPLOSION,
					warden.getX(), warden.getY() + 0.5D, warden.getZ(),
					2, radius * 0.5D, 0.2D, radius * 0.5D, 0.0D);
			for (Player p : server.getEntitiesOfClass(Player.class,
					warden.getBoundingBox().inflate(radius), Player::isAlive)) {
				double dx = p.getX() - warden.getX();
				double dz = p.getZ() - warden.getZ();
				double distSqr = dx * dx + dz * dz;
				if (distSqr > radius * radius) continue;
				Vec3 kb = new Vec3(dx, 0, dz);
				if (kb.lengthSqr() < 0.01D) kb = new Vec3(0.5D, 0, 0);
				kb = kb.normalize();
				p.setDeltaMovement(p.getDeltaMovement().add(kb.x * launch, launch * 0.8D, kb.z * launch));
				p.hurtMarked = true;
				p.hurt(warden.damageSources().mobAttack(warden), damage);
			}
			warden.swing(InteractionHand.MAIN_HAND);
		}
	}

	/**
	 * The signature region attack, switched by attunement. Each variant keeps
	 * the same contract: telegraphed, ranged-or-area, and dodgeable.
	 */
	private static final class RegionalSpecialGoal extends Goal {
		private final EndWardenEntity warden;
		private int windup;

		RegionalSpecialGoal(EndWardenEntity warden) {
			this.warden = warden;
			setFlags(EnumSet.of(Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (windup > 0 || warden.specialCooldown > 0 || warden.isGuarding() || warden.isGrabbing()) return false;
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
				warden.specialCooldown = warden.isEnraged() ? 45 : 90;
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
				case EndesiumRegions.END_WASTES -> fireShards(server, target, 5); // dust bolt fan
				case EndesiumRegions.CHORUS_WILDS -> blinkBehind(server, target); // flank blink
				case EndesiumRegions.SHATTERED_HIGHLANDS -> galeSlam(server, target); // knockback burst
				case EndesiumRegions.VOID_MARSHES -> mireGrasp(server, target); // pull + slow
				case EndesiumRegions.LUMINOUS_GROVES -> lumenFlash(server, target); // blind + self heal
				case EndesiumRegions.ASHEN_EXPANSE -> emberNova(server, target); // ignite ring
				case EndesiumRegions.CRYSTAL_BARRENS -> fireShards(server, target, 4); // homing shards
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
			target.hurt(warden.damageSources().mobAttack(warden), 10.0F);
			target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
			server.sendParticles(ParticleTypes.PORTAL,
					warden.getX(), warden.getY() + 1.0D, warden.getZ(),
					14, 0.3D, 0.6D, 0.3D, 0.05D);
		}

		private void galeSlam(ServerLevel server, LivingEntity target) {
			server.sendParticles(ParticleTypes.POOF,
					warden.getX(), warden.getY() + 0.5D, warden.getZ(),
					24, 2.2D, 0.3D, 2.2D, 0.05D);
			for (Player p : server.getEntitiesOfClass(Player.class,
					warden.getBoundingBox().inflate(6.0D), Player::isAlive)) {
				Vec3 kb = p.position().subtract(warden.position()).normalize();
				p.setDeltaMovement(p.getDeltaMovement().add(kb.x * 1.5D, 0.85D, kb.z * 1.5D));
				p.hurtMarked = true;
				p.hurt(warden.damageSources().mobAttack(warden), warden.isEnraged() ? 8.0F : 5.0F);
			}
			warden.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.9F, 0.8F);
		}

		private void mireGrasp(ServerLevel server, LivingEntity target) {
			Vec3 pull = warden.position().subtract(target.position()).normalize().scale(1.4D);
			target.setDeltaMovement(target.getDeltaMovement().add(pull.x, 0.2D, pull.z));
			target.hurtMarked = true;
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
			server.sendParticles(ParticleTypes.SQUID_INK,
					target.getX(), target.getY() + 0.5D, target.getZ(),
					10, 0.3D, 0.3D, 0.3D, 0.02D);
		}

		private void lumenFlash(ServerLevel server, LivingEntity target) {
			server.sendParticles(ParticleTypes.END_ROD,
					warden.getX(), warden.getY() + 1.5D, warden.getZ(),
					20, 1.0D, 0.8D, 1.0D, 0.08D);
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
			warden.heal(12.0F);
			warden.playSound(SoundEvents.ALLAY_ITEM_GIVEN, 1.0F, 0.6F);
		}

		private void emberNova(ServerLevel server, LivingEntity target) {
			server.sendParticles(ParticleTypes.FLAME,
					warden.getX(), warden.getY() + 0.6D, warden.getZ(),
					40, 2.5D, 0.4D, 2.5D, 0.1D);
			for (Player p : server.getEntitiesOfClass(Player.class,
					warden.getBoundingBox().inflate(6.0D), Player::isAlive)) {
				p.hurt(warden.damageSources().mobAttack(warden), 8.0F);
				p.igniteForSeconds(8);
			}
		}

		private void suppress(ServerLevel server, LivingEntity target) {
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
			target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 160, 0));
			target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 80, 0));
			target.hurt(warden.damageSources().mobAttack(warden), 8.0F);
			server.sendParticles(ModParticles.NULL_DISTORTION,
					target.getX(), target.getY() + 1.0D, target.getZ(),
					14, 0.4D, 0.6D, 0.4D, 0.02D);
		}
	}
}
