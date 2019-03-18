package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.protocol.SkipPacket;

@EqualsAndHashCode(callSuper=false)
@Data
public class SpawnPositionOld extends SkipPacket {

	@Override
	public void skip(ByteBuf buf) {
		buf.skipBytes(Integer.BYTES*3);
	}

}
