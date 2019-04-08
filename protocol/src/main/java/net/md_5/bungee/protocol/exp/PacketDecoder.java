package net.md_5.bungee.protocol.exp;

import io.netty.buffer.ByteBuf;

public interface PacketDecoder<DATA> {
	DATA decode(ByteBuf buf);
}
