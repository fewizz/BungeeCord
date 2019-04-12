package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;

public interface Packet {
	default void read(ByteBuf buf, Direction dir, Protocol protocol) {
		read(buf);
	}
	
	void read(ByteBuf buf);
	
	default void write(ByteBuf buf, Direction dir, Protocol protocol) {
		write(buf);
	}
	
	void write(ByteBuf buf);
}
