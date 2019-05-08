package net.md_5.bungee;

import java.io.DataInput;
import java.security.PublicKey;
import java.util.Queue;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.score.Objective;
import net.md_5.bungee.api.score.Score;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.forge.ForgeConstants;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtil;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftOutput;
import net.md_5.bungee.protocol.PacketPreparer;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.LegacyClientCommand;
import net.md_5.bungee.protocol.packet.LegacyLoginRequest;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.util.QuietException;

public class LegacyServerConnector extends ServerConnector {
	private int fmlVanillaCompatabilityLevel = 0;
	private ChannelInboundHandler legacyFMLModlistCatcher = null;
	private SecretKey secret;
	
	public LegacyServerConnector(UserConnection user, BungeeServerInfo target) {
		super(user, target);
	}
	
	@Override
	public void handle(PacketWrapper packet) throws Exception {
		super.handle(packet);
	}
	
	@Override
	public void connected(ChannelWrapper channel) throws Exception {
		super.connected(channel);
		
		LegacyLoginRequest copiedLoginRequest = user.getPendingConnection().getLegacyLoginRequest().clone();
		
		if(ipForward())
			copiedLoginRequest.setHost(copiedLoginRequest.getHost() + "\00" + user.getAddress().getHostString() + "\00" + user.getUUID());
		
		if(forgeSupport()) {
			ChannelPipeline p = user.getCh().getHandle().pipeline();
			
			legacyFMLModlistCatcher = new SimpleChannelInboundHandler<PacketWrapper>(PacketWrapper.class) {

				@Override
				protected void channelRead0(ChannelHandlerContext ctx, PacketWrapper msg) throws Exception {		
					if(msg.packet instanceof PluginMessage) {
						PluginMessage pm = (PluginMessage) msg.packet;
						if(pm.getTag().equals("FML") && pm.getData()[0] == 1) {// client packet response
							ch.write(pm);
							return;
						}
					}
					
					ctx.fireChannelRead(msg);
				}
				
			};
			p.addBefore(PipelineUtil.BOSS, "legacy_fml_modlist_catcher", legacyFMLModlistCatcher);
		}
		
		copiedLoginRequest.setProtocolVersion(user.getPendingConnection().getProtocol().version);
		copiedLoginRequest.setUserName(user.getName());
		channel.write(copiedLoginRequest);
		
		if(forgeSupport() && user.getPendingConnection().getForgeLogin() != null)
			ch.write(user.getPendingConnection().getForgeLogin());
		
		server = new ServerConnection(ch, target);
	}
	
	@Override
	public void prepare(PacketPreparer p) {
		if(p.getPacket() instanceof Login && forgeSupport())
			((Login)p.getPacket()).setFmlVanillaComp(fmlVanillaCompatabilityLevel == 0);
	}
	
