package net.md_5.bungee.protocol;

import gnu.trove.map.TObjectIntMap;
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

	private TObjectIntMap<Class<? extends Packet>> map;
	
	public PacketEncoder(NetworkState ns, Direction d, Protocol p) {
		this.networkState = ns;
		this.direction = d;
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

	public PacketEncoder(@NonNull NetworkState state, @NonNull Side side, @NonNull Protocol p) {
		this.networkState = state;
		this.direction = side.getInboundDirection();
		this.protocol = p;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, DefinedPacket msg, ByteBuf out) throws Exception {
		int packetID = -1;
		try {
			packetID = map.get(msg.getClass());
		} catch (Exception e) {
			throw new RuntimeException("Can't find id of packet " + msg.getClass().getName());
		}
		// System.out.println("ENC, id: " + packetID + ", dir: " + direction.name());

		if (!protocol.isLegacy())
			DefinedPacket.writeVarInt(packetID, out);
		else
			out.writeByte(packetID);

		msg.write(out, direction, protocol);
	}
}
