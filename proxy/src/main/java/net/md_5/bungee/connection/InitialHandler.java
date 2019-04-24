package net.md_5.bungee.connection;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.logging.Level;

import javax.crypto.SecretKey;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtil;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.EncryptionRequest;

public abstract class InitialHandler extends PacketHandler implements PendingConnection {
	protected final BungeeCord bungee = BungeeCord.getInstance();
	@Setter
	@Getter
	protected boolean onlineMode = bungee.config.isOnlineMode();
	@Getter
	public final ListenerInfo listener;
	@Getter
	@Setter
	private UUID uniqueId = null;
	@Getter
	@Setter
	private UUID offlineId = null;
	public final ChannelWrapper ch;
	
	protected final Unsafe unsafe = new Unsafe() {
		@Override
		public void sendPacket(DefinedPacket packet) {
			ch.write(packet);
		}
	};
	@Getter
	private LoginResult loginProfile;
	
	public InitialHandler(Channel ch, ListenerInfo info) {
		this.ch = PipelineUtil.getChannelWrapper(ch);
		this.listener = info;
	}

	@Override
	public void exception(Throwable t) throws Exception {
		disconnect(ChatColor.RED + Util.exception(t));
	}

	@Override
	public void handle(PacketWrapper packet) throws Exception {
		Preconditions.checkState(packet.packet != null, "Unexpected packet received during login process!");
	}

	protected void ping(Callback<ProxyPingEvent> cb) {
		ServerInfo forced = AbstractReconnectHandler.getForcedHost(this);
		final String motd = (forced != null) ? forced.getMotd() : listener.getMotd();

		Callback<ServerPing> pingBack = (ServerPing result, Throwable e) -> { 
			if (e != null) {
				result = new ServerPing();
				result.setDescription(bungee.getTranslation("ping_cannot_connect"));
				bungee.logger.log(Level.WARNING, "Error pinging remote server", e);
			}

			bungee.pluginManager.callEvent(new ProxyPingEvent(InitialHandler.this, result, cb));
			
			if (bungee.getConnectionThrottle() != null)
				bungee.getConnectionThrottle().unthrottle(getAddress().getAddress());
		};

		if (forced != null && listener.isPingPassthrough()) {
			((BungeeServerInfo) forced).ping((ping, error) -> {
				if(ping != null) {
					if(!listener.isRemoteMotd())
						ping.setDescription(motd);
					if(!listener.isRemotePlayers())
						ping.setPlayers(new ServerPing.Players(listener.getMaxPlayers(), bungee.getOnlineCount(), null));
				}
				pingBack.done(ping, error);
				
			}, getProtocol());
		}
		else pingBack.done(
			new ServerPing(
				new ServerPing.Protocol(bungee.getName() + " " + bungee.getGameVersion(), getProtocol().version),
				new ServerPing.Players(listener.getMaxPlayers(), bungee.getOnlineCount(), null),
				motd,
				bungee.config.getFaviconObject())
			,
			null
		);
	}

	protected void auth(EncryptionRequest request, SecretKey sharedKey, Runnable run) throws Exception {
		String encName = URLEncoder.encode(getName(), "UTF-8");

		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		for (byte[] bit : new byte[][] { request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded() }) {
			sha.update(bit);
		}
		String encodedHash = URLEncoder.encode(new BigInteger(sha.digest()).toString(16), "UTF-8");

		String preventProxy = ((bungee.config.isPreventProxyConnections()) ? "&ip=" + URLEncoder.encode(getAddress().getAddress().getHostAddress(), "UTF-8") : "");
		String authURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + encName + "&serverId=" + encodedHash + preventProxy;

		HttpClient.get(authURL, ch.handle.eventLoop(), (result, error) -> {
			if (error == null) {
				LoginResult obj = bungee.gson.fromJson(result, LoginResult.class);
				if (obj != null && obj.getId() != null) {
					loginProfile = obj;
					uniqueId = Util.getUUID(loginProfile.getId());
					run.run();
					return;
				}
				disconnect(bungee.getTranslation("offline_mode_player"));
			} else {
				disconnect(bungee.getTranslation("mojang_fail"));
				bungee.logger.log(Level.SEVERE, "Error authenticating " + getName() + " with minecraft.net", error);
			}
		});
	}
	
