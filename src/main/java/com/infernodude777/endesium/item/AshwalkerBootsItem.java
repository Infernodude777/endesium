package com.infernodude777.endesium.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ashwalker Boots grant fire resistance and let the wearer stand on lava.
 *
 * <p>The ability never places a temporary block and never changes a lava
 * source into a flowing or source block. The server simply treats the liquid
 * surface as the wearer's ground while the boots are equipped.</p>
 */
public final class AshwalkerBootsItem extends ArmorItem {
	public AshwalkerBootsItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties) {
		super(material, type, properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!(entity instanceof LivingEntity living)) return;
		if (!living.getItemBySlot(EquipmentSlot.FEET).is(this)) return;

		// Server side: fire immunity so the lava can never burn the wearer.
		if (!level.isClientSide()) {
			// Long-duration silent refresh so there is never an unprotected
			// tick between applications (a 40-tick buffer under a 60-tick
			// refresh).
			living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120, 0, false, false, true));
			living.clearFire();
		}

		// BOTH sides run the surface clamp: the client predicts standing on
		// the lava and the server agrees, so walking on lava is perfectly
		// smooth instead of a rubber-band fight between the two.
		walkOnLava(living);
	}

	private static boolean isInLavaFluid(LivingEntity living) {
		if (living.isInLava()) return true;
		Level level = living.level();
		BlockPos feet = BlockPos.containing(living.getX(), living.getY(), living.getZ());
		for (int dy = -1; dy <= 1; dy++) {
			if (level.getBlockState(feet.above(dy)).is(Blocks.LAVA)) return true;
		}
		return false;
	}

	private static void walkOnLava(LivingEntity living) {
		// Shift-descend: hold Shift while in lava to sink like scaffolding. Releasing Shift stays down.
		if (living.isShiftKeyDown() && isInLavaFluid(living)) {
			var mot = living.getDeltaMovement();
			double ny = mot.y - 0.18D;
			if (ny < -0.55D) ny = -0.55D;
			living.setDeltaMovement(mot.x * 0.90D, ny, mot.z * 0.90D);
			living.resetFallDistance();
			living.setOnGround(false);
			if (living.level() instanceof ServerLevel sl && living.tickCount % 4 == 0) {
				sl.sendParticles(ParticleTypes.LAVA, living.getX(), living.getY() + 0.2D, living.getZ(), 2, 0.2D, 0.1D, 0.2D, 0.01D);
			}
			return;
		}

		Level level = living.level();

		// Probe the block directly beneath the bounding box, and the feet
		// block itself: both cover standing-on-surface and sinking-in cases.
		BlockPos belowFeet = BlockPos.containing(
				living.getX(), living.getBoundingBox().minY - 0.08D, living.getZ());
		BlockState belowState = level.getBlockState(belowFeet);

		if (belowState.is(Blocks.LAVA)) {
			surfaceOnto(living, belowFeet.getY() + 1.0D);
			return;
		}

		// Sunk deeper: find the lava surface anywhere up to two blocks up.
		BlockPos feet = BlockPos.containing(living.getX(), living.getY(), living.getZ());
		for (int dy = 0; dy <= 2; dy++) {
			BlockPos probe = feet.above(dy);
			if (level.getBlockState(probe).is(Blocks.LAVA)
					&& !level.getBlockState(probe.above()).is(Blocks.LAVA)) {
				surfaceOnto(living, probe.getY() + 1.0D);
				return;
			}
		}
	}

	private static void surfaceOnto(LivingEntity living, double surfaceY) {
		// Only correct the position while the wearer is at or near the
		// surface; deep dives (lava falls) are intentionally not rescued so
		// the boots reward careful walking rather than swimming.
		if (living.getY() > surfaceY + 0.35D || living.getY() < surfaceY - 1.25D) return;

		double vy = living.getDeltaMovement().y;
		living.setPos(living.getX(), Math.max(living.getY(), surfaceY), living.getZ());
		if (vy < 0.0D) {
			living.setDeltaMovement(living.getDeltaMovement().x, 0.0D, living.getDeltaMovement().z);
		}
		living.setOnGround(true);
		living.resetFallDistance();
		if (living.level() instanceof ServerLevel server) {
			server.sendParticles(ParticleTypes.SMOKE,
					living.getX(), surfaceY + 0.1D, living.getZ(), 1, 0.15D, 0.02D, 0.15D, 0.002D);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("While worn: fire resistance").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Stand on lava — Shift to sink like scaffolding").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.literal("Full ash set: permanent Strength I (Volcanic Heart)").withStyle(ChatFormatting.GRAY));
	}
}