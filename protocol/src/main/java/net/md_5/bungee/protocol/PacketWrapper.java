package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

public class PacketWrapper extends DefaultByteBufHolder {
	public final DefinedPacket packet;
	public final int id;
	
	public PacketWrapper(DefinedPacket p, ByteBuf buf, int id) {
		super(buf);
		this.packet = p;
		this.id = id;
	}
	
	public PacketWrapper copyPacket() {
		return new PacketWrapper(packet, content().copy(), id);
	}
	
	public void trySingleRelease() { } // old stuff
}
