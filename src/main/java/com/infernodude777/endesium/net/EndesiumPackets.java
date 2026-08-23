package com.infernodude777.endesium.net;

import com.infernodude777.endesium.Endesium;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Endesium networking. The client sends an empty {@link SonicBoomPayload} when
 * the Sonic Boom key is pressed; the server validates everything (wearing the
 * Resonant Elytra, cooldown, alive) and performs the attack, so the ability is
 * fully server-authoritative.
 */
public final class EndesiumPackets {
	private EndesiumPackets() {
	}

	public record SonicBoomPayload() implements CustomPacketPayload {
		public static final Type<SonicBoomPayload> TYPE = new Type<>(Endesium.id("sonic_boom"));
		public static final StreamCodec<ByteBuf, SonicBoomPayload> STREAM_CODEC =
				StreamCodec.unit(new SonicBoomPayload());

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public static void register() {
		PayloadTypeRegistry.playC2S().register(SonicBoomPayload.TYPE, SonicBoomPayload.STREAM_CODEC);
	}
}
