package com.infernodude777.endesium.entity;

import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
 * The End Wastes sentinel. Maintains distance, observes, and strikes
 * with telegraphed lunges. Enrages below half health for a speed boost.
 */
public class ProductionVoidStalkerEntity extends Monster implements GeoEntity {
    private static final double DETECTION_RANGE_SQR = 32.0D * 32.0D;
    private static final double PREFERRED_MIN_SQR = 6.0D * 6.0D;
    private static final double PREFERRED_MAX_SQR = 10.0D * 10.0D;
    private static final double ATTACK_RANGE_SQR = 2.6D * 2.6D;
    private static final int ATTACK_COOLDOWN = 24;
    private static final int REPOSITION_COOLDOWN = 160;
    private static final int REPOSITION_FAIL_COOLDOWN = 80;
    private static final int SEARCH_TIMEOUT = 100;
    private static final int AWARE_TICKS = 15;
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(
            ProductionVoidStalkerEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.void_stalker.idle_listen");
    private static final RawAnimation OBSERVE_ANIM = RawAnimation.begin().thenLoop("animation.void_stalker.observe");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.void_stalker.walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.void_stalker.run");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.void_stalker.attack_anticipation").thenPlay("animation.void_stalker.attack_impact").thenPlay("animation.void_stalker.attack_recovery");
    private static final RawAnimation HURT_ANIM = RawAnimation.begin().thenPlay("animation.void_stalker.hurt");
    private static final RawAnimation REPOSITION_ANIM = RawAnimation.begin().thenPlay("animation.void_stalker.reposition");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlay("animation.void_stalker.death");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private State state = State.IDLE;
    private int stateTicks;
    private int attackCooldown;
    private int repositionCooldown;
    private BlockPos lastKnownPlayerPos;
    private int searchTicks;
    private boolean enraged;

