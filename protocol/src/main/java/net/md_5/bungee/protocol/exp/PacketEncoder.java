package net.md_5.bungee.protocol.exp;

import io.netty.buffer.ByteBuf;

public interface PacketEncoder<DATA> {
	void encode(DATA data, ByteBuf buf);
}
