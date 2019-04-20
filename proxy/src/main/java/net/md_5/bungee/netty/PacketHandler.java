package net.md_5.bungee.netty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.protocol.PacketPreparer;
import net.md_5.bungee.protocol.PacketWrapper;

@AllArgsConstructor
public abstract class PacketHandler extends net.md_5.bungee.protocol.AbstractPacketHandler {
	@Getter
	@NonNull
	protected final ChannelWrapper ch;
	
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

	@Deprecated
	public void connected(ChannelWrapper channel) throws Exception {
	}
	
	public void connected() throws Exception {
		connected(ch);
	}

	@Deprecated
	public void disconnected(ChannelWrapper channel) throws Exception {
	}
	
	public void disconnected() throws Exception {
		disconnected(ch);
	}

	@Deprecated
	public void writabilityChanged(ChannelWrapper channel) throws Exception {
	}
	
	public void writabilityChanged() throws Exception {
		writabilityChanged(ch);
	}
}
