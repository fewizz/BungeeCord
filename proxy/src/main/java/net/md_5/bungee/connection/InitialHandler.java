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
import lombok.val;
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
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.NetworkState;
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
public class InitialHandler extends PacketHandler implements PendingConnection {
	private final BungeeCord bungee;
	private ChannelWrapper ch;
	@Getter
	private final ListenerInfo listener;
	@Getter
	private Handshake handshake;
	@Getter
	private LegacyLoginRequest legacyLoginRequest;
	@Getter
	private Protocol protocol;
	@Getter
	private LoginRequest loginRequest;
	private EncryptionRequest request;
	@Getter
	private final List<PluginMessage> relayMessages = new BoundedArrayList<>(128);
	@Getter
	@Setter
	private PluginMessage legacyForgeResponeMessage;
	private State thisState = State.HANDSHAKE;
	private final Unsafe unsafe = new Unsafe() {
		@Override
		public void sendPacket(DefinedPacket packet) {
			ch.write(packet);
		}
	};
	@Getter
	private boolean onlineMode = BungeeCord.getInstance().config.isOnlineMode();
	@Getter
	private InetSocketAddress virtualHost;
	private String name;
	@Getter
	private UUID uniqueId;
	@Getter
	private UUID offlineId;
	@Getter
	private LoginResult loginProfile;
	@Getter
	private String extraDataInHandshake = "";
	@Getter
	public Login forgeLogin;

	@Override
	public boolean shouldHandle(PacketWrapper packet) throws Exception {
		return !ch.isClosing();
	}

	private enum State {
		HANDSHAKE, STATUS, PING, USERNAME, ENCRYPT, FINISHED;
		
		void shouldBe(State s) {
			Preconditions.checkState(s == this, "State is "+this.name()+", should be "+s.name());
		}
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

	@Override
	public void handle(StatusRequest statusRequest) throws Exception {
		thisState.shouldBe(State.STATUS);

		ServerInfo forced = AbstractReconnectHandler.getForcedHost(this);
		final String motd = (forced != null) ? forced.getMotd() : listener.getMotd();

		ServerPing def = new ServerPing(
			new ServerPing.Protocol(bungee.getName() + " " + bungee.getGameVersion(), getProtocol().version),
			new ServerPing.Players(listener.getMaxPlayers(), bungee.getOnlineCount(), null),
			motd,
			bungee.config.getFaviconObject()
		);
		
		Callback<ServerPing> pingBack = (ServerPing result, Throwable e) -> { 
			if (e != null) {
				result = new ServerPing();
				result.setDescription(bungee.getTranslation("ping_cannot_connect"));
			}

			Callback<ProxyPingEvent> callback = (ProxyPingEvent pingResult, Throwable error) -> {
				if(pingResult.response.getPlayers() == null || !listener.isRemotePlayers()) {
					pingResult.response.setPlayers(def.getPlayers());
				}
				
				if(pingResult.response.getDescription() == null || !listener.isRemoteMotd())
					pingResult.response.setDescriptionComponent(def.getDescriptionComponent());
				
				if (isLegacy()) {
					val r = Kick.StatusResponce.builder();
					r.motd(pingResult.response.getDescription());
					r.max(pingResult.response.getPlayers().getMax());
					r.players(pingResult.response.getPlayers().getOnline());
					r.mcVersion(pingResult.getConnection().getProtocol().versions.get(0));
					r.protocolVersion(pingResult.getConnection().getProtocol().version);
					unsafe.sendPacket(new Kick(r.build().toString()));
					ch.close();
				} else {
					Gson gson = getProtocol() == Protocol.MC_1_7_2 ? bungee.gsonLegacy : bungee.gson;
					unsafe.sendPacket(new StatusResponse(gson.toJson(pingResult.response)));
				}
				
				if (bungee.getConnectionThrottle() != null)
					bungee.getConnectionThrottle().unthrottle(getAddress().getAddress());
				
			};

			bungee.getPluginManager().callEvent(new ProxyPingEvent(InitialHandler.this, result, callback));
		};

		if (forced != null && listener.isPingPassthrough())
			((BungeeServerInfo) forced).ping((ServerPing result, Throwable e) -> {
				if(e != null) {
					bungee.getLogger().log(Level.WARNING, toString() + " Error pinging remote server " + forced, e);
				}
				pingBack.done(result, e);
			}, getProtocol());
		else
			pingBack.done(def, null);

		thisState = State.PING;
	}

