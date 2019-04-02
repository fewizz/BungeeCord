package net.md_5.bungee.protocol;

import io.netty.channel.ChannelHandler;

public interface PacketDecoder extends ChannelHandler {
	public Protocol getProtocol();
	public void setProtocol(Protocol pv);
	public NetworkState getNetworkState();
	public void setNetworkState(NetworkState p);
}
