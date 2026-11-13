package de.raindancer.hyades.fabric;

import de.raindancer.client.protocol.Wire;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** One frame on {@code rains:client}, as the game carries it: the bytes and nothing around them. */
public record RainsPayload(byte[] frame) implements CustomPacketPayload {

	public static final Type<RainsPayload> TYPE = new Type<>(Identifier.parse(Wire.CHANNEL));

	public static final StreamCodec<FriendlyByteBuf, RainsPayload> CODEC = StreamCodec.of(
			(buffer, payload) -> buffer.writeBytes(payload.frame()),
			buffer -> {
				byte[] frame = new byte[buffer.readableBytes()];
				buffer.readBytes(frame);
				return new RainsPayload(frame);
			});

	@Override
	public Type<RainsPayload> type() {
		return TYPE;
	}
}
