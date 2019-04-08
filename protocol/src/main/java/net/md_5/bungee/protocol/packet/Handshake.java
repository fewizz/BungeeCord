package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.NetworkState;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolGen;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Handshake extends DefinedPacket {

	@NonNull
	private Protocol protocol;
	@NonNull
	private String host;
	@NonNull
	private int port;
	
	@NonNull
	@Setter
	private NetworkState requestedNetworkState;
	
	int version;

	@Override
	public void read(ByteBuf buf) {
		version = readVarInt(buf);
		protocol = Protocol.byNumber(version, ProtocolGen.POST_NETTY);
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
