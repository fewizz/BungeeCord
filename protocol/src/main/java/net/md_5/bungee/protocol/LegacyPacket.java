package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;

public abstract class LegacyPacket extends DefinedPacket {
	public abstract boolean check(ByteBuf buf);
}
