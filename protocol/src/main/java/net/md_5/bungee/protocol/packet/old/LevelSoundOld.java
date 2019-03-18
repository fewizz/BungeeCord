package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class LevelSoundOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		skipLegacyString(buf, 256);
		buf.skipBytes(Integer.BYTES*3 + Float.BYTES + 1);
	}
}
