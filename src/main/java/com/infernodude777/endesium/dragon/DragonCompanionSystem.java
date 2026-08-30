package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.mixin.LivingEntityJumpAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The companion dragon: place the vanilla dragon egg on the End exit fountain
 * and it will hatch, then grow from a playful baby into a large, friendly,
 * tameable mount. Feed it Ender Pearls to bond it, then hop on and steer by
 * looking - WASD to move, look up or down to climb and dive, and hold Space
 * for three seconds to launch a magic ball in the direction it faces.
 */
public final class DragonCompanionSystem {
	private DragonCompanionSystem() {
	}

	public static final EntityType<CompanionDragon> COMPANION_DRAGON = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			ResourceLocation.fromNamespaceAndPath(Endesium.MOD_ID, "companion_dragon"),
			FabricEntityTypeBuilder.create(MobCategory.CREATURE, CompanionDragon::new)
					.dimensions(EntityDimensions.fixed(4.0F, 3.0F))
					.trackRangeChunks(10)
					.build());

	/** The charged breath projectile - a small fireball that never sets blocks on fire. */
	public static final EntityType<CompanionDragonBolt> COMPANION_DRAGON_BOLT = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			ResourceLocation.fromNamespaceAndPath(Endesium.MOD_ID, "companion_dragon_bolt"),
			FabricEntityTypeBuilder.create(MobCategory.MISC, DragonCompanionSystem::createBolt)
					.dimensions(EntityDimensions.fixed(0.3125F, 0.3125F))
					.trackRangeChunks(4)
					.build());

	private static CompanionDragonBolt createBolt(EntityType<CompanionDragonBolt> type, Level level) {
		return new CompanionDragonBolt(type, level);
	}

	/** Per-dimension hatching state, kept in memory for the session. */
	private static final Map<ResourceKey<Level>, HatchState> STATES = new HashMap<>();

	public static void register() {
		FabricDefaultAttributeRegistry.register(COMPANION_DRAGON, EnderDragon.createAttributes());
		ServerTickEvents.END_SERVER_TICK.register(DragonCompanionSystem::tick);
		Endesium.LOGGER.info("Companion dragon system registered");
	}

	/**
	 * True for the actual Endesium boss dragon, false for the tameable
	 * companion. Every fight layer (assault, special attacks, arena
	 * reactions, sky) keys off "the EnderDragon in the arena", so they must
	 * never pick up a companion as their boss.
	 */
	public static boolean isBossDragon(EnderDragon dragon) {
		return !(dragon instanceof CompanionDragon);
	}

	private static void tick(MinecraftServer server) {
		for (ServerLevel level : server.getAllLevels()) {
			if (level.dimension() != Level.END) continue;
			STATES.computeIfAbsent(level.dimension(), k -> new HatchState(level)).tick(level);
		}
	}

	/** Watches the fountain column for a dragon egg, then hatches a companion. */
	static final class HatchState {
		private static final long HATCH_TICKS = 12000; // 10 minutes
		private long lastScan;
		private final Map<BlockPos, Long> eggStart = new HashMap<>();
		private boolean spawnedBaby;
		private UUID babyId;

		HatchState(ServerLevel level) {
		}

		void tick(ServerLevel level) {
			// Once the current companion is gone (died or removed), a fresh
			// egg placed on the fountain can hatch a new one.
			if (spawnedBaby && babyId != null) {
				Entity baby = level.getEntity(babyId);
				if (baby == null || !baby.isAlive()) {
					spawnedBaby = false;
					babyId = null;
				}
			}
			long now = level.getGameTime();
			if (now - lastScan >= 40) {
				lastScan = now;
				for (BlockPos egg : findEggs(level)) {
					eggStart.putIfAbsent(egg, now);
				}
			}
			eggStart.entrySet().removeIf(e ->
					!level.getBlockState(e.getKey()).is(Blocks.DRAGON_EGG));
			for (var entry : new HashMap<>(eggStart).entrySet()) {
				BlockPos pos = entry.getKey();
				if (now - entry.getValue() >= HATCH_TICKS && !spawnedBaby) {
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
					hatchedBaby(level, pos.above());
					spawnedBaby = true;
					eggStart.clear();
				}
			}
		}

		private void hatchedBaby(ServerLevel level, BlockPos at) {
			CompanionDragon dragon = COMPANION_DRAGON.create(level);
			if (dragon == null) return;
			this.babyId = dragon.getUUID();
			dragon.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, 0.0F, 0.0F);
			dragon.setPersistenceRequired();
			dragon.setCustomName(net.minecraft.network.chat.Component.literal("Ember"));
			// Her name tag is up from the moment she hatches so she's never
			// confused with the boss dragon.
			dragon.setCustomNameVisible(true);
			dragon.setHealth(dragon.getMaxHealth());
			level.addFreshEntity(dragon);
			Endesium.LOGGER.info("Dragon egg hatched into a companion at {}", at.toShortString());
		}

		private static List<BlockPos> findEggs(ServerLevel level) {
			List<BlockPos> found = new ArrayList<>();
			// The End fountain sits at x=z=0; scan a 3x3 column region around it.
			for (int dx = -1; dx <= 1; dx++) {
				for (int dz = -1; dz <= 1; dz++) {
					for (int y = level.getMaxBuildHeight() - 1; y > level.getMinBuildHeight(); y--) {
						if (level.getBlockState(new BlockPos(dx, y, dz)).is(Blocks.DRAGON_EGG)) {
							found.add(new BlockPos(dx, y, dz));
							break;
						}
					}
				}
			}
			return found;
		}
	}

	/**
	 * The tameable mount. Grows through three stages (baby, teen, adult); only
	 * an adult bonds to a player, and only a bonded dragon will let you ride
	 * it. Stage and tame state live in synched entity data so the client sees
	 * the same dragon as the server - no more reloaded adults acting like
	 * babies.
	 */
	public static class CompanionDragon extends EnderDragon {
		private static final EntityDataAccessor<Integer> DATA_STAGE =
				SynchedEntityData.defineId(CompanionDragon.class, EntityDataSerializers.INT);
		private static final EntityDataAccessor<Boolean> DATA_TAMED =
				SynchedEntityData.defineId(CompanionDragon.class, EntityDataSerializers.BOOLEAN);
		private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER =
				SynchedEntityData.defineId(CompanionDragon.class, EntityDataSerializers.OPTIONAL_UUID);

		private static final int GROW_PER_STAGE = 6000; // ~5 minutes per stage
		private static final int CHARGE_REQUIRED = 60; // hold Space 3 seconds
		private static final double RIDE_SPEED = 0.9D;
		private int growthTicks;
		private int chargeTicks;
		private int fireCooldown;

		public CompanionDragon(EntityType<? extends CompanionDragon> type, Level level) {
			super(type, level);
			setNoGravity(true);
		}

		@Override
		protected void defineSynchedData(SynchedEntityData.Builder builder) {
			super.defineSynchedData(builder);
			builder.define(DATA_STAGE, 0);
			builder.define(DATA_TAMED, false);
			builder.define(DATA_OWNER, Optional.empty());
		}

		public int getStage() {
			return this.entityData.get(DATA_STAGE);
		}

		public boolean isTamed() {
			return this.entityData.get(DATA_TAMED);
		}

		public UUID getOwnerId() {
			return this.entityData.get(DATA_OWNER).orElse(null);
		}

		private void setOwnerId(UUID ownerId) {
			this.entityData.set(DATA_OWNER, Optional.ofNullable(ownerId));
		}

		private boolean isOwner(Player player) {
			UUID ownerId = getOwnerId();
			return ownerId != null && ownerId.equals(player.getUUID());
		}

		private void setStage(int stage) {
			this.entityData.set(DATA_STAGE, stage);
		}

		private void setTamed(boolean tamed) {
			this.entityData.set(DATA_TAMED, tamed);
		}

		@Override
		public void aiStep() {
			// The vanilla dragon animates its wings from flapTime inside its own
			// aiStep, which we bypass for full flight control, so keep the
			// counter alive ourselves (the client model reads it every frame).
			this.oFlapTime = this.flapTime;
			this.flapTime += 0.15F + (float) this.getDeltaMovement().horizontalDistance() * 2.0F;

			if (this.isVehicle() && this.getControllingPassenger() instanceof Player rider && rider.isAlive()) {
				this.steerWithRider(rider);
			} else {
				// Always friendly and docile - never run the vanilla Dragon's
				// hostile arena phases, whether or not she's tamed yet. An
				// untamed adult just hovers peacefully instead of going feral.
				Vec3 v = this.getDeltaMovement();
				double bob = Math.sin(this.tickCount * 0.06D) * 0.02D;
				this.setDeltaMovement(v.x * 0.92D, v.y * 0.94D + bob, v.z * 0.92D);
				this.move(MoverType.SELF, this.getDeltaMovement());
			}
			this.growTick();
			// No explicit baseTick() here: the entity tick chain already runs
			// baseTick() exactly once per tick before aiStep().
		}

		private void growTick() {
			if (this.getStage() >= 2) return;
			this.growthTicks++;
			if (this.growthTicks >= GROW_PER_STAGE) {
				this.growthTicks = 0;
				this.setStage(this.getStage() + 1);
				this.refreshDimensions();
				this.playSound(SoundEvents.ENDER_DRAGON_AMBIENT, 0.6F, this.getStage() == 2 ? 1.0F : 1.4F);
			}
		}

		private void steerWithRider(Player rider) {
			if (this.getStage() < 2 || !this.isTamed() || !this.isOwner(rider)) {
				this.ejectPassengers();
				return;
			}
			float yaw = rider.getYRot();
			this.setYRot(yaw);
			this.setYHeadRot(yaw);
			this.yBodyRot = yaw;

			float forward = rider.zza; // W/S
			float side = rider.xxa;    // A/D
			Vec3 look = Vec3.directionFromRotation(0.0F, yaw);
			Vec3 right = new Vec3(-look.z, 0.0D, look.x);
			// Vanilla's input model: a positive xxa means "strafe left" (see
			// Entity.getInputVector), while the right vector here points to the
			// dragon's right, so negate the side input to match the rider's keys.
			Vec3 wish = look.scale(forward * RIDE_SPEED)
					.add(right.scale(-side * RIDE_SPEED * 0.6D));

			// Look up or down to climb and dive.
			float pitch = rider.getXRot();
			double vert = pitch < -20.0F ? 0.55D : pitch > 20.0F ? -0.45D : 0.0D;

			// Space: a burst of lift, and holding it three seconds charges the
			// magic ball.
			boolean space = ((LivingEntityJumpAccessor) rider).endesium$isJumping();
			if (space) {
				wish = wish.add(0.0D, 0.35D, 0.0D);
				if (this.fireCooldown <= 0) {
					this.chargeTicks++;
					if (this.chargeTicks >= CHARGE_REQUIRED) {
						this.shootMagicBall(rider.getLookAngle());
						this.chargeTicks = 0;
						this.fireCooldown = 40;
					}
				}
			} else {
				this.chargeTicks = 0;
			}

			wish = wish.add(0.0D, vert, 0.0D);
			this.setDeltaMovement(wish);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.hurtMarked = true;
		}

		private void shootMagicBall(Vec3 dir) {
			if (!(this.level() instanceof ServerLevel server)) return;
			dir = dir.normalize().scale(1.2D);
			CompanionDragonBolt ball = new CompanionDragonBolt(this.level(), this, dir);
			ball.setPos(this.getX() + dir.x * 2.0D, this.getY() + 1.5D, this.getZ() + dir.z * 2.0D);
			server.addFreshEntity(ball);
			this.playSound(SoundEvents.DRAGON_FIREBALL_EXPLODE, 1.0F, 1.0F);
		}

		@Override
		public LivingEntity getControllingPassenger() {
			return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
		}

		@Override
		public boolean canUsePortal(boolean allowSpawn) {
			// Vanilla dragons are hard-coded to never use portals. Ember is a
			// pet, so she follows her rider through nether and end portals.
			return true;
		}

		@Override
		public boolean hurt(DamageSource source, float amount) {
			if (this.isTamed()) {
				// A bonded dragon only answers to its rider: the owner, the
				// void, and /kill can hurt it, but stray mobs and other
				// players cannot.
				if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)) {
					return super.hurt(source, amount);
				}
				if (!(source.getEntity() instanceof Player player) || !this.isOwner(player)) {
					return false;
				}
			}
			return super.hurt(source, amount);
		}

		@Override
		public InteractionResult mobInteract(Player player, InteractionHand hand) {
			var stack = player.getItemInHand(hand);
			if (stack.is(Items.ENDER_PEARL)) {
				if (this.level().isClientSide) {
					player.swing(hand);
					return InteractionResult.CONSUME;
				}
				if (!this.isTamed()) {
					stack.shrink(1);
					this.setTamed(true);
					this.setOwnerId(player.getUUID());
					this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 0.7F);
					this.setCustomNameVisible(true);
					this.setPersistenceRequired();
				}
				return InteractionResult.sidedSuccess(this.level().isClientSide);
			}
			if (this.isTamed() && this.getStage() >= 2 && this.isOwner(player)) {
				if (!this.level().isClientSide) {
					if (player.isShiftKeyDown()) {
						if (this.isVehicle()) this.ejectPassengers();
					} else if (!this.isVehicle()) {
						player.startRiding(this, true);
					}
				}
				return InteractionResult.sidedSuccess(this.level().isClientSide);
			}
			return super.mobInteract(player, hand);
		}

		@Override
		public float getScale() {
			return switch (this.getStage()) {
				case 0 -> 0.5F;
				case 1 -> 0.75F;
				default -> 1.1F;
			};
		}

		@Override
		public Vec3 getPassengerRidingPosition(Entity passenger) {
			return this.position().add(0.0D, 2.0D * this.getScale(), 0.0D);
		}

		@Override
		public void addAdditionalSaveData(CompoundTag tag) {
			super.addAdditionalSaveData(tag);
			// stage/tamed ride the synched data (auto-persisted); keep the
			// growth progress so a reloaded baby keeps its timer.
			tag.putInt("EndesiumGrowthTicks", this.growthTicks);
		}

		@Override
		public void readAdditionalSaveData(CompoundTag tag) {
			super.readAdditionalSaveData(tag);
			this.growthTicks = tag.getInt("EndesiumGrowthTicks");
		}
	}
}
