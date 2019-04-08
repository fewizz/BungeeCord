package net.md_5.bungee.protocol;

import java.util.List;

import gnu.trove.map.TIntObjectMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.Getter;

public class LegacyPacketDecoder extends ByteToMessageDecoder implements PacketDecoder {
	@Getter
	private NetworkState networkState = NetworkState.LEGACY;
	@Getter
	private final Side side;
	@Getter
	private Protocol protocol;
	private TIntObjectMap<Class<? extends Packet>> map;
	
	public LegacyPacketDecoder(Side side, int pv) {
		this.side = side;
		this.protocol = (Protocol.byNumber(pv, ProtocolGen.PRE_NETTY));
		updateMap();
	}
	
	public LegacyPacketDecoder(LegacyPacketDecoder lpd) {
		this.networkState = lpd.networkState;
		this.side = lpd.side;
		this.protocol = lpd.protocol;
		updateMap();
	}
	
	@Override
	public void setNetworkState(NetworkState ns) {
		this.networkState = ns;
		updateMap();
	}
	
	@Override
	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
		updateMap();
	}
	
	private void updateMap() {
		map = protocol.getIdToClassUnmodifiableMap(networkState, side.getOutboundDirection());
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int begin = in.readerIndex();

		try {
			int packetId = in.readUnsignedByte();

			Packet packet = map.get(packetId).newInstance();

			if (packet == null)
				throw new RuntimeException("Don't know that packet"
						+ ", id: " + packetId
						+ ", env: " + environmentDescription());
			
			//System.out.println("DEC, id: " + packetId + ", dir: " + direction.name());
			read0(in, packet);
			
			// Do it manually, because when in becomes !in.isReadable, 
			// super BTMD not sends last message immediately, so it releases bytebuf
			firePacket(packet, in.slice(begin, in.readerIndex() - begin), ctx);
		} catch (IndexOutOfBoundsException e) {// Temp. solution. //TODO
			in.readerIndex(begin);
		}
	}
	
	protected void read0(ByteBuf buf, Packet p) {
		p.read( buf, side.getOutboundDirection(), protocol );
	}
}