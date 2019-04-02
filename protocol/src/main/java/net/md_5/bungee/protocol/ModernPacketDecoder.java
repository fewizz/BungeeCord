package net.md_5.bungee.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Getter;
import lombok.Setter;

public class ModernPacketDecoder extends MessageToMessageDecoder<ByteBuf> implements PacketDecoder {
	@Setter
	@Getter
	private NetworkState networkState = NetworkState.HANDSHAKE;
	private final Direction direction;
	@Setter
	@Getter
	private Protocol protocol;

	public ModernPacketDecoder(Direction dir, int pv) {
		this.direction = dir;
		protocol = Protocol.byNumber(pv, ProtocolGen.POST_NETTY);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int begin = in.readerIndex();
		int packetId = DefinedPacket.readVarInt(in);

		DefinedPacket packet = protocol.createPacket(networkState, packetId, direction);

		if (packet == null)
			in.skipBytes(in.readableBytes());
		else
			packet.read(in, direction, protocol);
		
		ByteBuf sliced = in.slice(begin, in.readerIndex() - begin);
		int was = sliced.refCnt();
		sliced.retain(100);
		ctx.fireChannelRead(new PacketWrapper(packet, sliced));
		sliced.release(sliced.refCnt() - was);

		if (in.isReadable())
			throw new BadPacketException("Did not read all bytes from packet " + packet.getClass() + " " + packetId + " cs " + networkState + " Direction " + direction);

	}
}
