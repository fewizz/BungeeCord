package net.md_5.bungee;

import java.net.InetSocketAddress;

import com.google.common.base.Preconditions;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerSwitchEvent;
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
	public final BungeeServerInfo info;
	@Getter
	private final boolean forgeServer = false;
	@Getter
	@Setter
	private long sentPingId = -1;
	
	public ServerConnection(UserConnection<?> uc, ChannelWrapper ch, BungeeServerInfo info) {
		this.user = uc;
		this.ch = ch;
		this.info = info;
		
		BungeeCord bungee = BungeeCord.getInstance();
		
		bungee.pluginManager.callEvent(new ServerSwitchEvent(uc));
		bungee.logger.info(toString()+" Connected to ["+info.getName()+"]");
		
		info.addPlayer(uc);
		ch.closeFuture().addListener(future -> {
			bungee.logger.info("["+user.getAddress()+"/"+user.getName()+"] Disconnected from ["+info.getName()+"]");
			info.removePlayer(user);
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
		ch.close();
	}

	@Override
	public void disconnect(BaseComponent reason) {
		disconnect();
	}

	@Override
	public InetSocketAddress getAddress() {
		return info.getAddress();
	}

	@Override
	public boolean isConnected() {
		return ch.isActive();
	}

	@Override
	public Unsafe unsafe() {
		return unsafe;
	}
}
