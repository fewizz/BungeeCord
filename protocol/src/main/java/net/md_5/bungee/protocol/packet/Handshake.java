package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.NetworkState;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
public class Handshake extends DefinedPacket {

	private int protocolVersion;
	@NonNull
	private String host;
	private int port;
	
	@NonNull
	private NetworkState requestedNetworkState;
	
	public Handshake(Handshake hs) {
		Handshake.builder()
			.protocolVersion(protocolVersion)
			.host(host)
			.port(port)
			.build();
	}

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
}
