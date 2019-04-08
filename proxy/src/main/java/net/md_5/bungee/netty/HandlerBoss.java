package net.md_5.bungee.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.timeout.ReadTimeoutException;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.PingHandler;
import net.md_5.bungee.protocol.OverflowPacketException;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.util.QuietException;

/**
 * This class is a primitive wrapper for {@link PacketHandler} instances tied to
 * channels to maintain simple states, and only call the required, adapted
 * methods when the channel is connected.
 */
public class HandlerBoss extends ChannelInboundHandlerAdapter {

	private ChannelWrapper channel;
	private PacketHandler handler;

	public HandlerBoss setHandler(PacketHandler handler) {
		this.handler = handler;
		return this;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (handler != null) {
			channel = new ChannelWrapper(ctx);
			if (!(handler instanceof InitialHandler || handler instanceof PingHandler))
				ProxyServer.getInstance().getLogger().log(Level.INFO, "{0} has connected", handler);
			handler.connected(channel);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (handler != null) {
			channel.markClosed();
			handler.disconnected(channel);

			if (!(handler instanceof InitialHandler || handler instanceof PingHandler))
				ProxyServer.getInstance().getLogger().log(Level.INFO, "{0} has disconnected", handler);
		}
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
		if (handler != null)
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

		if (handler == null)
			return;
		
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
		if (ctx.channel().isActive()) {
			boolean logExceptions = !(handler instanceof PingHandler);

			if (logExceptions) {
				if (cause instanceof ReadTimeoutException) {
					ProxyServer.getInstance().getLogger().log(Level.WARNING, "{0} - read timed out", handler);
				} else if (cause instanceof DecoderException && cause.getCause() instanceof OverflowPacketException) {
					ProxyServer.getInstance().getLogger().log(Level.WARNING, "{0} - overflow in packet detected! {1}", new Object[] { handler, cause.getCause().getMessage() });
				} else if (cause instanceof IOException || (cause instanceof IllegalStateException && handler instanceof InitialHandler)) {
					ProxyServer.getInstance().getLogger().log(Level.WARNING, "{0} - {1}: {2}", new Object[] { handler, cause.getClass().getSimpleName(), cause.getMessage() });
				} else if (cause instanceof QuietException) {
					ProxyServer.getInstance().getLogger().log(Level.SEVERE, "{0} - encountered exception: {1}", new Object[] { handler, cause });
				} else {
					ProxyServer.getInstance().getLogger().log(Level.SEVERE, handler + " - encountered exception", cause);
				}
			}

			if (handler != null) {
				try {
					handler.exception(cause);
				} catch (Exception ex) {
					ProxyServer.getInstance().getLogger().log(Level.SEVERE, handler + " - exception processing exception", ex);
				}
			}

			ctx.close();
		}
	}
}
