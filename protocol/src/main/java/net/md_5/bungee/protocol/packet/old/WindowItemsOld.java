package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class WindowItemsOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(1);
		short count = buf.readShort();
		while(count-- != 0)
			skipLegacyItemStack(buf);
	}
}
