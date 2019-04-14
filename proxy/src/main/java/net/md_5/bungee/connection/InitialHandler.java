package net.md_5.bungee.connection;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.crypto.SecretKey;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtil;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.NetworkState;
import net.md_5.bungee.protocol.PacketPreparer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolGen;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LegacyClientCommand;
import net.md_5.bungee.protocol.packet.LegacyLoginRequest;
import net.md_5.bungee.protocol.packet.LegacyStatusRequest;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginPayloadResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.util.BoundedArrayList;
import net.md_5.bungee.util.BufUtil;
import net.md_5.bungee.util.QuietException;

@RequiredArgsConstructor
public abstract class InitialHandler extends PacketHandler implements PendingConnection {
	final BungeeCord bungee = BungeeCord.getInstance();
	@Setter
	@Getter
	boolean onlineMode = bungee.config.isOnlineMode();
	
	protected ChannelWrapper ch;
	@Getter
	protected
	final ListenerInfo listener;
	
	protected final Unsafe unsafe = new Unsafe() {
		@Override
		public void sendPacket(DefinedPacket packet) {
			ch.write(packet);
		}
	};
	@Getter
	private final List<PluginMessage> relayMessages = new BoundedArrayList<>(128);
	@Getter
	protected InetSocketAddress virtualHost;
	protected String name;
	@Getter
	protected UUID uniqueId;
	@Getter
	private UUID offlineId;
	@Getter
	private LoginResult loginProfile;

	@Getter
	public Login forgeLogin;

	@Override
	public boolean shouldHandle(PacketWrapper packet) throws Exception {
		return !ch.isClosing();
	}

	@Override
	public void connected(ChannelWrapper channel) throws Exception {
		this.ch = channel;
	}

	@Override
	public void exception(Throwable t) throws Exception {
		disconnect(ChatColor.RED + Util.exception(t));
	}

	@Override
	public void handle(PacketWrapper packet) throws Exception {
		if (packet.packet == null)
			throw new QuietException("Unexpected packet received during login process! " + BufUtil.dump(packet.content(), 16));
	}

	@Override
	public void handle(PluginMessage pluginMessage) throws Exception {
		// TODO: Unregister?
		if (PluginMessage.SHOULD_RELAY.apply(pluginMessage))
			relayMessages.add(pluginMessage);
	}

	protected void ping(Callback<ProxyPingEvent> cb) {
		ServerInfo forced = AbstractReconnectHandler.getForcedHost(this);
		final String motd = (forced != null) ? forced.getMotd() : listener.getMotd();

		Callback<ServerPing> pingBack = (ServerPing result, Throwable e) -> { 
			if (e != null) {
				result = new ServerPing();
				result.setDescription(ProxyServer.getInstance().getTranslation("ping_cannot_connect"));
				bungee.getLogger().log(Level.WARNING, "Error pinging remote server", e);
			}

			bungee.getPluginManager().callEvent(new ProxyPingEvent(InitialHandler.this, result, cb));
			
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
				
			}, /*handshake.getProtocol()*/null);//TODO
		}
		else pingBack.done(
			new ServerPing(
				new ServerPing.Protocol(bungee.getName() + " " + bungee.getGameVersion(), getProtocol().version),
				new ServerPing.Players(listener.getMaxPlayers(), bungee.getOnlineCount(), null),
				motd,
				BungeeCord.getInstance().config.getFaviconObject())
			,
			null
		);
	}

	protected void loginAndFinish(EncryptionRequest request, SecretKey sharedKey) throws Exception {
		String encName = URLEncoder.encode(InitialHandler.this.getName(), "UTF-8");

		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		for (byte[] bit : new byte[][] { request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded() }) {
			sha.update(bit);
		}
		String encodedHash = URLEncoder.encode(new BigInteger(sha.digest()).toString(16), "UTF-8");

		String preventProxy = ((BungeeCord.getInstance().config.isPreventProxyConnections()) ? "&ip=" + URLEncoder.encode(getAddress().getAddress().getHostAddress(), "UTF-8") : "");
		String authURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + encName + "&serverId=" + encodedHash + preventProxy;

		HttpClient.get(authURL, ch.getHandle().eventLoop(), (result, error) -> {
			if (error == null) {
				LoginResult obj = BungeeCord.getInstance().gson.fromJson(result, LoginResult.class);
				if (obj != null && obj.getId() != null) {
					loginProfile = obj;
					name = obj.getName();
					uniqueId = Util.getUUID(obj.getId());
					login();
					return;
				}
				disconnect(bungee.getTranslation("offline_mode_player"));
			} else {
				disconnect(bungee.getTranslation("mojang_fail"));
				bungee.getLogger().log(Level.SEVERE, "Error authenticating " + getName() + " with minecraft.net", error);
			}
		});
	}

	abstract protected void login();
	
	final protected void checkPlayer(Callback<LoginEvent> cb) {
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

		offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getName()).getBytes(Charsets.UTF_8));
		
		if (uniqueId == null)
			uniqueId = offlineId;

		Callback<LoginEvent> complete = (LoginEvent result, Throwable error) -> {
			if (ch.isClosed())
				return;
			if (result.isCancelled()) {
				disconnect(result.getCancelReasonComponents());
				return;
			}


			cb.done(result, error);
		};

		// fire login event
		bungee.getPluginManager().callEvent(new LoginEvent(InitialHandler.this, complete));
	}

	@Override
	public void handle(LoginPayloadResponse response) throws Exception {
		disconnect("Unexpected custom LoginPayloadResponse");
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
		return "[" + ((getName() != null) ? getName() : getAddress()) + "] <-> InitialHandler";
	}

	@Override
	public boolean isConnected() {
		return !ch.isClosed();
	}
}
