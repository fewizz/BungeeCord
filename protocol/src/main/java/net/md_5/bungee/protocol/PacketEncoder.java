package net.md_5.bungee.protocol;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.NonNull;

public class PacketEncoder extends MessageToByteEncoder<DefinedPacket> {
	@Getter
	@NonNull
	private NetworkState networkState;
	private final Direction direction;
	@Getter
	@NonNull
	private Protocol protocol;

	private TObjectIntMap<Class<? extends Packet>> map = new TObjectIntHashMap<>();
	
	public PacketEncoder(@NonNull NetworkState state, @NonNull Side side, @NonNull Protocol p) {
		this.networkState = state;
		this.direction = side.getInboundDirection();
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
		map = protocol.getClassToIdUnmodifiableMap(networkState, direction);
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, DefinedPacket msg, ByteBuf out) throws Exception {
		int packetID = -1;
		try {
			packetID = map.get(msg.getClass());
		} catch (Exception e) {
			throw new RuntimeException("Error while retrieving id of packet " + msg.getClass().getName(), e);
		}
		// System.out.println("ENC, id: " + packetID + ", dir: " + direction.name());

		if (protocol.isModern())
			DefinedPacket.writeVarInt(packetID, out);
		else
			out.writeByte(packetID);

		msg.write(out, direction, protocol);
	}
}
