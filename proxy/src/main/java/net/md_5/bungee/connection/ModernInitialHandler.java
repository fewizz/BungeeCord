package net.md_5.bungee.connection;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Level;

import javax.crypto.SecretKey;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.ModernUserConnection;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtil;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.NetworkState;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolGen;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LoginPayloadResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.util.BoundedArrayList;

public class ModernInitialHandler extends InitialHandler {
	@Getter
	private Handshake handshake;
	@Getter
	private LoginRequest loginRequest;
	private EncryptionRequest request;
	@Getter
	private final List<PluginMessage> relayMessages = new BoundedArrayList<>(128);
	private State thisState = State.HANDSHAKE;
	@Getter
	@Setter
	private InetSocketAddress virtualHost;
	@Setter
	private String name;
	@Getter
	private String extraDataInHandshake = "";
	
	private enum State {
		HANDSHAKE, STATUS, PING, USERNAME, ENCRYPT, FINISHED;
	}
	
	public ModernInitialHandler(ListenerInfo listener) {
		super(listener);
	}
	
	@Override
	public void handle(Handshake handshake) throws Exception {
		Preconditions.checkState(thisState == State.HANDSHAKE, "Not expecting HANDSHAKE");
		this.handshake = handshake;
		
		Protocol protocol = Protocol.byNumber(handshake.getProtocolVersion(), ProtocolGen.POST_NETTY);
		boolean undefinedProtocol = getProtocol() == null;

		if(bungee.getConfig().isHandshake()) {
			String str = undefinedProtocol ? 
					"Undefined protocol, version : " + handshake.getProtocolVersion()
					: 
					"Protocol: " + protocol.name();
			str = "[" + ch.getRemoteAddress() + "] " + str;
			bungee.getLogger().info(str);
		}
		
		if(protocol != null)
			ch.setProtocol(protocol);

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
			
		if (bungee.getConfig().isLogPings())
			bungee.getLogger().log(Level.INFO, "{0} has connected", this);
		
		bungee.getPluginManager().callEvent(new PlayerHandshakeEvent(this, handshake));

		switch (handshake.getRequestedNetworkState()) {
		case STATUS:
			// Ping
			thisState = State.STATUS;
			if(!isLegacy())
				ch.setNetworkState(NetworkState.STATUS);
			break;
		case LOGIN:
			// Login
			if (!bungee.getConfig().isLogPings()) {
				bungee.getLogger().log(Level.INFO, "{0} has connected", this);
			}
			thisState = State.USERNAME;
			
			if (undefinedProtocol)
				disconnect(bungee.getTranslation("unsupported_client"));
			
			if(!isLegacy())
				ch.setNetworkState(NetworkState.LOGIN);
			
			break;
		default:
			throw new IllegalArgumentException("Cannot request protocol " + handshake.getRequestedNetworkState());
		}
	}
	
	@Override
	public void handle(StatusRequest statusRequest) throws Exception {
		Preconditions.checkState(thisState == State.STATUS, "Not expecting STATUS");

		ping((pingResult, error) -> {
			boolean useGsonLegacy = getProtocol().isBetweenInclusive(Protocol.MC_1_7_2, Protocol.MC_1_7_6);
			Gson gson = useGsonLegacy ? BungeeCord.getInstance().gsonLegacy : BungeeCord.getInstance().gson;
			unsafe.sendPacket(new StatusResponse(gson.toJson(pingResult.getResponse())));
		});

		thisState = State.PING;
	}
	
	@Override
	public void handle(PingPacket ping) throws Exception {
		Preconditions.checkState(thisState == State.PING, "Not expecting PING");
		unsafe.sendPacket(ping);
		ch.close();
	}
	
	@Override
	public void handle(LoginPayloadResponse response) throws Exception {
		disconnect("Unexpected custom LoginPayloadResponse");
	}
	
	@Override
	public void handle(LoginRequest loginRequest) throws Exception {
		Preconditions.checkState(thisState == State.USERNAME, "Not expecting USERNAME");
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


		// fire pre login event
		bungee.getPluginManager().callEvent(new PreLoginEvent(this, (PreLoginEvent result, Throwable error) -> {
			if (ch.isClosed())
				return;
			if (result.isCancelled()) {
				disconnect(result.getCancelReasonComponents());
				return;
			}
			
			thisState = State.ENCRYPT;
			
			if (isOnlineMode())
				unsafe().sendPacket(request = EncryptionUtil.encryptRequest());
			else finish();
		}));
	}
	
	@Override
	public void handle(final EncryptionResponse encryptResponse) throws Exception {
		if(isLegacy() && thisState == State.USERNAME)
			thisState = State.ENCRYPT;
		Preconditions.checkState(thisState == State.ENCRYPT, "Not expecting ENCRYPT");

		SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, request);

		BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
		ch.addBefore(PipelineUtil.FRAME_DEC, PipelineUtil.DECRYPT, new CipherDecoder(decrypt));
		BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
		ch.addBefore(PipelineUtil.FRAME_ENC, PipelineUtil.ENCRYPT, new CipherEncoder(encrypt));

		if(isOnlineMode())
			loginAndFinish(request, sharedKey);

	}
	
	@Override
	public void disconnect(final BaseComponent... reason) {
		if (thisState != State.STATUS && thisState != State.PING)
			ch.delayedClose(new Kick(ComponentSerializer.toString(reason)));
		else
			ch.close();
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.byNumber(handshake.getProtocolVersion(), ProtocolGen.POST_NETTY);
	}
	
	@Override
	public void setOnlineMode(boolean onlineMode) {
		Preconditions.checkState(thisState == State.USERNAME, "Can only set online mode status whilst state is username");
		this.onlineMode = onlineMode;
	}
	
	@Override
	public String getName() {
		return (name != null) ? name : (loginRequest == null) ? null : loginRequest.getData();
	}

	@Override
	protected void finish() {
		checkPlayer((result, error) -> {
			ModernUserConnection userCon = new ModernUserConnection(bungee, ch, getName(), this);
			userCon.setCompressionThreshold(BungeeCord.getInstance().config.getCompressionThreshold());
			userCon.init();

			if (getProtocol().newerOrEqual(Protocol.MC_1_7_6))
				unsafe.sendPacket(new LoginSuccess(getUniqueId().toString(), getName())); // With dashes in between
			else if (!isLegacy())
				unsafe.sendPacket(new LoginSuccess(getUUID(), getName())); // Without dashes, for older clients.

			ch.setNetworkState(NetworkState.GAME);

			ch.getHandle().pipeline().get(HandlerBoss.class).setHandler(new UpstreamBridge(bungee, userCon));
			bungee.getPluginManager().callEvent(new PostLoginEvent(userCon));
		
			ServerInfo server;
			
			if (bungee.getReconnectHandler() != null)
				server = bungee.getReconnectHandler().getServer(userCon);
			else
				server = AbstractReconnectHandler.getForcedHost(this);
			
			if (server == null)
				server = bungee.getServerInfo(listener.getDefaultServer());

			userCon.connect(server, null, true, ServerConnectEvent.Reason.JOIN_PROXY);

			thisState = State.FINISHED;
		});
	}
}
