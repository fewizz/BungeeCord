package net.md_5.bungee.connection;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;

public class PacketBuffer extends PacketHandler {
	List<ByteBuf> packets = new ArrayList<>();
	
	@Override
	public void handle(PacketWrapper packet) throws Exception {
		packets.add(packet.content().copy());
	}
	
	public void setPacketHandlerAndRereadPackets(PacketHandler ph) {
		// TODO
	}
	
	@Override
	public String toString() {
		return "[PB]";
	}

}
