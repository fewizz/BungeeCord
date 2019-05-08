package net.md_5.bungee.protocol;

import java.io.IOException;
import java.io.OutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

public interface PacketDecoder extends ChannelInboundHandler {
	public Protocol getProtocol();
	public void setProtocol(Protocol pv);
	public NetworkState getNetworkState();
	public void setNetworkState(NetworkState p);
	public void setTrace(OutputStream b);
	public OutputStream getTrace();
	public Side getSide();
	
	default void firePacket(DefinedPacket p, ByteBuf buf, ChannelHandlerContext ctx, int id) {
		OutputStream os = getTrace();
		if(os != null) {
			try {
				os.write(("side: "+getSide().name()+", "+infoPacket(p, id) + "\n").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
			"," + infoPacket(p, id);
	}
	
	static String infoPacket(Packet p, int id) {
		return
			"id: " + id +
			(p != null ? ", class: " + p.getClass().getName() : "");
	}
}
