package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

public class PacketWrapper extends DefaultByteBufHolder {

	public final Packet packet;
	
	public PacketWrapper(Packet p, ByteBuf buf) {
		super(buf);
		packet = p;
	}
}
