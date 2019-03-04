package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StatusRequestOld extends DefinedPacket {
	int payloadID = -1;
	String branding;
	int protocolVer = -1;
	String ip;
	int port;
	
	@Override
	public void read(ByteBuf buf) {
		buf.skipBytes(1);
		payloadID = buf.readUnsignedByte();
		branding = readLegacyString(buf);
		buf.skipBytes(2); //len
		protocolVer = buf.readUnsignedByte();
		ip = readLegacyString(buf);
		port = buf.readInt();
		buf.skipBytes(buf.readableBytes());
	}
	
	@Override
	public void write(ByteBuf buf) {
		//buf.writeByte(1);
	}
	
	
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
