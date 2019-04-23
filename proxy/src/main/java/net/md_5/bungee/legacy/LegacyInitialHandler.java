package net.md_5.bungee.legacy;

import java.net.InetSocketAddress;

import javax.crypto.SecretKey;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import io.netty.channel.Channel;
import lombok.Getter;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.PipelineUtil;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolGen;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.Kick.StatusResponce;
import net.md_5.bungee.protocol.packet.LegacyClientCommand;
import net.md_5.bungee.protocol.packet.LegacyLoginRequest;
import net.md_5.bungee.protocol.packet.LegacyStatusRequest;
import net.md_5.bungee.protocol.packet.Login;

public class LegacyInitialHandler extends InitialHandler {
	LegacyStatusRequest statusRequest;
	@Getter
	private LegacyLoginRequest loginRequest;
	@Getter
	private Login forgeLogin;

	public LegacyInitialHandler(Channel ch, ListenerInfo listener) {
		super(ch, listener);
	}
	
	@Override
	public void handle(final LegacyStatusRequest request) throws Exception {
		this.statusRequest = request;
		Protocol protocol = null;
		Protocol possibleProtocol = null;
		
		if(request.getHost() == null)
			possibleProtocol = Protocol.MC_1_5_2;
		else {
			protocol = Protocol.byNumber(request.getProtocolVersion(), ProtocolGen.PRE_NETTY);
			if(protocol == null)
				possibleProtocol = Protocol.MC_1_6_4;
		}
		
		if(bungee.getConfig().isHandshake()) {
			String str = protocol == null ? 
				"Undefined protocol, version: " + request.getProtocolVersion()
				: 
				"Protocol: " + protocol.name();
			str = "[" + ch.getRemoteAddress() + "] " + str;
			bungee.getLogger().info(str);
		}
		
		if(protocol == null)
			protocol = possibleProtocol;
		
		ch.setProtocol(protocol);
		
		final Protocol protocol0 = protocol;
		
		ping((result, error) -> {
			String message = StatusResponce.builder()
				.protocolVersion(protocol0.version)
				.mcVersion(protocol0.versions.get(0))
				.motd(result.getResponse().getDescription())
				.players(result.getResponse().getPlayers().getOnline())
				.max(result.getResponse().getPlayers().getMax())
				.build()
				.toString();
			ch.write(new Kick(message));
			ch.close();
		});
	}

	private EncryptionRequest encryptionRequest;
	
	@Override
	public void handle(LegacyLoginRequest lr) throws Exception {
		this.loginRequest = lr;
		
		preLogin((result, error) -> {
			encryptionRequest = EncryptionUtil.encryptRequest();
			if(!isOnlineMode())
				encryptionRequest.setServerId("-");
			ch.write(encryptionRequest);
		});
	}

	@Override
	public void handle(LegacyClientCommand clientCommandOld) throws Exception {
		if (clientCommandOld.command != 0)
			throw new RuntimeException();

		if(isOnlineMode())
			auth(encryptionRequest, EncryptionUtil.getSecret(encryptResponse, encryptionRequest), () -> login());
		else login();
	}
	
	private void login() {
		login((res, error) -> {
			LegacyUserConnection con = new LegacyUserConnection(ch, this);
			postLogin(con);
		});
	}
	
	@Override
	public void handle(Login login) throws Exception {
		if(login.getEntityId() != Hashing.murmur3_32().hashString("FML", Charsets.US_ASCII).asInt()
				&& login.getDimension() != 2)
			return;
		// Forge client
		forgeLogin = login;
	}
	
	EncryptionResponse encryptResponse;
	
	@Override
	public void handle(final EncryptionResponse encryptResponse) throws Exception {
		this.encryptResponse = encryptResponse;

		SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, encryptionRequest);

		ch.write(new EncryptionResponse());

		BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
		ch.handle.pipeline().addBefore(PipelineUtil.PACKET_DEC, PipelineUtil.DECRYPT, new CipherDecoder(decrypt));
		BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
		ch.handle.pipeline().addBefore(PipelineUtil.PACKET_ENC, PipelineUtil.ENCRYPT, new CipherEncoder(encrypt));
	}

	@Override
	public String getName() {
		String name = super.getName();
		if(name != null) return name;
		return loginRequest.getUserName();
	}

	@Override
	public void disconnect(BaseComponent... reason) {
		ch.write(new Kick(TextComponent.toLegacyText(reason)));
		ch.close();
	}

	@Override
	public InetSocketAddress getVirtualHost() {
		if(statusRequest != null && statusRequest.getHost() != null)
			return InetSocketAddress.createUnresolved(statusRequest.getHost(), statusRequest.getPort());
		else if(loginRequest != null && loginRequest.getHost() != null)
			return InetSocketAddress.createUnresolved(loginRequest.getHost(), loginRequest.getPort());
		return null;
	}

}
