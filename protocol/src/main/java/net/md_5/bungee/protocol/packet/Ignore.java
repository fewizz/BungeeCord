package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

@Data
@EqualsAndHashCode(callSuper=false)
public class Ignore extends DefinedPacket {
	@Override
	public void read(ByteBuf buf) {
		buf.skipBytes(buf.readableBytes());
	}
	
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {}
}
