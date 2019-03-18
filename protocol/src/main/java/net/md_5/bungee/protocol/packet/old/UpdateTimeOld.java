package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.protocol.SkipPacket;

@EqualsAndHashCode(callSuper=false)
@Data
public class UpdateTimeOld extends SkipPacket {
	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(Long.BYTES*2);
	}

}
