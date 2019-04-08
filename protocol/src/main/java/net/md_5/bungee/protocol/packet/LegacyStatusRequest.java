package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.Protocol;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LegacyStatusRequest extends DefinedPacket {
	byte first;
	
	String branding;
	int protocolVersion = -1;
	String host;
	int port = -1;
	
	@Override
	public void read(ByteBuf buf) {
		try {
    		first = buf.readByte();
    		buf.skipBytes(1);
    		branding = readLegacyString(buf, 255);
    		buf.skipBytes(2); //len
    		protocolVersion = buf.readUnsignedByte();
    		host = readLegacyString(buf, 255);
    		port = buf.readInt();
		}
		catch(Exception e) { }
	}
	
	@Override
	public void write(ByteBuf buf, Direction dir, Protocol p) {
		if(p.olderOrEqual(Protocol.MC_1_3))
			return;
		buf.writeByte(1);
		if(p.olderOrEqual(Protocol.MC_1_5_2))
			return;
		buf.writeByte(0xFA);
		writeLegacyString(branding, buf);
		buf.writeShort(host.length() + 7);
		buf.writeByte(protocolVersion);
		writeLegacyString(host, buf);
		buf.writeInt(port);
	}
	
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
