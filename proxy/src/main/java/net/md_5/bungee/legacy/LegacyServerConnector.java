package net.md_5.bungee.legacy;

import java.io.DataInput;
import java.security.PublicKey;
import java.util.Queue;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.val;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.forge.ForgeConstants;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PipelineUtil;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.PacketPreparer;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.LegacyClientCommand;
import net.md_5.bungee.protocol.packet.LegacyLoginRequest;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.util.QuietException;

public class LegacyServerConnector extends ServerConnector<LegacyUserConnection> {
	private int fmlVanillaCompatabilityLevel = 0;
	private ChannelInboundHandler legacyFMLModlistCatcher = null;
	private SecretKey secret;
	@Getter
	private Login login;
	
	public LegacyServerConnector(ChannelWrapper ch, LegacyUserConnection user, BungeeServerInfo target) {
		super(ch, user, target);
	}
	
	@Override
	public void connected() throws Exception {
		val loginRequest = new LegacyLoginRequest(user.pendingConnection.getLoginRequest());
		
		if(target.ipForward())
			loginRequest.setHost(loginRequest.getHost() + "\00" + user.getAddress().getHostString() + "\00" + user.getUUID());
		
		if(target.forgeSupport()) {
			ChannelPipeline p = user.getCh().pipeline();
			
			legacyFMLModlistCatcher = new SimpleChannelInboundHandler<PacketWrapper>(PacketWrapper.class) {

				@Override
				protected void channelRead0(ChannelHandlerContext ctx, PacketWrapper msg) throws Exception {		
					if(msg.packet instanceof PluginMessage) {
						PluginMessage pm = (PluginMessage) msg.packet;
						if(pm.getTag().equals("FML") && pm.getData()[0] == 1) {// client packet response
							channel.write(pm);
							return;
						}
					}
					
					ctx.fireChannelRead(msg);
				}
				
			};
			p.addBefore(PipelineUtil.BOSS, "legacy_fml_modlist_catcher", legacyFMLModlistCatcher);
		}
		
		channel.write(loginRequest);
		
		if(target.forgeSupport() && user.isForgeUser())
			channel.write(user.pendingConnection.getForgeLogin());
	}
	
	@Override
	public void prepare(PacketPreparer p) {
		if(p.getPacket() instanceof Login && target.forgeSupport())
			((Login)p.getPacket()).setFmlVanillaComp(fmlVanillaCompatabilityLevel == 0);
	}
	
	@Override
	public void handle(PluginMessage pluginMessage) throws Exception {
		if(target.forgeSupport() && pluginMessage.getTag().equals(ForgeConstants.FML_TAG) && pluginMessage.getData()[0] == 0) {
			DataInput i = pluginMessage.getStream();
			i.skipBytes(1);
			int count = i.readInt();
			for(int x = 0; x < count; x++ )
				i.readUTF();
			fmlVanillaCompatabilityLevel = i.readByte();
		}
		user.unsafe().sendPacket(pluginMessage);
	}
	
	@Override
	public void handle(Login login) throws Exception {
		if(legacyFMLModlistCatcher != null)
			user.getCh().pipeline().remove(legacyFMLModlistCatcher);
		
		this.login = login;
		// TODO
		//channel.write(bungee.registerChannels(user.pendingConnection.getProtocol()));
		
		Queue<DefinedPacket> packetQueue = target.getPacketQueue();
		synchronized (packetQueue) {
			while (!packetQueue.isEmpty())
				channel.write(packetQueue.poll());
		}
		
		if (user.getSettings() != null) 
			channel.write(user.getSettings());
		
		loginFuture.setSuccess(null);
		
		throw CancelSendSignal.INSTANCE;
	}
	
	@Override
	public void handle(EncryptionRequest encryptionRequest) throws Exception {
		if(!encryptionRequest.getServerId().equals("-"))
			throw new QuietException( "Server in online mode!" );
		
		PublicKey pub = EncryptionUtil.getPubkey(encryptionRequest);
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		secret = kg.generateKey();

		EncryptionResponse er = EncryptionResponse.builder()
			.sharedSecret(EncryptionUtil.encrypt(pub, secret.getEncoded()))
			.verifyToken(EncryptionUtil.encrypt(pub, encryptionRequest.getVerifyToken()))
			.build();
		channel.write(er);

		BungeeCipher encrypt = EncryptionUtil.getCipher(true, secret);
		channel.pipeline().addBefore(PipelineUtil.PACKET_ENC, PipelineUtil.ENCRYPT, new CipherEncoder(encrypt));

		throw CancelSendSignal.INSTANCE;
	}
	
	@Override
	public void handle(EncryptionResponse encryptionResponse) throws Exception {
		BungeeCipher encrypt = EncryptionUtil.getCipher(false, secret);
		channel.pipeline().addBefore(PipelineUtil.PACKET_DEC, PipelineUtil.DECRYPT, new CipherDecoder(encrypt));

		channel.write(new LegacyClientCommand(0));

		throw CancelSendSignal.INSTANCE;
	}

}
