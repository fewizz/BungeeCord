package net.md_5.bungee;

import java.net.InetSocketAddress;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PluginMessage;

public class ServerConnection implements Server {

	@NonNull
	private final UserConnection<?> user;
	@Getter
	@NonNull
	private final ChannelWrapper ch;
	@Getter
	@NonNull
	private final BungeeServerInfo info;
	@Getter
	@Setter
	private boolean isObsolete;
	@Getter
	private final boolean forgeServer = false;
	@Getter
	@Setter
	private long sentPingId = -1;
	
	public ServerConnection(UserConnection<?> uc, ChannelWrapper ch, BungeeServerInfo info) {
		this.user = uc;
		this.ch = ch;
		this.info = info;
		
		ch.getHandle().closeFuture().addListener(future -> {
			BungeeCord.getInstance().getLogger().info("["+user.getAddress()+"/"+user.getName()+"] Disconnected from ["+info.getName()+"]");
		});
	}
	private final Unsafe unsafe = new Unsafe() {
		@Override
		public void sendPacket(DefinedPacket packet) {
			ch.write(packet);
		}
	};

	@Override
	public void sendData(String channel, byte[] data) {
		unsafe().sendPacket(new PluginMessage(channel, data, forgeServer));
	}

	@Override
	public void disconnect(String reason) {
		disconnect();
	}

	@Override
	public void disconnect(BaseComponent... reason) {
		Preconditions.checkArgument(reason.length == 0, "Server cannot have disconnect reason");
		ch.delayedClose(null);
	}

	@Override
	public void disconnect(BaseComponent reason) {
		disconnect();
	}

	@Override
	public InetSocketAddress getAddress() {
		return getInfo().getAddress();
	}

	@Override
	public boolean isConnected() {
		return !ch.isClosed();
	}

	@Override
	public Unsafe unsafe() {
		return unsafe;
	}
}
