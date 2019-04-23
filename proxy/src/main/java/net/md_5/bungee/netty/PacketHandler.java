package net.md_5.bungee.netty;

import lombok.AllArgsConstructor;
import net.md_5.bungee.protocol.PacketPreparer;
import net.md_5.bungee.protocol.PacketWrapper;

@AllArgsConstructor
public abstract class PacketHandler extends net.md_5.bungee.protocol.AbstractPacketHandler {
	@Override
	public abstract String toString();

	public void prepare(PacketPreparer p) {
	}
	
	public boolean shouldHandle(PacketWrapper packet) throws Exception {
		return true;
	}

	public void exception(Throwable t) throws Exception {
	}

	public void handle(PacketWrapper packet) throws Exception {
	}

	public void connected(ChannelWrapper channel) throws Exception {
		connected();
	}
	
	public void connected() throws Exception {
	}

	public void disconnected(ChannelWrapper channel) throws Exception {
		disconnected();
	}
	
	public void disconnected() throws Exception {
	}

	public void writabilityChanged(ChannelWrapper channel) throws Exception {
		writabilityChanged();
	}
	
	public void writabilityChanged() throws Exception {
	}
}
