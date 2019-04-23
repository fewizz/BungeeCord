package net.md_5.bungee.netty;

import java.net.InetSocketAddress;
import java.util.logging.Level;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.protocol.PacketPreparer;
import net.md_5.bungee.protocol.PacketWrapper;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
@AllArgsConstructor
@NoArgsConstructor
public class HandlerBoss extends ChannelInboundHandlerAdapter {
	private PacketHandler ph;
	
	public HandlerBoss setHandler(@NonNull PacketHandler handler) {
		ph = handler;
		return this;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ph.connected();
	}

	private ChannelWrapper cw(ChannelHandlerContext ctx) {
		return ctx.channel().attr(PipelineUtil.CHANNEL_WRAPPER).get();
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ph.disconnected();
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		ph.writabilityChanged();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HAProxyMessage) {
			HAProxyMessage proxy = (HAProxyMessage) msg;
			InetSocketAddress newAddress = new InetSocketAddress(proxy.sourceAddress(), proxy.sourcePort());

			ProxyServer.getInstance().getLogger().log(Level.FINE, "Set remote address via PROXY {0} -> {1}", new Object[] { cw(ctx).getRemoteAddress(), newAddress });

			cw(ctx).setRemoteAddress(newAddress);
			return;
		}
		
		if(msg instanceof PacketPreparer) {
			ph.prepare((PacketPreparer)msg);
			return;
		}
			
		PacketWrapper wrapper = (PacketWrapper) msg;
		boolean shouldHandle = ph.shouldHandle(wrapper);
		try {
			if(wrapper.packet != null && shouldHandle) {
				try {
					wrapper.packet.handle(ph);
				} catch (CancelSendSignal ex) {
					shouldHandle = false;
				}
			}
			
			if (shouldHandle)
				ph.handle(wrapper);
		} catch(Exception e) {
			String msg0 = "Error occured during packet handling.";
			if(wrapper.packet != null)
				msg0 += " Packet: " + wrapper.packet.getClass().getName();
			throw new Error(msg0, e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		try {
			//if(cause instanceof DecoderException) {
			//	BungeeCord.getInstance().getLogger().log(Level.WARNING, "Exception while decoding packet", cause);
			//}
			//else {
				BungeeCord.getInstance().getLogger().log(Level.WARNING, "Exception caught", cause);
			//}
			ph.exception(cause);
		} catch (Exception ex) {
			ProxyServer.getInstance().getLogger().log(Level.SEVERE, ph + " - exception processing exception", ex);
		}
	}
}
