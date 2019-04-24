package net.md_5.bungee.netty;

import java.net.InetSocketAddress;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.compress.PacketCompressor;
import net.md_5.bungee.compress.PacketDecompressor;
import net.md_5.bungee.protocol.NetworkState;
import net.md_5.bungee.protocol.PacketDecoder;
import net.md_5.bungee.protocol.PacketEncoder;
import net.md_5.bungee.protocol.Protocol;

public class ChannelWrapper {
	Protocol protocol;
	NetworkState networkState;
	public final Channel handle;
	@Getter
	@Setter
	private InetSocketAddress remoteAddress;

	public ChannelWrapper(Channel c, Protocol p) {
		this(c, p, p.initialNetworkState());
	}
	
	public ChannelWrapper(@NonNull Channel c, @NonNull Protocol p, @NonNull NetworkState ns) {
		this.handle = c;
		this.remoteAddress = (InetSocketAddress) this.handle.remoteAddress();
		this.protocol = p;
		this.networkState = ns;
	}

	public void setNetworkState(@NonNull NetworkState state) {
		if (getProtocol().isLegacy() && state != NetworkState.LEGACY)
			throw new RuntimeException("You can't use NetworkState other than Legacy, when protocol is legacy itself");

		this.networkState = state;
		
		(
			(PacketDecoder) handle
			.pipeline()
			.get(PipelineUtil.PACKET_DEC)
		).setNetworkState(state);
		(
			(PacketEncoder) handle
			.pipeline()
			.get(PipelineUtil.PACKET_ENC)
		).setNetworkState(state);
	}
	
	public void setPacketHandler(PacketHandler ph) {
		handle.pipeline()
			.get(HandlerBoss.class)
			.setHandler(ph);
	}

	public void setProtocol(@NonNull Protocol protocol) {
		Protocol was = getProtocol();

		PacketDecoder dec = (PacketDecoder) handle.pipeline().get(PipelineUtil.PACKET_DEC);

		if (was.generation != protocol.generation)
			throw new RuntimeException("Incompatible generation");

		PacketEncoder enc = (PacketEncoder) handle.pipeline().get(PipelineUtil.PACKET_ENC);

		dec.setProtocol(protocol);
		enc.setProtocol(protocol);
		
		this.protocol = protocol;

		if (BungeeCord.getInstance().getConfig().isProtocolChange())
			BungeeCord.getInstance().getLogger().info("[" + getRemoteAddress() + "] "
					+ "Done changing protocol of cw, from: " + was.name() + ", to: " + protocol.name());
	}
	
	public Protocol getProtocol() {
		return protocol;
	}
	
	public NetworkState getConnectionState() {
		return networkState;
	}

	public void write(Object packet) {
		handle.writeAndFlush(packet, handle.voidPromise());
	}
	
	public ChannelPipeline pipeline() {
		return handle.pipeline();
	}
	
	public EventLoop eventLoop() {
		return handle.eventLoop();
	}
	
	public ChannelFuture closeFuture() {
		return handle.closeFuture();
	}

	public void close() {
		handle.close();
	}

	public Channel getHandle() {
		return handle;
	}
	
	public boolean isActive() {
		return handle.isActive();
	}

	public void setCompressionThreshold(int compressionThreshold) {
		if (handle.pipeline().get(PacketCompressor.class) == null && compressionThreshold != -1)
			handle.pipeline().addBefore(PipelineUtil.PACKET_ENC, "compress", new PacketCompressor());
		
		if (compressionThreshold != -1)
			handle.pipeline().get(PacketCompressor.class).setThreshold(compressionThreshold);
		else
			handle.pipeline().remove("compress");
	

		if (handle.pipeline().get(PacketDecompressor.class) == null && compressionThreshold != -1)
			handle.pipeline().addBefore(PipelineUtil.PACKET_DEC, "decompress", new PacketDecompressor());
		
		if (compressionThreshold == -1)
			handle.pipeline().remove("decompress");
	}
}
