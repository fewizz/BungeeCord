package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.Packet;
import net.md_5.bungee.protocol.Protocol;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LegacyStatusRequest extends Packet {
	int payloadID = -1;
	String branding;
	int protocolVer = -1;
	String ip;
	int port;
	
	@Override
	public void read(ByteBuf buf, Direction dir, Protocol p) {
		buf.skipBytes(1);
		if(p.olderOrEqual(Protocol.MC_1_5_2))
			return;
		payloadID = buf.readUnsignedByte();
		branding = readLegacyString(buf, 255);
		buf.skipBytes(2); //len
		protocolVer = buf.readUnsignedByte();
		ip = readLegacyString(buf, 255);
		port = buf.readInt();
	}
	
	@Override
	public void write(ByteBuf buf, Direction dir, Protocol p) {
		if(p.olderOrEqual(Protocol.MC_1_5_2))
			return;
		buf.writeByte(1);
		buf.writeByte(payloadID);
		writeLegacyString(branding, buf);
		buf.writeShort(-1);
		buf.writeByte(protocolVer);
		writeLegacyString(ip, buf);
		buf.writeInt(port);
	}
	
	
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
