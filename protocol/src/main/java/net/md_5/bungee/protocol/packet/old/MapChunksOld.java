package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class MapChunksOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		int count = buf.readShort();
		int data = buf.readInt();
		buf.skipBytes(1);
		buf.skipBytes(data);
		buf.skipBytes(count * (Integer.BYTES*2 + Short.BYTES*2));
	}

}
