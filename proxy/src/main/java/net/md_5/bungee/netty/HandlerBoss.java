package net.md_5.bungee.netty;

import java.net.InetSocketAddress;
import java.util.logging.Level;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import lombok.NonNull;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.protocol.PacketPreparer;
import net.md_5.bungee.protocol.PacketWrapper;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
public class HandlerBoss extends ChannelInboundHandlerAdapter {
	private ChannelWrapper channel;
	private PacketHandler handler;

	public HandlerBoss(@NonNull PacketHandler handler) {
		setHandler(handler);
	}
	
	public HandlerBoss setHandler(@NonNull PacketHandler handler) {
		this.handler = handler;
		return this;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		channel = new ChannelWrapper(ctx);
		handler.connected(channel);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		channel.markClosed();
		handler.disconnected(channel);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		handler.writabilityChanged(channel);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HAProxyMessage) {
			HAProxyMessage proxy = (HAProxyMessage) msg;
			InetSocketAddress newAddress = new InetSocketAddress(proxy.sourceAddress(), proxy.sourcePort());

			ProxyServer.getInstance().getLogger().log(Level.FINE, "Set remote address via PROXY {0} -> {1}", new Object[] { channel.getRemoteAddress(), newAddress });

			channel.setRemoteAddress(newAddress);
			return;
		}
		
		if(msg instanceof PacketPreparer) {
			handler.prepare((PacketPreparer)msg);
			return;
		}
			
		PacketWrapper wrapper = (PacketWrapper) msg;
		boolean shouldHandle = handler.shouldHandle(wrapper);
		try {
			if(wrapper.packet != null && shouldHandle) {
				try {
					wrapper.packet.handle(handler);
				} catch (CancelSendSignal ex) {
					shouldHandle = false;
				}
			}
			
			if (shouldHandle)
				handler.handle(wrapper);
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
			handler.exception(cause);
		} catch (Exception ex) {
			ProxyServer.getInstance().getLogger().log(Level.SEVERE, handler + " - exception processing exception", ex);
		}
	}
}
