package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class PlayerPositionOld extends SkipPacket{
	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(Double.BYTES*4 + 1);
	}
}
