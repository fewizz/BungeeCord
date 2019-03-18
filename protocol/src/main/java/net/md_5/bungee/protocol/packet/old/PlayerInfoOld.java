package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.protocol.SkipPacket;

@EqualsAndHashCode(callSuper=false)
@Data
public class PlayerInfoOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		skipLegacyString(buf, 16);
		buf.skipBytes(1 + Short.BYTES);
	}

}
