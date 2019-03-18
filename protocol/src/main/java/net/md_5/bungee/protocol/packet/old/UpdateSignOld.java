package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class UpdateSignOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(Integer.BYTES*2 + Short.BYTES);
		for(int i = 0; i < 4; i++) skipLegacyString(buf, 15);
	}
}