	@Override
	public void handle(PingPacket ping) throws Exception {
		thisState.shouldBe(State.PING);
		unsafe.sendPacket(ping);
		disconnect("");
	}
	
	private boolean trySetProtocol(int version, ProtocolGen gen) {
		protocol = Protocol.byNumber(version, gen);
		boolean undefinedProtocol = protocol == null;
		
		if(bungee.getConfig().isHandshake()) {
			String str = undefinedProtocol ? 
				"Undefined protocol, version : " + version
				: 
				"Protocol: " + protocol.name();
			str = "[" + ch.getRemoteAddress() + "] " + str;
			bungee.getLogger().info(str);
		}

		if(undefinedProtocol)
			protocol = ch.getProtocol();
		
		ch.setProtocol(protocol);
		
		return undefinedProtocol;
	}

	private void logConnected(NetworkState state) {
		if (state == NetworkState.LOGIN ||
			(state == NetworkState.STATUS && bungee.config.isLogPings()))
		{
			bungee.getLogger().log(Level.INFO, this.toString() + " Connected to listener ["+listener.getHost()+"]");
		}
	}
	
	@Override
	public void handle(Handshake handshake) throws Exception {
		thisState.shouldBe(State.HANDSHAKE);
		this.handshake = handshake;
		
		boolean undefinedProtocol = trySetProtocol(handshake.getProtocolVersion(), ProtocolGen.POST_NETTY);

		// Starting with FML 1.8, a "\0FML\0" token is appended to the handshake. This
		// interferes
		// with Bungee's IP forwarding, so we detect it, and remove it from the host
		// string, for now.
		// We know FML appends \00FML\00. However, we need to also consider that other
		// systems might
		// add their own data to the end of the string. So, we just take everything from
		// the \0 character
		// and save it for later.
		if (handshake.getHost().contains("\0")) {
			String[] split = handshake.getHost().split("\0", 2);
			handshake.setHost(split[0]);
			extraDataInHandshake = "\0" + split[1];
		}

		// SRV records can end with a . depending on DNS / client.
		if (handshake.getHost().endsWith("."))
			handshake.setHost(handshake.getHost().substring(0, handshake.getHost().length() - 1));
		
		this.virtualHost = InetSocketAddress.createUnresolved(handshake.getHost(), handshake.getPort());
		
		NetworkState requested = handshake.getRequestedNetworkState();
		
		logConnected(requested);
			
		bungee.getPluginManager().callEvent(new PlayerHandshakeEvent(InitialHandler.this, handshake));

		switch (requested) {
		case STATUS:
			// Ping
			thisState = State.STATUS;
			ch.setNetworkState(NetworkState.STATUS);
			break;
		case LOGIN:
			// Login
			thisState = State.USERNAME;
			
			if (undefinedProtocol)
				disconnect(bungee.getTranslation("unsupported_client"));
			
			ch.setNetworkState(NetworkState.LOGIN);
			
			break;
		default:
			throw new IllegalArgumentException("Cannot request protocol " + handshake.getRequestedNetworkState());
		}
	}

