package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class VehicleSpawnOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(Integer.BYTES*4 + 3);
		int i = buf.readInt();
		if(i > 0)
			buf.skipBytes(Short.BYTES*3);
	}
}
