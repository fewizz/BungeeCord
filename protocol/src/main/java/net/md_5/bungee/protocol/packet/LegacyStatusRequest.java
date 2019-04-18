package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.LegacyPacketDecoder;
import net.md_5.bungee.protocol.Protocol;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Builder
public class LegacyStatusRequest extends DefinedPacket {
	String branding;
	int protocolVersion;
	String host;
	int port;
	
	@Override
	public void read(ByteBuf buf) {
		try {
			protocolVersion = buf.readByte();
			buf.skipBytes(1);
			branding = readLegacyString(buf, 255);
			buf.skipBytes(2); //len
			protocolVersion = buf.readUnsignedByte();
	
			host = readLegacyString(buf, 255);
			port = buf.readInt();
		} catch(Exception e) {
			throw LegacyPacketDecoder.OMT;
		}
	}
	
	@Override
	public void write(ByteBuf buf, Direction dir, Protocol p) {
		buf.writeByte(1);
		if(p.olderOrEqual(Protocol.MC_1_5_2))
			return;
		buf.writeByte(0xFA);
		writeLegacyString(branding, buf);
		buf.writeShort(7 + 2*host.length());
		buf.writeByte(protocolVersion);
		writeLegacyString(host, buf);
		buf.writeInt(port);
	}
	
	
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
