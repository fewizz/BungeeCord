package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class UpdateAttribsOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(Integer.BYTES);
		int count = buf.readInt();
		while(count-- != 0) {
			skipLegacyString(buf, 64);
			buf.skipBytes(Double.BYTES);
			buf.skipBytes(buf.readShort()*(Long.BYTES*2+Double.BYTES+1));
		}
	}
}
