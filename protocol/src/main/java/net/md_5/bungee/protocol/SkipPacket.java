package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;

public interface SkipPacket extends Packet {
	
	void skip(ByteBuf buf);
	
	@Override
	default void read(ByteBuf buf) {
		skip(buf);
	}
	
	@Override
	default void write(ByteBuf buf) {
		throw new RuntimeException();
	}

}
