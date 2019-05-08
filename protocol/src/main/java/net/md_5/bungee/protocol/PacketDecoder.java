package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

public interface PacketDecoder extends ChannelInboundHandler {
	public Protocol getProtocol();
	public void setProtocol(Protocol pv);
	public NetworkState getNetworkState();
	public void setNetworkState(NetworkState p);
	public void setTrace(boolean b);
	public boolean isTrace();
	public Side getSide();
	
	default void firePacket(DefinedPacket p, ByteBuf buf, ChannelHandlerContext ctx, int id) {
		if(isTrace())
			System.out.println(info(p, id));
		int was = buf.refCnt();
		buf.retain(); // for comp.
		ctx.fireChannelRead(new PacketWrapper(p, buf, id));
		int dec = buf.refCnt() - was;
		if(dec > 0)
			buf.release(dec);
		else if(dec < 0)
			throw new RuntimeException("Packet's ByteBuf was decreased more than one time");
	}
	
	default String info(Packet p, int id) {
		return
			"protocol: " + getProtocol().name() +
			", ns: " + getNetworkState().name() + 
			", side: " + getSide().name() +
			", id: " + id +
			(p != null ? "class: " + p.getClass() : "");
	}
}
