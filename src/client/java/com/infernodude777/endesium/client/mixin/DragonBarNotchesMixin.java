package com.infernodude777.endesium.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

/**
 * Draws permanent phase-break notches on the Ender Dragon's boss bar at the
 * three enrage thresholds (60%, 35%, 15%). The fight communicates its phases
 * through the bar itself - when the health line crosses a notch, the next
 * phase begins - instead of flashing text on the screen.
 */
@Mixin(BossHealthOverlay.class)
abstract class DragonBarNotchesMixin {
	@Shadow
	@Final
	private Map<UUID, LerpingBossEvent> events;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;)V", at = @At("TAIL"))
	private void endesium$drawPhaseBreaks(GuiGraphics graphics, CallbackInfo ci) {
		if (this.minecraft.player == null
				|| this.minecraft.player.level().dimension() != Level.END) {
			return;
		}
		String dragonName = Component.translatable("entity.minecraft.ender_dragon").getString();
		int screenW = this.minecraft.getWindow().getGuiScaledWidth();
		int y = 12;
		for (LerpingBossEvent event : this.events.values()) {
			if (event.getName().getString().equals(dragonName)) {
				for (float notch : new float[]{0.60F, 0.35F, 0.15F}) {
					int x = screenW / 2 - 91 + (int) (182.0F * notch);
					graphics.fill(x, y, x + 1, y + 5, 0xB0101010);
					graphics.fill(x, y, x + 1, y + 1, 0x50E8E8E8);
				}
			}
			y += 19;
		}
	}
}
