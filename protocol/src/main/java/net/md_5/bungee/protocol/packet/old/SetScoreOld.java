package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class SetScoreOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		skipLegacyString(buf, 16);
		byte v = buf.readByte();
		if(v != 1) {
			skipLegacyString(buf, 16);
			buf.skipBytes(Integer.BYTES);
		}
	}
}