	@Override
	public void handle(LoginRequest loginRequest) throws Exception {
		thisState.shouldBe(State.USERNAME);
		this.loginRequest = loginRequest;

		if (getName().contains(".")) {
			disconnect(bungee.getTranslation("name_invalid"));
			return;
		}

		if (getName().length() > 16) {
			disconnect(bungee.getTranslation("name_too_long"));
			return;
		}

		int limit = BungeeCord.getInstance().config.getPlayerLimit();
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

		Callback<PreLoginEvent> callback = (PreLoginEvent result, Throwable error) -> {
			if (result.isCancelled()) {
				disconnect(result.getCancelReasonComponents());
				return;
			}
			if (ch.isClosed())
				return;
			
			if (onlineMode)
				unsafe().sendPacket(request = EncryptionUtil.encryptRequest());
			else {
				if(isLegacy()) {
					request = EncryptionUtil.encryptRequest();
					request.setServerId("-");
					unsafe.sendPacket(request);
				}
				else
					finish();
			}
			
			thisState = State.ENCRYPT;
		};

		// fire pre login event
		bungee.getPluginManager().callEvent(new PreLoginEvent(InitialHandler.this, callback));
	}

	EncryptionResponse encryptResponse;

	@Override
	public void handle(final EncryptionResponse encryptResponse) throws Exception {
		if(isLegacy() && thisState == State.USERNAME)
			thisState = State.ENCRYPT;
		Preconditions.checkState(thisState == State.ENCRYPT, "Not expecting ENCRYPT");
		this.encryptResponse = encryptResponse;

		SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, request);

		if (!isLegacy()) {
			BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
			ch.addBefore(PipelineUtil.FRAME_DEC, PipelineUtil.DECRYPT, new CipherDecoder(decrypt));
			BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
			ch.addBefore(PipelineUtil.FRAME_ENC, PipelineUtil.ENCRYPT, new CipherEncoder(encrypt));

			if(isOnlineMode())
				loginAndFinish(sharedKey);
		} else {
			ch.write(new EncryptionResponse());

			BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
			ch.addBefore(PipelineUtil.PACKET_DEC, PipelineUtil.DECRYPT, new CipherDecoder(decrypt));
			BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
			ch.addBefore(PipelineUtil.PACKET_ENC, PipelineUtil.ENCRYPT, new CipherEncoder(encrypt));
		}
	}

	private void loginAndFinish(SecretKey sharedKey) throws Exception {
		String encName = URLEncoder.encode(InitialHandler.this.getName(), "UTF-8");

		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		for (byte[] bit : new byte[][] { request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded() }) {
			sha.update(bit);
		}
		String encodedHash = URLEncoder.encode(new BigInteger(sha.digest()).toString(16), "UTF-8");

		String preventProxy = ((BungeeCord.getInstance().config.isPreventProxyConnections()) ? "&ip=" + URLEncoder.encode(getAddress().getAddress().getHostAddress(), "UTF-8") : "");
		String authURL = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=" + encName + "&serverId=" + encodedHash + preventProxy;

		Callback<String> handler = new Callback<String>() {
			@Override
			public void done(String result, Throwable error) {
				if (error == null) {
					LoginResult obj = BungeeCord.getInstance().gson.fromJson(result, LoginResult.class);
					if (obj != null && obj.getId() != null) {
						loginProfile = obj;
						name = obj.getName();
						uniqueId = Util.getUUID(obj.getId());
						finish();
						return;
					}
					disconnect(bungee.getTranslation("offline_mode_player"));
				} else {
					disconnect(bungee.getTranslation("mojang_fail"));
					bungee.getLogger().log(Level.SEVERE, "Error authenticating " + getName() + " with minecraft.net", error);
				}
			}
		};
		HttpClient.get(authURL, ch.getHandle().eventLoop(), handler);
	}

