package com.infernodude777.endesium.block;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.registry.ModBlockEntities;
import com.infernodude777.endesium.registry.ModBlocks;
import com.infernodude777.endesium.registry.ModItems;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.resonance.ResonanceManager;
import com.infernodude777.endesium.resonance.ResonanceType;
import com.infernodude777.endesium.state.PostDragonState;
import com.infernodude777.endesium.world.EndRuinVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.sounds.SoundSource;
import com.infernodude777.endesium.registry.ModSounds;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The persistent state behind every End Ruin mechanism and the Shattered
 * Spire core. The variant is chosen at generation time and stored here so that
 * activation rewards, resonance strength, and the hidden compartment all match
 * the structure the player actually found.
 */
public class ResonantMechanismBlockEntity extends BlockEntity {
	private EndRuinVariant variant = EndRuinVariant.INTACT;
	private boolean active;
	private boolean rewardClaimed;
	private int beamTicks;
	private boolean sourceRegistered;
	private Vec3 beamOrigin = Vec3.ZERO;

	public ResonantMechanismBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.RESONANT_MECHANISM, pos, state);
	}

	public void setVariant(EndRuinVariant variant) {
		this.variant = variant;
		setChanged();
	}

	public EndRuinVariant variant() {
		return variant;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ResonantMechanismBlockEntity mechanism) {
		if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
			if (mechanism.beamTicks > 0) {
				mechanism.spawnActivationBeam(serverLevel, pos);
				mechanism.beamTicks--;
			}
			// The first server tick registers immediately after a chunk load;
			// subsequent refreshes stay on the inexpensive twenty-tick heartbeat.
			if (!mechanism.sourceRegistered || level.getGameTime() % 20L == 0L) {
				mechanism.registerCurrentSource(serverLevel);
				mechanism.sourceRegistered = true;
			}
		}
	}


	private void registerCurrentSource(ServerLevel serverLevel) {
		ResonanceManager.get(serverLevel).registerSource(worldPosition,
				active ? ResonanceType.ACTIVE_MECHANISM : sourceTypeFor(serverLevel),
				active ? variant.resonanceRadius() : sourceRadiusFor(serverLevel),
				active ? variant.resonanceStrength() + 0.35F : sourceStrengthFor(serverLevel),
				active);
	}

	/**
	 * After the Dragon is defeated, dormant mechanisms awaken: ruin signals
	 * reach farther and the Resonant Archive core can be felt across the
	 * wastes. Active mechanisms are unaffected. Before the transformation the
	 * Archive core is sealed: it reads as an ordinary dormant relic so the
	 * Lens never treats it as an awakened source until the Dragon is dead.
	 */
	private ResonanceType sourceTypeFor(ServerLevel serverLevel) {
		if (variant == EndRuinVariant.ARCHIVE) {
			return PostDragonState.get(serverLevel).isTransformationActive()
					? ResonanceType.AWAKENED_ARCHIVE
					: ResonanceType.DORMANT_RELIC;
		}
		return variant.resonanceType();
	}

	private int sourceRadiusFor(ServerLevel serverLevel) {
		if (variant == EndRuinVariant.ARCHIVE) {
			return PostDragonState.get(serverLevel).isTransformationActive()
					? variant.resonanceRadius() // 512: the loudest signal in the mod
					: 96;                       // sealed: an ordinary dormant range
		}
		if (PostDragonState.get(serverLevel).isTransformationActive()) {
			return (int) (variant.resonanceRadius() * 1.5D);
		}
		return variant.resonanceRadius();
	}

	// Activation pulse: visible from a distance.
	private void emitActivationPulse(ServerLevel level, BlockPos pos) {
		level.sendParticles(ModParticles.RESONANCE_ACTIVE,
				pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
				40, 3.0D, 2.0D, 3.0D, 0.05D);
		level.playSound(null, pos, ModSounds.RESONANCE_STRIKE,
				SoundSource.BLOCKS, 1.2F, 1.0F);
	}

	private float sourceStrengthFor(ServerLevel serverLevel) {
		if (variant == EndRuinVariant.ARCHIVE) {
			return PostDragonState.get(serverLevel).isTransformationActive()
					? variant.resonanceStrength() // 1.8: the strongest signal in the mod
					: 1.0F;                      // sealed: an ordinary dormant strength
		}
		if (PostDragonState.get(serverLevel).isTransformationActive()) {
			return variant.resonanceStrength() * 1.3F;
		}
		return variant.resonanceStrength();
	}

	public boolean isActive() {
		return active;
	}

	public boolean activate(ServerPlayer player) {
		if (active || level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return false;
		if (variant == EndRuinVariant.ARCHIVE && !PostDragonState.get(serverLevel).isTransformationActive()) {
			player.displayClientMessage(Component.literal("The archive is sealed. It waits for the End to wake."), true);
			return false;
		}
		if (!ResonanceManager.get(serverLevel).activate(worldPosition, player)) return false;
		active = true;
		beamOrigin = player.getEyePosition();
		beamTicks = 12;
		emitActivationPulse(serverLevel, worldPosition);
		player.displayClientMessage(Component.literal("The mechanism wakes."), true);
		level.setBlock(worldPosition, getBlockState().setValue(ResonantMechanismBlock.ACTIVE, true), 3);
		setChanged();
		openHiddenCompartment(serverLevel);
		if (!rewardClaimed) {
			rewardClaimed = true;
			spawnReward(serverLevel);
		}
		awardAdvancements(player);
		return true;
	}

	/** Draws a brief, white, advancing line from the player's Lens to the mechanism. */
	private void spawnActivationBeam(ServerLevel serverLevel, BlockPos pos) {
		Vec3 target = Vec3.atCenterOf(pos).add(0.0D, 0.35D, 0.0D);
		float progress = 1.0F - (beamTicks / 12.0F);
		int points = Math.max(1, (int) Math.ceil(progress * 18.0F));
		for (int i = 0; i < points; i++) {
			float along = points == 1 ? progress : (float) i / 17.0F;
			Vec3 point = beamOrigin.lerp(target, Math.min(1.0F, along));
			serverLevel.sendParticles(ModParticles.RESONANCE_BEAM, point.x, point.y, point.z,
					1, 0.0D, 0.0D, 0.0D, 0.0D);
		}
	}

	private void openHiddenCompartment(ServerLevel serverLevel) {
		// Ruins can face any cardinal direction; inspect all four local sides
		// instead of assuming the old east/west-only layout.
		for (int[] direction : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
			BlockPos panel = worldPosition.offset(direction[0] * 5, 0, direction[1] * 5);
			BlockPos barrel = worldPosition.offset(direction[0] * 4, 0, direction[1] * 4);
			BlockState panelState = serverLevel.getBlockState(panel);
			if ((panelState.is(Blocks.END_STONE_BRICKS) || panelState.is(ModBlocks.END_GRAY))
					&& serverLevel.getBlockState(barrel).is(Blocks.BARREL)) {
				serverLevel.setBlock(panel, Blocks.AIR.defaultBlockState(), 3);
				serverLevel.sendParticles(com.infernodude777.endesium.particle.ModParticles.RESONANCE_ACTIVE,
						panel.getX() + 0.5D, panel.getY() + 0.5D, panel.getZ() + 0.5D, 6,
						0.2D, 0.2D, 0.2D, 0.02D);
				return;
			}
		}
	}

	private void spawnReward(ServerLevel serverLevel) {
		Vec3 drop = Vec3.atBottomCenterOf(worldPosition.above());
		int shardCount = switch (variant) {
			case FRACTURED -> 2;
			case SPIRE -> 3;
			case ARCHIVE -> 2;
			case SUNKEN -> 1;
			default -> 1;
		};
		ItemStack shard = new ItemStack(ModItems.VOID_SHARD, shardCount);
		ItemEntity shardEntity = new ItemEntity(serverLevel, drop.x, drop.y, drop.z, shard);
		shardEntity.setDeltaMovement(0.0D, 0.15D, 0.0D);
		serverLevel.addFreshEntity(shardEntity);

		// The Resonance Token is the guaranteed progression proof: it can only
		// be earned by waking a mechanism, and it gates the Echo Compass. The
		// Archive instead yields the post-Dragon Archive Sigil.
		ItemStack token = variant == EndRuinVariant.ARCHIVE
				? new ItemStack(ModItems.ARCHIVE_SIGIL, 1)
				: new ItemStack(ModItems.RESONANCE_TOKEN, 1);
		ItemEntity tokenEntity = new ItemEntity(serverLevel, drop.x, drop.y, drop.z, token);
		tokenEntity.setDeltaMovement(0.0D, 0.18D, 0.0D);
		serverLevel.addFreshEntity(tokenEntity);

		ItemStack fragment = fragmentFor(variant);
		ItemEntity fragmentEntity = new ItemEntity(serverLevel, drop.x, drop.y, drop.z, fragment);
		fragmentEntity.setDeltaMovement(0.0D, 0.15D, 0.0D);
		serverLevel.addFreshEntity(fragmentEntity);
	}

	private static ItemStack fragmentFor(EndRuinVariant variant) {
		ItemStack fragment = new ItemStack(Items.PAPER);
		switch (variant) {
			case FRACTURED -> {
				fragment.set(DataComponents.CUSTOM_NAME, Component.literal("Fractured Log").withStyle(ChatFormatting.GRAY));
				fragment.set(DataComponents.LORE, new ItemLore(List.of(
						Component.literal("\"The tower answers last. Hold the lens level and it pulls toward the far shelf.\"").withStyle(ChatFormatting.DARK_GRAY),
						Component.literal("\"Whatever stood there fell long ago. The mechanism remembers the fall.\"").withStyle(ChatFormatting.DARK_GRAY)
				)));
			}
			case SUNKEN -> {
				fragment.set(DataComponents.CUSTOM_NAME, Component.literal("Archive Fragment").withStyle(ChatFormatting.GRAY));
				fragment.set(DataComponents.LORE, new ItemLore(List.of(
						Component.literal("\"Three rings. The eye above the ring. The spire between them.\"").withStyle(ChatFormatting.DARK_GRAY),
						Component.literal("\"The archive records what the towers saw before the silence.\"").withStyle(ChatFormatting.DARK_GRAY)
				)));
			}
			case ARCHIVE -> {
				fragment.set(DataComponents.CUSTOM_NAME, Component.literal("Archive Fragment").withStyle(ChatFormatting.GRAY));
				fragment.set(DataComponents.LORE, new ItemLore(List.of(
						Component.literal("\"The archive waited for the sky to break. Now it listens.\"").withStyle(ChatFormatting.DARK_GRAY),
						Component.literal("\"What sleeps beneath the End remembers the first ring.\"").withStyle(ChatFormatting.DARK_GRAY)
				)));
			}
			case SPIRE -> {
				fragment.set(DataComponents.CUSTOM_NAME, Component.literal("Spire Fragment").withStyle(ChatFormatting.GRAY));
				fragment.set(DataComponents.LORE, new ItemLore(List.of(
						Component.literal("\"They built the towers to listen. When the last eye closed, the rings fell silent.\"").withStyle(ChatFormatting.DARK_GRAY),
						Component.literal("\"Below the broken crown, the core still holds its charge. Leave it be.\"").withStyle(ChatFormatting.DARK_GRAY)
				)));
			}
			default -> {
				fragment.set(DataComponents.CUSTOM_NAME, Component.literal("Observation Fragment").withStyle(ChatFormatting.GRAY));
				fragment.set(DataComponents.LORE, new ItemLore(List.of(
						Component.literal("\"Station three. The resonance returns when the lens is held still.\"").withStyle(ChatFormatting.DARK_GRAY),
						Component.literal("\"The tall one watches from the far shelf. It does not seem hostile.\"").withStyle(ChatFormatting.DARK_GRAY)
				)));
			}
		}
		return fragment;
	}

	/** Re-checks progression when an already-active mechanism is inspected. This
	 * lets players who activated it before a datapack reload still receive the
	 * correct advancement without duplicating the reward. */
	public void awardAdvancements(ServerPlayer player) {
		var advancements = player.server.getAdvancements();
		var first = advancements.get(Endesium.id("first_resonance"));
		// The long chain requires having already woken a ruin before the spire.
		boolean ruinKnownBefore = first != null && player.getAdvancements().getOrStartProgress(first).isDone();
		if (first != null) {
			player.getAdvancements().award(first, "activate_mechanism");
		}
		if (variant == EndRuinVariant.ARCHIVE) {
			var archive = advancements.get(Endesium.id("archive_awakened"));
			if (archive != null) {
				player.getAdvancements().award(archive, "woke_archive");
			}
			return;
		}
		if (variant == EndRuinVariant.SPIRE) {
			var whatRemains = advancements.get(Endesium.id("what_remains"));
			if (whatRemains != null) {
				player.getAdvancements().award(whatRemains, "activate_spire");
			}
			if (ruinKnownBefore) {
				var longResonance = advancements.get(Endesium.id("the_long_resonance"));
				if (longResonance != null) {
					player.getAdvancements().award(longResonance, "chain_complete");
				}
			}
			return;
		}
		if (variant == EndRuinVariant.FRACTURED) {
			var fractured = advancements.get(Endesium.id("fractured_station"));
			if (fractured != null) {
				player.getAdvancements().award(fractured, "activate_fractured");
			}
		} else if (variant == EndRuinVariant.SUNKEN) {
			var sunken = advancements.get(Endesium.id("sunken_archive"));
			if (sunken != null) {
				player.getAdvancements().award(sunken, "activate_sunken");
			}
		}
	}

	@Override
	public void setRemoved() {
		if (level instanceof ServerLevel serverLevel) {
			ResonanceManager.get(serverLevel).unregisterSource(worldPosition);
		}
		super.setRemoved();
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putString("Variant", variant.name());
		tag.putBoolean("Active", active);
		tag.putBoolean("RewardClaimed", rewardClaimed);
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		String variantName = tag.getString("Variant");
		if (!variantName.isEmpty()) {
			try {
				variant = EndRuinVariant.valueOf(variantName);
			} catch (IllegalArgumentException ignored) {
				variant = EndRuinVariant.INTACT;
			}
		}
		active = tag.getBoolean("Active");
		rewardClaimed = tag.getBoolean("RewardClaimed");
	}
}
