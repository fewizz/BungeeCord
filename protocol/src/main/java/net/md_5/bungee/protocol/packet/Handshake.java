package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.NetworkState;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Handshake extends DefinedPacket implements Cloneable {

	private int protocolVersion;
	@NonNull
	private String host;
	private int port;
	
	@NonNull
	@Setter
	private NetworkState requestedNetworkState;

	@Override
	public void read(ByteBuf buf) {
		protocolVersion = readVarInt(buf);
		host = readString(buf);
		port = buf.readUnsignedShort();
		requestedNetworkState = NetworkState.byID(readVarInt(buf));
	}

	@Override
	public void write(ByteBuf buf) {
		writeVarInt(protocolVersion, buf);
		writeString(host, buf);
		buf.writeShort(port);
		writeVarInt(requestedNetworkState.getId(), buf);
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
	
	@Override
	public Handshake clone() {
		try {
			return (Handshake) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
