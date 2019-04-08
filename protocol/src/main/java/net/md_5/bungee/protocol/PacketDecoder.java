package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

public interface PacketDecoder extends ChannelInboundHandler {
	public Protocol getProtocol();
	public void setProtocol(Protocol pv);
	public Side getSide();
	public NetworkState getNetworkState();
	public void setNetworkState(NetworkState p);
	
	default void firePacket(Packet p, ByteBuf buf, ChannelHandlerContext ctx) {
		int was = buf.refCnt();
		buf.retain(); // for comp.
		ctx.fireChannelRead(new PacketWrapper(p, buf));
		int dec = buf.refCnt() - was;
		if(dec > 0)
			buf.release(dec);
		else if(dec < 0)
			throw new RuntimeException("Packet's ByteBuf was decreased more than one time");
	}
	
	default String environmentDescription() {
		return "networkState: "+getNetworkState()+", protocol: ("+getProtocol()+"), side: "+getSide();
	}
}