	final protected void preLogin(Callback<PreLoginEvent> cb) {
		if (getName().contains(".")) {
			disconnect(bungee.getTranslation("name_invalid"));
			return;
		}

		if (getName().length() > 16) {
			disconnect(bungee.getTranslation("name_too_long"));
			return;
		}

		int limit = bungee.config.getPlayerLimit();
		if (limit > 0 && bungee.getOnlineCount() > limit) {
			disconnect(bungee.getTranslation("proxy_full"));
			return;
		}

		// If offline mode and they are already on, don't allow connect
		// We can just check by UUID here as names are based on UUID
		if (!isOnlineMode() && bungee.getPlayer(getUniqueId()) != null) {
			disconnect(bungee.getTranslation("already_connected_proxy"));
			return;
		}


		// fire pre login event
		bungee.pluginManager.callEvent(new PreLoginEvent(this, (PreLoginEvent result, Throwable error) -> {
			if (result.isCancelled()) {
				disconnect(result.getCancelReasonComponents());
				return;
			}
			
			cb.done(result, error);
		}));
	}
	
	final protected void login(Callback<LoginEvent> cb) {
		offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getName()).getBytes(Charsets.UTF_8));
		uniqueId = uniqueId == null ? offlineId : uniqueId;
				
		if (isOnlineMode()) {
			// Check for multiple connections
			// We have to check for the old name first
			ProxiedPlayer oldName = bungee.getPlayer(getName());
			if (oldName != null) {
				// TODO See #1218
				oldName.disconnect(bungee.getTranslation("already_connected_proxy"));
			}
			// And then also for their old UUID
			ProxiedPlayer oldID = bungee.getPlayer(getUniqueId());
			if (oldID != null)
				// TODO See #1218
				oldID.disconnect(bungee.getTranslation("already_connected_proxy"));
			
		} else {
			// In offline mode the existing user stays and we kick the new one
			ProxiedPlayer oldName = bungee.getPlayer(getName());
			if (oldName != null) {
				// TODO See #1218
				disconnect(bungee.getTranslation("already_connected_proxy"));
				return;
			}

		}

		// fire login event
		bungee.pluginManager.callEvent(new LoginEvent(this, (LoginEvent result, Throwable error) -> {
			if (!ch.handle.isActive())
				return;
			if (result.isCancelled()) {
				disconnect(result.getCancelReasonComponents());
				return;
			}

			cb.done(result, error);
		}));
	}
	
	protected <IH extends InitialHandler, UC extends UserConnection<IH>> void postLogin(UC userCon) {
		ch.pipeline().get(HandlerBoss.class).setHandler(new UpstreamBridge<IH, UC>(userCon));
		bungee.pluginManager.callEvent(new PostLoginEvent(userCon));
		
		bungee.logger.info(toString() + " Connected to listener(" + listener.getHost()+")");
	
		ServerInfo server;
		
		if (bungee.getReconnectHandler() != null)
			server = bungee.getReconnectHandler().getServer(userCon);
		else
			server = AbstractReconnectHandler.getForcedHost(this);
		
		if (server == null)
			server = bungee.getServerInfo(listener.getDefaultServer());

		userCon.connect(server, null, true, ServerConnectEvent.Reason.JOIN_PROXY);
	}

	@Override
	public void disconnect(String reason) {
		disconnect(TextComponent.fromLegacyText(reason));
	}

	@Override
	public void disconnect(BaseComponent reason) {
		disconnect(new BaseComponent[] { reason });
	}

	@Override
	public InetSocketAddress getAddress() {
		return ch.getRemoteAddress();
	}

	@Override
	public Unsafe unsafe() {
		return unsafe;
	}

	@Override
	public String getUUID() {
		return uniqueId.toString().replace("-", "");
	}

	@Override
	public String toString() {
		String str = "[" + getAddress();
		if(getName() != null)
			str += "/" + getName();
		str += "] [IH]";
		return str;
	}

	@Override
	public boolean isConnected() {
		return ch.handle.isActive();
	}
	
	@Override
	public Protocol getProtocol() {
		return ch.getProtocol();
	}
	
	@Override
	public String getName() {
		return loginProfile != null ? loginProfile.getName() : null;
	}
}
