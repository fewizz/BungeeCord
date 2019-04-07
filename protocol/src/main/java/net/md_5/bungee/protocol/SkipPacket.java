package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;

@FunctionalInterface
public interface SkipPacket extends Packet {
	
	public void skip(ByteBuf buf);
	
	@Override
	default void read(ByteBuf buf, Direction direction, Protocol protocolVersion) {
		skip(buf);
	}
	
	@Override
	default void write(ByteBuf buf, Direction direction, Protocol protocolVersion) {
	}
	
	@Override
	default void handle(AbstractPacketHandler ph) throws Exception {	
	}

}
