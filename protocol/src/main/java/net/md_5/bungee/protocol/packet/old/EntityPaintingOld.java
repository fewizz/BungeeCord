package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class EntityPaintingOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(Integer.BYTES);
		skipLegacyString(buf, 13);
		buf.skipBytes(Integer.BYTES*4);
	}
}
