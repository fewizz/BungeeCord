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
public class LegacyLoginRequest extends DefinedPacket {
	int protocolVersion;
	String userName;
	String host;
	int port; 
	
	public LegacyLoginRequest(LegacyLoginRequest lr) {
		this.protocolVersion = lr.protocolVersion;
		this.userName = lr.userName;
		this.host = lr.host;
		this.port = lr.port;
	}
	
	@Override
	public void read(ByteBuf buf) {
		protocolVersion = buf.readUnsignedByte();
		userName = readLegacyString(buf, 16);
		host = readLegacyString(buf, 255);
		port = buf.readInt();
	}
	
	@Override
	public void write(ByteBuf buf) {
		buf.writeByte(protocolVersion);
		writeLegacyString(userName, buf);
		writeLegacyString(host, buf);
		buf.writeInt(port);
	}
	
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}

}
