package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class PlayNoteBlockOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(Integer.BYTES*2 + Short.BYTES*2 + 2);
	}
}
