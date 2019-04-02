package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

public class PacketWrapper extends DefaultByteBufHolder {

	public final DefinedPacket packet;
	
	public PacketWrapper(DefinedPacket p, ByteBuf buf) {
		super(buf);
		packet = p;
	}
	
	public void trySingleRelease() { } // old stuff
}
