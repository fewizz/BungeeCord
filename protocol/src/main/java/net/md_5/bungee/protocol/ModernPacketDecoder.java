package net.md_5.bungee.protocol;

import java.util.List;

import gnu.trove.map.TIntObjectMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Getter;

public class ModernPacketDecoder extends MessageToMessageDecoder<ByteBuf> implements PacketDecoder {
	@Getter
	private NetworkState networkState = NetworkState.HANDSHAKE;
	@Getter
	private final Direction direction;
	@Getter
	private Protocol protocol;
	private TIntObjectMap<Class<? extends Packet>> map;

	public ModernPacketDecoder(Direction dir, int pv) {
		this.direction = dir;
		protocol = Protocol.byNumber(pv, ProtocolGen.POST_NETTY);
		updateMap();
	}
	
	@Override
	public void setNetworkState(NetworkState s) {
		this.networkState = s;
		updateMap();
	}
	
	@Override
	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
		updateMap();
	}
	
	private void updateMap() {
		map = protocol.getIdToClassUnmodifiableMap(networkState, direction);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		int packetId = DefinedPacket.readVarInt(in);

		Class<? extends Packet> clazz = map.get(packetId);
		
		Packet packet = null;
		if(clazz != null) {
			try {
				packet = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("For packet class, You should provide public constructor without args", e);
			}
		}
		
		if (packet == null)
			in.skipBytes(in.readableBytes());
		else {
			packet.read(in, direction, protocol);
			if (in.isReadable())
				throw new BadPacketException("Did not read all bytes from packet " + packet.getClass() + ". id: " + packetId + ", ns: " + networkState + ", direction: " + direction);
		}
		
		firePacket(packet, in.slice(0, in.capacity()), ctx);
	}
}
