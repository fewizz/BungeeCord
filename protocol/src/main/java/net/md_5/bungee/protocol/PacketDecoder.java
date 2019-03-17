package net.md_5.bungee.protocol;

import io.netty.channel.ChannelHandler;

public interface PacketDecoder extends ChannelHandler {
	public ProtocolVersion getProtocolVersion();
	public void setProtocolVersion(ProtocolVersion pv);
	public Protocol getProtocol();
	public void setProtocol(Protocol p);
}
