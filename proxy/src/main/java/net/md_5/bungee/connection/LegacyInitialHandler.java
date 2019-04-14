package net.md_5.bungee.connection;

import javax.crypto.SecretKey;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;

import net.md_5.bungee.EncryptionUtil;
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
import net.md_5.bungee.protocol.packet.LegacyClientCommand;
import net.md_5.bungee.protocol.packet.LegacyLoginRequest;
import net.md_5.bungee.protocol.packet.LegacyStatusRequest;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.StatusRequest;

public class LegacyInitialHandler extends InitialHandler {

	public LegacyInitialHandler(ListenerInfo listener) {
		super(listener);
	}
	
	@Override
	public void handle(final LegacyStatusRequest request) throws Exception {
		Handshake handshake = new Handshake();
		
		if(request.getHost() == null)
			handshake.setProtocol(Protocol.MC_1_5_2);
		else {
			handshake.setHost(request.getHost());
			handshake.setPort(request.getPort());
			Protocol p = Protocol.byNumber(request.getProtocolVersion(), ProtocolGen.PRE_NETTY);
			handshake.setProtocol(p != null ? p : Protocol.MC_1_6_4);
		}
		
		handshake.setRequestedNetworkState(NetworkState.STATUS);
		handle(handshake);
		handle(new StatusRequest());
	}

	@Override
	public void handle(LegacyLoginRequest lr) throws Exception {
		Preconditions.checkState(thisState == State.HANDSHAKE, "Not expecting NADSHAKE");
		Handshake handshake = new Handshake();
		handshake.setProtocol(Protocol.byNumber(lr.getProtocolVersion(), ProtocolGen.PRE_NETTY));
		handshake.setHost(lr.getHost());
		handshake.setPort(lr.getPort());
		handshake.setRequestedNetworkState(NetworkState.LOGIN);
		handle(handshake);
		handle(new LoginRequest(lr.getUserName()));
	}

	@Override
	public void handle(LegacyClientCommand clientCommandOld) throws Exception {
		if (clientCommandOld.command != 0)
			throw new RuntimeException();

		if(isOnlineMode())
			loginAndFinish(EncryptionUtil.getSecret(encryptResponse, request));
		else login();
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

}
