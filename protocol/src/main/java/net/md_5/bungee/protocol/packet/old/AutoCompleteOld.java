package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.SkipPacket;

public class AutoCompleteOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		skipLegacyString(buf, 32767);
	}
}
