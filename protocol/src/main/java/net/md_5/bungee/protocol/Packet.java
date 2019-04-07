package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;

public interface Packet {
	public void read(ByteBuf buf, Direction direction, Protocol protocolVersion);
    public void write(ByteBuf buf, Direction direction, Protocol protocolVersion);
    
    public void handle(AbstractPacketHandler ph) throws Exception;
}