	@Override
	public void handle(PluginMessage pluginMessage) throws Exception {
		if(forgeSupport() && pluginMessage.getTag().equals(ForgeConstants.FML_TAG) && pluginMessage.getData()[0] == 0) {
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
			user.getCh().getHandle().pipeline().remove(legacyFMLModlistCatcher);
			
		ServerConnectedEvent event = new ServerConnectedEvent(user, server);
		bungee.getPluginManager().callEvent(event);
	
		ch.write(bungee.registerChannels(user.pendingConnection.getProtocol()));
	
		Queue<DefinedPacket> packetQueue = target.getPacketQueue();
		synchronized (packetQueue) {
			while (!packetQueue.isEmpty())
				ch.write(packetQueue.poll());
		}
	
		for (PluginMessage message : user.getPendingConnection().getRelayMessages()) {
			ch.write(message);
		}
	
		if (user.getSettings() != null) 
			ch.write(user.getSettings());
		
		catchPackets = true;
		
		user.getCh().eventLoop().execute(() -> {
			if (user.getServer() == null) {
				// Once again, first connection
				user.setClientEntityId(login.getEntityId());
				user.setServerEntityId(login.getEntityId());
		
				// Set tab list size, this sucks balls, TODO: what shall we do about packet
				// mutability
				// Forge allows dimension ID's > 127
				user.unsafe().sendPacket(login.clone().setMaxPlayers(user.getPendingConnection().getListener().getTabListSize()));
	
				MinecraftOutput out = new MinecraftOutput();
				out.writeStringUTF8WithoutLengthHeaderBecauseDinnerboneStuffedUpTheMCBrandPacket(ProxyServer.getInstance().getName() + " (" + ProxyServer.getInstance().getVersion() + ")");
				user.unsafe().sendPacket(new PluginMessage("MC|Brand", out.toArray(), false));
		
				user.setDimension(login.getDimension());
			} else {
				user.getServer().setObsolete(true);
				user.getTabListHandler().onServerChange();
		
				Scoreboard serverScoreboard = user.getServerSentScoreboard();
				for (Objective objective : serverScoreboard.getObjectives())
					user.unsafe().sendPacket(new ScoreboardObjective(objective.getName(), objective.getValue(), objective.getType() == null ? null : ScoreboardObjective.HealthDisplay.fromString(objective.getType()), (byte) 1)); // Travertine - 1.7
				
				for (Score score : serverScoreboard.getScores())
					user.unsafe().sendPacket(new ScoreboardScore(score.getItemName(), (byte) 1, score.getScoreName(), score.getValue()));
				
				for (Team team : serverScoreboard.getTeams())
					user.unsafe().sendPacket(new net.md_5.bungee.protocol.packet.Team(team.getName()));
				
				serverScoreboard.clear();
				
				// Send remove bossbar packet
				for (UUID bossbar : user.getSentBossBars())
					user.unsafe().sendPacket(new net.md_5.bungee.protocol.packet.BossBar(bossbar, 1));
				
				user.getSentBossBars().clear();
		
				user.setDimensionChange(true);
				if (login.getDimension() == user.getDimension())
					user.unsafe().sendPacket(new Respawn((login.getDimension() >= 0 ? -1 : 0), login.getDifficulty(), login.getGameMode(), login.getWorldHeight(), login.getLevelType()));
		
				user.setServerEntityId(login.getEntityId());
				user.unsafe().sendPacket(new Respawn(login.getDimension(), login.getDifficulty(), login.getGameMode(), login.getWorldHeight(), login.getLevelType()));
				user.setDimension(login.getDimension());
		
				// Remove from old servers
				user.getServer().disconnect("Quitting");
			}
				
			// TODO: Fix this?
			/*if (!user.isActive()) {
				server.disconnect("Quitting");
				// Silly server admins see stack trace and die
				bungee.getLogger().warning("No client connected for pending server!");
				return;
			}*/
	
			// Add to new server
			// TODO: Move this to the connected() method of DownstreamBridge
			user.getPendingConnects().remove(target);
			user.setServerJoinQueue(null);
			user.setDimensionChange(false);
			
			user.setServer(server);
			
			ch.eventLoop().execute(() -> {
				target.addPlayer(user);
				ch.handle.pipeline().get(HandlerBoss.class).setHandler(new DownstreamBridge(user, user.getServer()));
				
				packets.forEach(pw -> {
					ch.handle.pipeline().fireChannelRead(pw);
					pw.release();
				});
				
				bungee.getPluginManager().callEvent(new ServerSwitchEvent(user));
			});
		});
		
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

		EncryptionResponse er = new EncryptionResponse();
		er.setSharedSecret(EncryptionUtil.encrypt(pub, secret.getEncoded()));
		er.setVerifyToken(EncryptionUtil.encrypt(pub, encryptionRequest.getVerifyToken()));
		ch.write(er);

		BungeeCipher encrypt = EncryptionUtil.getCipher(true, secret);
		ch.addBefore(PipelineUtil.PACKET_ENC, PipelineUtil.ENCRYPT, new CipherEncoder(encrypt));

		throw CancelSendSignal.INSTANCE;
	}
	
	@Override
	public void handle(EncryptionResponse encryptionResponse) throws Exception {
		BungeeCipher encrypt = EncryptionUtil.getCipher(false, secret);
		ch.addBefore(PipelineUtil.PACKET_DEC, PipelineUtil.DECRYPT, new CipherDecoder(encrypt));

		ch.write(new LegacyClientCommand(0));

		throw CancelSendSignal.INSTANCE;
	}

}
