package net.md_5.bungee.connection;

import java.net.InetSocketAddress;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;

import lombok.Getter;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.PipelineUtil;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.NetworkState;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolGen;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.Kick.StatusResponce;
import net.md_5.bungee.protocol.packet.LegacyClientCommand;
import net.md_5.bungee.protocol.packet.LegacyLoginRequest;
import net.md_5.bungee.protocol.packet.LegacyStatusRequest;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.StatusRequest;

public class LegacyInitialHandler extends InitialHandler {
	@Getter
	private LegacyLoginRequest loginRequest;
	@Getter
	private Login forgeLogin;

	public LegacyInitialHandler(ListenerInfo listener) {
		super(listener);
	}
	
	@Override
	public void handle(final LegacyStatusRequest request) throws Exception {
		Protocol protocol = null;
		Protocol possibleProtocol = null;
		String host = null;
		int port = -1;
		
		if(request.getHost() == null)
			possibleProtocol = Protocol.MC_1_5_2;
		else {
			host = request.getHost();
			port = request.getPort();
			protocol = Protocol.byNumber(request.getProtocolVersion(), ProtocolGen.PRE_NETTY);
			if(protocol == null)
				possibleProtocol = Protocol.MC_1_6_4;
		}
		
		if(bungee.getConfig().isHandshake()) {
			String str = protocol == null ? 
				"Undefined protocol, version : " + request.getProtocolVersion()
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
			ch.close(new Kick(message));
		});
	}

	@Override
	public void handle(LegacyLoginRequest lr) throws Exception {
		this.loginRequest = lr;
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
	
	EncryptionResponse encryptResponse;
	
	@Override
	public void handle(final EncryptionResponse encryptResponse) throws Exception {
		this.encryptResponse = encryptResponse;

		SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, request);


		ch.write(new EncryptionResponse());

		BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
		ch.addBefore(PipelineUtil.PACKET_DEC, PipelineUtil.DECRYPT, new CipherDecoder(decrypt));
		BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
		ch.addBefore(PipelineUtil.PACKET_ENC, PipelineUtil.ENCRYPT, new CipherEncoder(encrypt));
	}

	@Override
	public String getName() {
		return loginRequest.getUserName();
	}

	@Override
	public Protocol getProtocol() {
		return Protocol.byNumber(loginRequest.getProtocolVersion(), ProtocolGen.PRE_NETTY);
	}

	@Override
	public void disconnect(BaseComponent... reason) {
		
	}

	@Override
	protected void finish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InetSocketAddress getVirtualHost() {
		// TODO Auto-generated method stub
		return null;
	}

}
