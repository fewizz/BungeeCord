package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class SkipPacket extends DefinedPacket {
	
	public void skip(ByteBuf buf) {
		throw new UnsupportedOperationException( "Packet must implement skip method" );
	}
	
	@Override
	public final void read(ByteBuf buf) {
		skip(buf);
	}
	
	@Override
	public final void handle(AbstractPacketHandler handler) throws Exception {
	}

}
