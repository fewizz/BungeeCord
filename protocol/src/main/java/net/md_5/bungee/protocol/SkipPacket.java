package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper=false)
@Data
public class SkipPacket extends Packet {
	/*@FunctionalInterface
	interface Skipper {
		void skip(ByteBuf buf);
	}
	Skipper s;*/
	
	void skip(ByteBuf buf) {
		//if(s != null)
		//	s.skip(buf);
		//else
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
