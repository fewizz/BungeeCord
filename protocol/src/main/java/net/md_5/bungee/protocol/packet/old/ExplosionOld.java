package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class ExplosionOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(Double.BYTES*3 + Float.BYTES);
		buf.skipBytes(3*buf.readInt());
		buf.skipBytes(Float.BYTES*3);
	}
}
