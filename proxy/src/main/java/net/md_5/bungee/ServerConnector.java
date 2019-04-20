package net.md_5.bungee;

import java.util.Locale;

import com.google.common.base.Preconditions;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.util.QuietException;

public abstract class ServerConnector<UC extends UserConnection<?>> extends PacketHandler {
	protected final UC user;
	protected ServerConnection server;
	@Getter
	protected final BungeeServerInfo target;
	@Getter
	private final Promise<ServerConnection> loginFuture;
	
	public ServerConnector(ChannelWrapper channel, UC user, BungeeServerInfo info) {
		super(channel);
		this.user = user;
		this.target = info;
		this.loginFuture = new DefaultPromise<>(ch.getHandle().eventLoop());
	}
	
	protected boolean ipForward() {
		return target
			.getIpForward() != null ?
				target.getIpForward()
				:
				BungeeCord.getInstance().config.isIpForward()
		;
	}
	
	protected boolean forgeSupport() {
		return getTarget()
			.getForgeSupport() != null ? 
				target.getForgeSupport() 
				:
				BungeeCord.getInstance().config.isForgeSupport()
		;
	}
	
	@Override
	public String toString() {
		return "["+user.getAddress()+"/"+user.getName()+"] [SC]";
	}
	
	@Override
	public void handle(Kick kick) throws Exception {
		ServerInfo def = user.updateAndGetNextServer(target);
		ServerKickEvent event = new ServerKickEvent(
			user,
			target,
			user.getPendingConnection().isLegacy() ? 
				TextComponent.fromLegacyText(kick.getMessage())
				:
				ComponentSerializer.parse(kick.getMessage()),
			def,
			ServerKickEvent.State.CONNECTING
		);
		if (event.getKickReason().toLowerCase(Locale.ROOT).contains("outdated") && def != null) {
			// Pre cancel the event if we are going to try another server
			event.setCancelled(true);
		}
		ProxyServer.getInstance().getPluginManager().callEvent(event);
		if (event.isCancelled() && event.getCancelServer() != null) {
			user.connect(event.getCancelServer(), ServerConnectEvent.Reason.KICK_REDIRECT);
			throw CancelSendSignal.INSTANCE;
		}

		String message = ProxyServer.getInstance().getTranslation("connect_kick", target.getName(), event.getKickReason());
		if (user.isDimensionChange())
			user.disconnect(message);
		else
			user.sendMessage(message);

		throw CancelSendSignal.INSTANCE;
	}
	
	@Override
	public void handle(PacketWrapper packet) throws Exception {
		if (packet.packet == null)
			throw new QuietException("Unexpected packet received during server login process!\n" +
				"id, cs: " + ch.getConnectionState().name());
	}
	
	@Override
	public void exception(Throwable t) throws Exception {
		String message = "Exception Connecting:" + Util.exception(t);
		
		if (user.getServer() == null)
			user.disconnect(message);
		else
			user.sendMessage(ChatColor.RED + message);
	}
	
	protected void finish() {
		Preconditions.checkNotNull(server);
		loginFuture.setSuccess(server);
	}

}
