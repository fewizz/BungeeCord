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
public class LegacyLoginRequest extends DefinedPacket implements Cloneable {
	int protocolVersion;
	String userName;
	String host;
	int port; 
	
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
	
	@Override
	public LegacyLoginRequest clone() {
		try {
			return (LegacyLoginRequest) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

}
