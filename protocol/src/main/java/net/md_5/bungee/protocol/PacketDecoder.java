package net.md_5.bungee.protocol;

import io.netty.channel.ChannelHandler;

public interface PacketDecoder extends ChannelHandler {
	public Protocol getProtocolVersion();
	public void setProtocolVersion(Protocol pv);
	public NetworkState getConnectionStatus();
	public void setConnectionStatus(NetworkState p);
}
