package net.md_5.bungee.modern;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import io.netty.channel.Channel;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.jni.cipher.BungeeCipher;
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

public class ModernInitialHandler extends InitialHandler {
	@Getter
	private Handshake handshake;
	@Getter
	private LoginRequest loginRequest;
	private EncryptionRequest request;
	@Getter
	private final List<PluginMessage> relayMessages = new ArrayList<>();
	private State thisState = State.HANDSHAKE;
	@Getter
	private String extraDataInHandshake = "";
	
	public ModernInitialHandler(Channel ch, ListenerInfo listener) {
		super(ch, listener);
	}
	
	enum State {
		HANDSHAKE, STATUS, PING, USERNAME, ENCRYPT, FINISHED;
		
		public void shouldBe(State state) {
			Preconditions.checkState(state == this, "Should be " + state.name() + ", but it's " + name());
		}
	}
	
	@Override
	public void handle(Handshake handshake) throws Exception {
		thisState.shouldBe(State.HANDSHAKE);
		this.handshake = handshake;
		
		Protocol protocol = Protocol.byNumber(handshake.getProtocolVersion(), ProtocolGen.POST_NETTY);
		boolean undefinedProtocol = getProtocol() == null;

		if(bungee.getConfig().isHandshake()) {
			String str = undefinedProtocol ? 
				" Undefined protocol, version: " + handshake.getProtocolVersion()
				: 
				" Protocol: " + protocol.name();
			
			bungee.getLogger().info(toString() + str);
		}
		
		if(protocol != null)
			ch.setProtocol(protocol);

		if (handshake.getHost().contains("\0")) {
			String[] split = handshake.getHost().split("\0", 2);
			handshake.setHost(split[0]);
			extraDataInHandshake = "\0" + split[1];
		}

		// SRV records can end with a . depending on DNS / client.
		if (handshake.getHost().endsWith("."))
			handshake.setHost(handshake.getHost().substring(0, handshake.getHost().length() - 1));
		
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
		thisState.shouldBe(State.STATUS);

		ping((pingResult, error) -> {
			boolean useGsonLegacy = getProtocol().isBetweenInclusive(Protocol.MC_1_7_2, Protocol.MC_1_7_6);
			Gson gson = useGsonLegacy ? BungeeCord.getInstance().gsonLegacy : BungeeCord.getInstance().gson;
			unsafe.sendPacket(new StatusResponse(gson.toJson(pingResult.getResponse())));
		});

		thisState = State.PING;
	}
	
	@Override
	public void handle(PingPacket ping) throws Exception {
		thisState.shouldBe(State.PING);
		ch.write(ping);
		ch.close();
	}
	
	@Override
	public void handle(LoginPayloadResponse response) throws Exception {
		disconnect("Unexpected custom LoginPayloadResponse");
	}
	
	@Override
	public void handle(LoginRequest loginRequest) throws Exception {
		thisState.shouldBe(State.USERNAME);
		this.loginRequest = loginRequest;

		preLogin((result, error) -> {
			thisState = State.ENCRYPT;
			
			if (isOnlineMode())
				unsafe().sendPacket(request = EncryptionUtil.encryptRequest());
			else login();
		});
	}
	
	@Override
	public void handle(final EncryptionResponse encryptResponse) throws Exception {
		thisState.shouldBe(State.ENCRYPT);

		SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, request);

		BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
		ch.handle.pipeline().addBefore(PipelineUtil.FRAME_DEC, PipelineUtil.DECRYPT, new CipherDecoder(decrypt));
		BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
		ch.handle.pipeline().addBefore(PipelineUtil.FRAME_ENC, PipelineUtil.ENCRYPT, new CipherEncoder(encrypt));

		if(isOnlineMode())
			auth(request, sharedKey, () -> login());
	}
	
	@Override
	public void disconnect(final BaseComponent... reason) {
		if (thisState != State.STATUS && thisState != State.PING)
			ch.write(new Kick(ComponentSerializer.toString(reason)));
		ch.close();
	}
	
	@Override
	public void setOnlineMode(boolean onlineMode) {
		thisState.shouldBe(State.USERNAME);
		this.onlineMode = onlineMode;
	}
	
	@Override
	public String getName() {
		String name = super.getName();
		if(name != null) return name;
		return (loginRequest == null) ? null : loginRequest.getData();
	}

	protected void login() {
		login((result, error) -> {
			ModernUserConnection userCon = new ModernUserConnection(ch, this);
			userCon.setCompressionThreshold(BungeeCord.getInstance().config.getCompressionThreshold());

			if (getProtocol().newerOrEqual(Protocol.MC_1_7_6))
				unsafe.sendPacket(new LoginSuccess(getUniqueId().toString(), getName())); // With dashes in between
			else if (!isLegacy())
				unsafe.sendPacket(new LoginSuccess(getUUID(), getName())); // Without dashes, for older clients.

			ch.setNetworkState(NetworkState.GAME);

			postLogin(userCon);

			thisState = State.FINISHED;
		});
	}

	@Override
	public InetSocketAddress getVirtualHost() {
		return InetSocketAddress.createUnresolved(handshake.getHost(), handshake.getPort());
	}
}
