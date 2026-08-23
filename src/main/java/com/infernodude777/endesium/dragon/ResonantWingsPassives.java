package com.infernodude777.endesium.dragon;

import com.infernodude777.endesium.Endesium;
import com.infernodude777.endesium.particle.ModParticles;
import com.infernodude777.endesium.registry.ModItems;
import com.infernodude777.endesium.resonance.ResonanceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Server-side passive abilities of the Resonant Elytra:
 *
 * <ul>
 *   <li><b>Raised step height</b> (STEP_HEIGHT attribute to 1.0).</li>
 *   <li><b>Resonant Plating</b> — +4 armor points while the wings are worn,
 *       applied as a transient attribute modifier so it appears and disappears
 *       cleanly as the chestpiece comes on and off.</li>
 *   <li><b>Resonant Vitality</b> — three extra hearts while worn; the bonus
 *       hearts vanish with the wings, and vanilla clamps any overflow.</li>
 *   <li><b>Resonance Sense</b> — faint motes when a meaningful resonance
 *       source is near.</li>
 * </ul>
 *
 * The active Sonic Boom lives in {@link SonicBoomHandler}, the knockback
 * damping (Void Grace) lives in the LivingEntity mixin, and the doubled glide
 * speed lives in the {@code travel} injection of the same mixin.
 */
public final class ResonantWingsPassives {
	private static final ResourceLocation STEP_MODIFIER_ID = Endesium.id("resonant_step");
	private static final AttributeModifier STEP_MODIFIER = new AttributeModifier(
			STEP_MODIFIER_ID, 0.4D, AttributeModifier.Operation.ADD_VALUE);

	/** Resonant Plating: a light chestplate's worth of armor while worn. */
	private static final ResourceLocation PLATING_MODIFIER_ID = Endesium.id("resonant_plating");
	private static final AttributeModifier PLATING_MODIFIER = new AttributeModifier(
			PLATING_MODIFIER_ID, 4.0D, AttributeModifier.Operation.ADD_VALUE);

	/** Resonant Vitality: three extra hearts while the wings are worn. */
	private static final ResourceLocation VITALITY_MODIFIER_ID = Endesium.id("resonant_vitality");
	private static final AttributeModifier VITALITY_MODIFIER = new AttributeModifier(
			VITALITY_MODIFIER_ID, 6.0D, AttributeModifier.Operation.ADD_VALUE);

	private ResonantWingsPassives() {
	}

	public static void tick(ServerPlayer player, int globalTick) {
		boolean wearing = player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.RESONANT_WINGS);

		applyWhileWorn(player.getAttribute(Attributes.STEP_HEIGHT), wearing, STEP_MODIFIER_ID, STEP_MODIFIER);
		applyWhileWorn(player.getAttribute(Attributes.ARMOR), wearing, PLATING_MODIFIER_ID, PLATING_MODIFIER);
		applyWhileWorn(player.getAttribute(Attributes.MAX_HEALTH), wearing, VITALITY_MODIFIER_ID, VITALITY_MODIFIER);

		if (!wearing || globalTick % 100 != 0) return;
		if (player.level().dimension() != Level.END) return;
		ResonanceManager.Signal signal = ResonanceManager.get(player.serverLevel()).sample(player);
		if (signal.band() != ResonanceManager.Band.NONE && signal.strength() > 0.25F) {
			player.serverLevel().sendParticles(ModParticles.RESONANCE_PULSE,
					player.getX() + (player.getRandom().nextDouble() - 0.5D) * 1.6D,
					player.getY(0.8D),
					player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 1.6D,
					2, 0.2D, 0.2D, 0.2D, 0.01D);
		}
	}

	/** Adds the modifier while worn, removes it otherwise; null-safe. */
	private static void applyWhileWorn(AttributeInstance attribute, boolean wearing,
			ResourceLocation id, AttributeModifier modifier) {
		if (attribute == null) return;
		if (wearing) {
			if (!attribute.hasModifier(id)) {
				attribute.addTransientModifier(modifier);
			}
		} else if (attribute.hasModifier(id)) {
			attribute.removeModifier(id);
		}
	}
}
