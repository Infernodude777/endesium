package com.infernodude777.endesium.command;

import com.infernodude777.endesium.dragon.DragonAssaultHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * {@code /dragonfight} - a live readout of the Endesium dragon fight: the
 * dragon's health, how many pillar crystals remain, the current enrage level,
 * and how many void wisps the assault layer has deployed. Intended for
 * playtesting the fight's escalation curve without guessing.
 */
public final class DragonFightCommand {
	private DragonFightCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("dragonfight")
						.executes(context -> status(context.getSource()))));
	}

	private static int status(CommandSourceStack source) {
		ServerLevel end = source.getServer().getLevel(Level.END);
		if (end == null) {
			source.sendFailure(Component.literal("The End is not loaded."));
			return 0;
		}
		String snapshot = DragonAssaultHandler.snapshot(end);
		source.sendSuccess(() -> Component.literal("Dragon fight: " + snapshot), false);
		return 1;
	}
}
