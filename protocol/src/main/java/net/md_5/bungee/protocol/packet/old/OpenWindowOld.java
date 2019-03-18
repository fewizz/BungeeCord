package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class OpenWindowOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(1);
		int v = buf.readByte();
		skipLegacyString(buf, 32);
		buf.skipBytes(2);
		if(v == 1) buf.skipBytes(Integer.BYTES);
	}
}
