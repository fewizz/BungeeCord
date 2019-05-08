package net.md_5.bungee.protocol;

import java.lang.reflect.Constructor;
import java.util.List;

import com.google.common.base.Preconditions;

import gnu.trove.map.TIntObjectMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

public class ModernPacketDecoder extends MessageToMessageDecoder<ByteBuf> implements PacketDecoder {
	@Getter
	private NetworkState networkState = NetworkState.HANDSHAKE;
	@Getter
	private final Side side;
	@Getter
	private Protocol protocol;
	@Setter
	@Getter
	private boolean trace;
	
	private TIntObjectMap<Constructor<? extends Packet>> map;

	public ModernPacketDecoder(@NonNull Side side, @NonNull Protocol p) {
		Preconditions.checkArgument(p.isModern());
		this.side = side;
		this.protocol = p;
		updateMap();
	}
	
	public void setNetworkState(NetworkState ns) {
		this.networkState = ns;
		updateMap();
	}
	
	public void setProtocol(Protocol p) {
		this.protocol = p;
		updateMap();
	}
	
	private void updateMap() {
		map = protocol.getIdToConstructorUnmodifiableMap(networkState, side.getOutboundDirection());
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int packetId = -1;
		DefinedPacket packet = null;
		
		try {
		
		packetId = DefinedPacket.readVarInt(in);

		val constructor = map.get(packetId);
		
		try {
			if(constructor != null)
				packet = (DefinedPacket) constructor.newInstance();
		} catch(Exception e) {
			throw new RuntimeException("Can't create instance", e);
		}

		if (packet == null)
			in.skipBytes(in.readableBytes());
		else {
			packet.read(in, side.getOutboundDirection(), protocol);
			
			if (in.isReadable())
				throw new RuntimeException("Did not read all bytes from packet");
		}
		
		firePacket(
			packet,
			in.slice(0, in.writerIndex()),
			ctx,
			packetId
		);
		
		} catch(RuntimeException e) {
			throw new RuntimeException("Error while decoding/handling packet. " + info(packet, packetId), e);
		}
	}
}
