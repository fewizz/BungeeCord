package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class SetPlayerTeam extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		skipLegacyString(buf, 16);
		int v = buf.readByte();
		
		if(v == 0 || v == 2) {
			skipLegacyString(buf, 32);
			skipLegacyString(buf, 16);
			skipLegacyString(buf, 16);
			buf.skipBytes(1);
		}
		
		if(v == 0 || v == 3 || v == 4) {
			int c = buf.readShort();
			while(c-- != 0) skipLegacyString(buf, 16);
		}
	}
}
