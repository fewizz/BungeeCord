package net.md_5.bungee.protocol;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import gnu.trove.map.TIntObjectMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.NonNull;

public class LegacyPacketDecoder extends ByteToMessageDecoder implements PacketDecoder {
	@Getter
	private NetworkState networkState = NetworkState.LEGACY;
	@Getter
	private final Direction direction;
	@Getter
	private Protocol protocol;
	
	private TIntObjectMap<Constructor<? extends Packet>> map;

	public LegacyPacketDecoder(@NonNull Side side, @NonNull Protocol p) {
		Preconditions.checkArgument(p.isLegacy());
		this.direction = side.getOutboundDirection();
		this.protocol = p;
		updateMap();
	}
	
	public LegacyPacketDecoder(LegacyPacketDecoder lpd) {
		this.networkState = lpd.networkState;
		this.direction = lpd.direction;
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
		map = protocol.getIdToConstructorUnmodifiableMap(networkState, direction);
	}
	
	public static final RuntimeException OMT = new RuntimeException();
	public ByteBuf omtBuf;
	ScheduledFuture<?> future = null;
	boolean scheduledRead = false;
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if(!ctx.channel().isActive())
			return;
		
		int begin = in.readerIndex();

		try {
			int packetId = in.readUnsignedByte();

			Packet packet = null;
			try {
				packet = map.get(packetId).newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Can't create packet instance with id: " + packetId, e);
			}

			//System.out.println("read, id: " + packetId);
			if (packet == null)
				throw new RuntimeException("Don't know that packet" + 
						", id: " + packetId + 
						", direction: " + direction.name() + 
						", protocol: " + protocol);
			
			boolean defined = packet instanceof DefinedPacket;
			
			if(defined)
				ctx.fireChannelRead(new PacketPreparer((DefinedPacket)packet));
			
			try {
				read0(in, packet);
			} catch(RuntimeException e) {
				if(e != OMT)
					throw e;
				
				if(future == null) {
					future = ctx.channel().eventLoop().schedule(() -> {
						scheduledRead = true;
						ctx.channel().pipeline().fireChannelRead(omtBuf);
						omtBuf.release();
						omtBuf = null;
					}, 50, TimeUnit.MILLISECONDS);
				}
				if(!scheduledRead) {
					if(omtBuf != null)
						omtBuf.release();

					omtBuf = in.copy();
					in.readerIndex(begin);
					return;
				}
			}
			
			scheduledRead = false;
			future = null;
			
			// Do it manually, because when in becomes !in.isReadable, 
			// super BTMD not sends last message immediately, so it releases bytebuf
			firePacket(defined ? (DefinedPacket)packet : null, in.slice(begin, in.readerIndex() - begin), packetId, ctx);
		} catch (IndexOutOfBoundsException e) {// Temp. solution. //TODO
			in.readerIndex(begin);
		}
	}
	
	protected void read0(ByteBuf buf, Packet p) {
		p.read( buf, direction, protocol );
	}
}