	private void finish() {
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
			if (result.isCancelled()) {
				disconnect(result.getCancelReasonComponents());
				return;
			}
			if (ch.isClosed())
				return;

			ch.getHandle().eventLoop().execute(() -> {
				if (!ch.isClosing()) {
					UserConnection userCon = new UserConnection(bungee, ch, getName(), InitialHandler.this);
					userCon.setCompressionThreshold(BungeeCord.getInstance().config.getCompressionThreshold());
					userCon.init();

					if (getProtocol().newerOrEqual(Protocol.MC_1_7_6))
						unsafe.sendPacket(new LoginSuccess(getUniqueId().toString(), getName())); // With dashes in between
					else if (!isLegacy())
						unsafe.sendPacket(new LoginSuccess(getUUID(), getName())); // Without dashes, for older clients.

					if (!isLegacy())
						ch.setNetworkState(NetworkState.GAME);

					ch.getHandle().pipeline().get(HandlerBoss.class).setHandler(new UpstreamBridge(bungee, userCon));
					bungee.getPluginManager().callEvent(new PostLoginEvent(userCon));
					ServerInfo server;
						
					if (bungee.getReconnectHandler() != null)
						server = bungee.getReconnectHandler().getServer(userCon);
					else
						server = AbstractReconnectHandler.getForcedHost(InitialHandler.this);
						
					if (server == null)
						server = bungee.getServerInfo(listener.getDefaultServer());

					userCon.connect(server, null, true, ServerConnectEvent.Reason.JOIN_PROXY);

					thisState = State.FINISHED;
				}
			});
		
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
	public void disconnect(final BaseComponent... reason) {
		if (thisState != State.STATUS && thisState != State.PING)
			ch.delayedClose(new Kick(ComponentSerializer.toString(reason)));
		else
			ch.close();
	}

	@Override
	public void disconnect(BaseComponent reason) {
		disconnect(new BaseComponent[] { reason });
	}

	@Override
	public String getName() {
		return (name != null) ? name : (loginRequest == null) ? null : loginRequest.getData();
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
	public void setOnlineMode(boolean onlineMode) {
		Preconditions.checkState(thisState == State.USERNAME, "Can only set online mode status whilst state is username");
		this.onlineMode = onlineMode;
	}

	@Override
	public void setUniqueId(UUID uuid) {
		Preconditions.checkState(thisState == State.USERNAME, "Can only set uuid while state is username");
		Preconditions.checkState(!onlineMode, "Can only set uuid when online mode is false");
		this.uniqueId = uuid;
	}

	@Override
	public String getUUID() {
		return uniqueId.toString().replace("-", "");
	}

	@Override
	public String toString() {
		return "["+getAddress()+((getName() != null) ? "/"+getName() : "") + "][IH]";
	}

	@Override
	public boolean isConnected() {
		return !ch.isClosed();
	}
	
	private String legacyInitital(int pv, String host, int port, NetworkState state) {
		trySetProtocol(pv == -1 ? Protocol.MC_1_5_2.version : pv, ProtocolGen.PRE_NETTY);
		
		if(host != null) {
			if (host.endsWith("."))
				host = host.substring(0, host.length() - 1);
		
			this.virtualHost = InetSocketAddress.createUnresolved(host, port);
		}
		
		logConnected(state);
		
		return host;
	}

	@Override
	public void handle(final LegacyStatusRequest request) throws Exception {
		thisState.shouldBe(State.HANDSHAKE);
		legacyInitital(request.getProtocolVersion(), request.getHost(), request.getPort(), NetworkState.STATUS);
		
		thisState = State.STATUS;
		
		handle(new StatusRequest());
	}

	@Override
	public void handle(LegacyLoginRequest lr) throws Exception {
		thisState.shouldBe(State.HANDSHAKE);
		
		this.legacyLoginRequest = lr;
		
		lr.setHost(legacyInitital(lr.getProtocolVersion(), lr.getHost(), lr.getPort(), NetworkState.LOGIN));
		
		thisState = State.USERNAME;
		
		handle(new LoginRequest(lr.getUserName()));
	}

	@Override
	public void handle(LegacyClientCommand clientCommandOld) throws Exception {
		if (clientCommandOld.command != 0)
			throw new RuntimeException();

		if(isOnlineMode())
			loginAndFinish(EncryptionUtil.getSecret(encryptResponse, request));
		else finish();
	}
	
	@Override
	public void handle(Login login) throws Exception {
		if(login.getEntityId() != Hashing.murmur3_32().hashString("FML", Charsets.US_ASCII).asInt()
				&& login.getDimension() != 2)
			return;
		// Forge client
		forgeLogin = login;
	}
}
