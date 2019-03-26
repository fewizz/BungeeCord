package net.md_5.bungee.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class LegacyPacketDecoder extends ByteToMessageDecoder implements PacketDecoder {
	@Setter
	@Getter
	private NetworkState connectionState = NetworkState.LEGACY;
	@Getter
	private final Direction direction;
	@Setter
	@Getter
	private Protocol protocol;

	public LegacyPacketDecoder(Direction dir, int pv) {
		this.direction = dir;
		protocol = Protocol.byNumber(pv, ProtocolGen.PRE_NETTY);
	}
	
	public LegacyPacketDecoder(LegacyPacketDecoder lpd) {
		this.connectionState = lpd.connectionState;
		this.direction = lpd.direction;
		this.protocol = lpd.protocol;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int begin = in.readerIndex();

		try {
			int packetId = in.readUnsignedByte();

			Packet packet = protocol.createPacket(connectionState, packetId, direction);

			if (packet == null)
				throw new RuntimeException("Don't know that packet" + 
						", id: " + packetId + 
						", direction: " + direction.name() + 
						", protocol: " + protocol);
			
			//System.out.println("DEC, id: " + packetId + ", dir: " + direction.name());
			read0(in, packet);
			
			// Do it manually, because when in becomes !in.isReadable, 
			// super BTMD not sends last message immediately, so it releases bytebuf
			ctx.fireChannelRead(new PacketWrapper(packet, in.slice(begin, in.readerIndex() - begin)));
		} catch (Exception e) {// Temp. solution. //TODO
			in.readerIndex(begin);
			if (!(e instanceof IndexOutOfBoundsException))
				throw e;
		}
	}
	
	protected void read0(ByteBuf buf, Packet p) {
		p.read( buf, direction, protocol );
	}
}