    public ProductionVoidStalkerEntity(EntityType<? extends ProductionVoidStalkerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.7D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 14.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.IDLE.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) updateBehavior();
    }

    private void updateBehavior() {
        tickCombatPhase();
        if (attackCooldown > 0) attackCooldown--;
        if (repositionCooldown > 0) repositionCooldown--;
        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            lastKnownPlayerPos = target.blockPosition();
            if (state != State.SEARCH) searchTicks = 0;
        }
        if (target == null || !target.isAlive()) {
            if (state == State.SEARCH && lastKnownPlayerPos != null) {
                if (searchTicks++ >= SEARCH_TIMEOUT) {
                    lastKnownPlayerPos = null;
                    setTarget(null);
                    enterState(State.IDLE);
                    return;
                }
                if (navigation.isDone()) {
                    navigation.moveTo(lastKnownPlayerPos.getX() + 0.5D, lastKnownPlayerPos.getY(), lastKnownPlayerPos.getZ() + 0.5D, 1.0D);
                }
                return;
            }
            lastKnownPlayerPos = null;
            enterState(State.IDLE);
            return;
        }
        stateTicks++;
        lookAt(target, 30.0F, 30.0F);
        double distance = distanceToSqr(target);
        if (distance > DETECTION_RANGE_SQR) {
            setTarget(null);
            lastKnownPlayerPos = null;
            enterState(State.IDLE);
            return;
        }
        boolean los = hasLineOfSight(target);
        switch (state) {
            case IDLE -> {
                if (distance <= DETECTION_RANGE_SQR) enterState(State.AWARE);
            }
            case AWARE -> {
                if (stateTicks >= AWARE_TICKS) enterState(los ? State.OBSERVE : State.SEARCH);
            }
            case SEARCH -> {
                if (los) enterState(State.OBSERVE);
                else if (searchTicks++ >= SEARCH_TIMEOUT) {
                    lastKnownPlayerPos = null;
                    setTarget(null);
                    enterState(State.IDLE);
                }
            }
            case OBSERVE -> {
                if (!los && stateTicks > 60) { enterState(State.SEARCH); return; }
                if (distance > PREFERRED_MAX_SQR) { enterState(State.POSITION); return; }
                if (distance <= ATTACK_RANGE_SQR && attackCooldown == 0) {
                    attackCooldown = ATTACK_COOLDOWN;
                    enterState(State.ATTACK_PREP);
                    return;
                }
                if (distance < 5.0D * 5.0D && stateTicks > 40 && repositionCooldown == 0) {
                    enterState(State.REPOSITION);
                    return;
                }
                if (getHealth() < getMaxHealth() * 0.3F) { enterState(State.RETREAT); return; }
                navigation.stop();
            }
            case POSITION -> {
                if (!los && stateTicks > 60) { enterState(State.SEARCH); return; }
                if (distance <= PREFERRED_MAX_SQR && distance >= PREFERRED_MIN_SQR) enterState(State.OBSERVE);
                else if (distance > PREFERRED_MAX_SQR) navigation.moveTo(target, 1.2D);
                else if (repositionCooldown == 0 && stateTicks > 60) enterState(State.REPOSITION);
            }
            case ATTACK_PREP -> {
                navigation.stop();
                if (!los || distance > 3.5D * 3.5D) { enterState(State.POSITION); return; }
                if (stateTicks == 1 && level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ModParticles.RESONANCE_ACTIVE, getX(), getEyeY(), getZ(), 5, 0.12D, 0.12D, 0.12D, 0.01D);
                }
                if (stateTicks == 9) {
                    target.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
                    playSound(ModSounds.VOID_STALKER_ATTACK, 1.0F, 1.0F);
                }
                if (stateTicks >= 14) enterState(State.RECOVER);
            }
            case RECOVER -> {
                navigation.stop();
                if (stateTicks >= 10) enterState(getHealth() < getMaxHealth() * 0.3F ? State.RETREAT : State.OBSERVE);
            }
            case REPOSITION -> {
                navigation.stop();
                if (stateTicks >= 12) reposition(target);
            }
            case RETREAT -> {
                if (distance >= 12.0D * 12.0D) { enterState(State.OBSERVE); return; }
                if (!los && stateTicks > 60) { enterState(State.SEARCH); return; }
                Vec3 away = position().subtract(target.position()).normalize();
                Vec3 retreatDestination = position().add(away.scale(14.0D));
                navigation.moveTo(retreatDestination.x, retreatDestination.y, retreatDestination.z, 1.1D);
            }
            case HURT -> {
                navigation.stop();
                if (stateTicks >= 6) enterState(State.OBSERVE);
            }
        }
    }

    private void tickCombatPhase() {
        boolean shouldEnrage = getHealth() < getMaxHealth() * 0.5F;
        if (shouldEnrage && !enraged) {
            enraged = true;
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() + 0.10D);
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ModParticles.VOID_STALKER_TRACE, getX(), getY() + 1.0D, getZ(), 16, 0.4D, 0.6D, 0.4D, 0.03D);
                playSound(ModSounds.VOID_STALKER_ATTACK, 1.0F, 0.7F);
            }
        } else if (!shouldEnrage && enraged) {
            enraged = false;
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() - 0.10D);
        }
    }

    private void reposition(LivingEntity target) {
        Vec3 away = position().subtract(target.position()).normalize();
        Vec3 side = new Vec3(-away.z, 0.0D, away.x).normalize();
        if (side.lengthSqr() < 0.1D) side = new Vec3(1.0D, 0.0D, 0.0D);
        if (random.nextBoolean()) side = side.scale(-1.0D);
        double repositionDistance = 8.0D + random.nextInt(7);
        Vec3 destination = target.position().add(side.scale(repositionDistance));
        BlockPos ground = BlockPos.containing(destination).below();
        boolean valid = level().getBlockState(ground).isSolidRender(level(), ground)
                && level().noCollision(this, getBoundingBox().move(destination.subtract(position())))
                && !level().getBlockState(BlockPos.containing(destination)).isSolidRender(level(), BlockPos.containing(destination));
        if (valid) {
            teleportTo(destination.x, ground.getY() + 1.0D, destination.z);
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ModParticles.VOID_STALKER_TRACE, getX(), getY(), getZ(), 8, 0.3D, 0.3D, 0.3D, 0.02D);
            }
            playSound(ModSounds.VOID_STALKER_REPOSITION, 0.8F, 1.0F);
            repositionCooldown = REPOSITION_COOLDOWN;
        } else {
            repositionCooldown = REPOSITION_FAIL_COOLDOWN;
        }
        enterState(State.OBSERVE);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean wasHurt = super.hurt(source, amount);
        if (wasHurt && isAlive() && state != State.ATTACK_PREP && state != State.REPOSITION) {
            enterState(State.HURT);
        }
        return wasHurt;
    }

    @Override
    protected int getBaseExperienceReward() {
        return 5;
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(Registries.LOOT_TABLE, com.infernodude777.endesium.Endesium.id("entities/void_stalker"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VOID_STALKER_IDLE;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 300 + random.nextInt(200);
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSounds.VOID_STALKER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VOID_STALKER_DEATH;
    }

    private void enterState(State next) {
        if (state == next) return;
        state = next;
        stateTicks = 0;
        entityData.set(DATA_STATE, next.ordinal());
    }

    public State state() {
        int ordinal = entityData.get(DATA_STATE);
        return ordinal >= 0 && ordinal < State.values().length ? State.values()[ordinal] : State.IDLE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "behavior", 5, this::animation));
    }

    private <E extends ProductionVoidStalkerEntity> PlayState animation(AnimationState<E> animationState) {
        if (isDeadOrDying()) return animationState.setAndContinue(DEATH);
        State visibleState = state();
        if (visibleState == State.HURT) return animationState.setAndContinue(HURT_ANIM);
        if (visibleState == State.ATTACK_PREP || visibleState == State.RECOVER) return animationState.setAndContinue(ATTACK);
        if (visibleState == State.REPOSITION) return animationState.setAndContinue(REPOSITION_ANIM);
        if (visibleState == State.OBSERVE || visibleState == State.AWARE) return animationState.setAndContinue(OBSERVE_ANIM);
        if (animationState.isMoving()) return animationState.setAndContinue(visibleState == State.POSITION || visibleState == State.RETREAT ? RUN : WALK);
        return animationState.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("StalkerState", state.name());
        tag.putInt("AttackCooldown", attackCooldown);
        tag.putInt("RepositionCooldown", repositionCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        try {
            state = State.valueOf(tag.getString("StalkerState"));
        } catch (IllegalArgumentException ignored) {
            state = State.IDLE;
        }
        entityData.set(DATA_STATE, state.ordinal());
        attackCooldown = tag.getInt("AttackCooldown");
        repositionCooldown = tag.getInt("RepositionCooldown");
    }

    public enum State { IDLE, AWARE, OBSERVE, SEARCH, POSITION, ATTACK_PREP, RECOVER, REPOSITION, RETREAT, HURT }
}
