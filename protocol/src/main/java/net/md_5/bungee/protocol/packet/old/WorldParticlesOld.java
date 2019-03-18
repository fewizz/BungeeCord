package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class WorldParticlesOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		skipLegacyString(buf, 64);
		buf.skipBytes(Float.BYTES*7 + Integer.BYTES);
	}
}
