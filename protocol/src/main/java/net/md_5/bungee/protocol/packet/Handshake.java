package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolGen;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.NetworkState;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Handshake extends DefinedPacket {

	private Protocol protocol;
	private String host;
	private int port;
	@Setter
	private NetworkState requestedNetworkState;

	@Override
	public void read(ByteBuf buf) {
		protocol = Protocol.byNumber(readVarInt(buf), ProtocolGen.POST_NETTY);
		host = readString(buf);
		port = buf.readUnsignedShort();
		requestedNetworkState = NetworkState.byID(readVarInt(buf));
	}

	@Override
	public void write(ByteBuf buf) {
		writeVarInt(protocol.version, buf);
		writeString(host, buf);
		buf.writeShort(port);
		writeVarInt(requestedNetworkState.getId(), buf);
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
