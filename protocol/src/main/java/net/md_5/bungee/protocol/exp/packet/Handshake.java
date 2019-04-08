package net.md_5.bungee.protocol.exp.packet;

import io.netty.buffer.ByteBuf;
import lombok.Builder;
import lombok.Data;
import net.md_5.bungee.protocol.NetworkState;
import net.md_5.bungee.protocol.exp.ModernPacketCoder;
import net.md_5.bungee.protocol.exp.ModernPacketDecoder;
import net.md_5.bungee.protocol.exp.ModernPacketEncoder;
import net.md_5.bungee.protocol.exp.ModernProtocol;
import net.md_5.bungee.protocol.exp.Protocol;

@Builder
@Data
public class Handshake {
	Protocol protocol;
	String address;
	int port;
	NetworkState requestedNetworkState;
	
	public static final ModernPacketCoder<Handshake> HANSHAKE_CODER = new ModernPacketCoder<Handshake>() {

		@Override
		public Handshake decode(ByteBuf buf) {
			int version = ModernPacketDecoder.readVarInt(buf);
			Protocol p = ModernProtocol._1_13_2;
			String address = ModernPacketDecoder.readString(buf);
			int port = buf.readUnsignedShort();
			int networkStateID = ModernPacketDecoder.readVarInt(buf);
			
			return Handshake.builder()
				.protocol(p)
				.address(address)
				.port(port)
				.requestedNetworkState(NetworkState.byID(networkStateID))
				.build();
		}

		@Override
		public void encode(Handshake data, ByteBuf buf) {
			ModernPacketEncoder.writeVarInt(buf, data.protocol.getVersion());
			ModernPacketEncoder.writeString(buf, data.address);
			buf.writeShort(data.port);
			ModernPacketEncoder.writeVarInt(buf, data.requestedNetworkState.getId());
		}
	};
}
