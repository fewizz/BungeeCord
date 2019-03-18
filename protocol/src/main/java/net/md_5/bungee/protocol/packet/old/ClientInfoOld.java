package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class ClientInfoOld extends SkipPacket {
	
	@Override
	public void skip(ByteBuf buf) {
		skipLegacyString(buf, 7);
		buf.skipBytes(4);
	}

}
