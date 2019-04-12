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

public class ModernPacketDecoder extends MessageToMessageDecoder<ByteBuf> implements PacketDecoder {
	@Getter
	private NetworkState networkState = NetworkState.HANDSHAKE;
	private final Direction direction;
	@Getter
	private Protocol protocol;
	
	private TIntObjectMap<Constructor<? extends Packet>> map;

	public ModernPacketDecoder(@NonNull Side side, @NonNull Protocol p) {
		Preconditions.checkArgument(p.isModern());
		this.direction = side.getOutboundDirection();
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
		map = protocol.getIdToConstructorUnmodifiableMap(networkState, direction);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int packetId = DefinedPacket.readVarInt(in);

		DefinedPacket packet = (DefinedPacket) map.get(packetId).newInstance();

		if (packet == null)
			in.skipBytes(in.readableBytes());
		else {
			packet.read(in, direction, protocol);
			
			if (in.isReadable())
				throw new RuntimeException("Did not read all bytes from packet " + packet.getClass() + " " + packetId + " cs " + networkState + " Direction " + direction);
		}
		
		firePacket(packet, in.slice(0, in.writerIndex()), ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}
}
