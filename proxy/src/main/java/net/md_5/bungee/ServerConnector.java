package net.md_5.bungee;

import java.util.Locale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
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

@RequiredArgsConstructor
public abstract class ServerConnector extends PacketHandler {
	BungeeCord bungee = BungeeCord.getInstance();
	final UserConnection user;
	ServerConnection server;
	ChannelWrapper ch;
	@Getter
	final BungeeServerInfo target;
	
	@Override
	public void connected(ChannelWrapper channel) throws Exception {
		this.ch = channel;
		//bungee.getLogger().info("["+user.getAddress()+"/"+user.getName() + "] Connected to [" + target.getName() + "]");
	}
	
	protected boolean ipForward() {
		return target
			.getIpForward() != null ?
				target.getIpForward()
				:
				bungee.config.isIpForward()
		;
	}
	
	protected boolean forgeSupport() {
		return getTarget()
			.getForgeSupport() != null ? 
				target.getForgeSupport() 
				:
				bungee.config.isForgeSupport()
		;
	}
	
	@Override
	public String toString() {
		return "[" + user.getName() + "] <-> ServerConnector [" + target.getName() + "]";
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
		bungee.getPluginManager().callEvent(event);
		if (event.isCancelled() && event.getCancelServer() != null) {
			user.connect(event.getCancelServer(), ServerConnectEvent.Reason.KICK_REDIRECT);
			throw CancelSendSignal.INSTANCE;
		}

		String message = bungee.getTranslation("connect_kick", target.getName(), event.getKickReason());
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
				", cs: " + ch.getConnectionState().name());
	}
	
	@Override
	public void disconnected(ChannelWrapper channel) throws Exception {
		user.getPendingConnects().remove(target);
	}
	
	@Override
	public void exception(Throwable t) throws Exception {
		String message = "Exception Connecting:" + Util.exception(t);
		
		if (user.getServer() == null)
			user.disconnect(message);
		else
			user.sendMessage(ChatColor.RED + message);
	}

}
