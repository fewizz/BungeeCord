package net.md_5.bungee.protocol;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
public class LegacyPacketDecoder extends ByteToMessageDecoder implements PacketDecoder {
	@Setter
	@Getter
	private NetworkState networkState = NetworkState.LEGACY;
	@Getter
	private final Direction direction;
	@Setter
	@Getter
	private Protocol protocol;

	public LegacyPacketDecoder(@NonNull Side side, @NonNull Protocol p) {
		Preconditions.checkArgument(p.isLegacy());
		this.direction = side.getOutboundDirection();
		this.protocol = p;
	}
	
	public LegacyPacketDecoder(LegacyPacketDecoder lpd) {
		this.networkState = lpd.networkState;
		this.direction = lpd.direction;
		this.protocol = lpd.protocol;
	}
	
	public static final RuntimeException OMT = new RuntimeException();
	ScheduledFuture<?> future = null;
	boolean scheduledRead = false;
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int begin = in.readerIndex();

		try {
			int packetId = in.readUnsignedByte();

			DefinedPacket packet = protocol.createPacket(networkState, packetId, direction);

			//System.out.println("read, id: " + packetId);
			if (packet == null)
				throw new RuntimeException("Don't know that packet" + 
						", id: " + packetId + 
						", direction: " + direction.name() + 
						", protocol: " + protocol);
			
			ctx.fireChannelRead(new PacketPreparer(packet));
			
			try {
				read0(in, packet);
			} catch(RuntimeException e) {
				if(e != OMT)
					throw e;
				
				if(future == null) {
					future = ctx.channel().eventLoop().schedule(() -> {
						scheduledRead = true;
						ctx.channel().read();
					}, 200, TimeUnit.MILLISECONDS);
				}
				if(!scheduledRead) {
					in.readerIndex(begin);
					return;
				}
			}
			
			scheduledRead = false;
			future = null;
			
			// Do it manually, because when in becomes !in.isReadable, 
			// super BTMD not sends last message immediately, so it releases bytebuf
			firePacket(packet, in.slice(begin, in.readerIndex() - begin), ctx);
		} catch (IndexOutOfBoundsException e) {// Temp. solution. //TODO
			in.readerIndex(begin);
		}
	}
	
	protected void read0(ByteBuf buf, DefinedPacket p) {
		p.read( buf, direction, protocol );
	}
}