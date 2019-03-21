package net.md_5.bungee;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.score.Objective;
import net.md_5.bungee.api.score.Score;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.DownstreamBridge;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.forge.ForgeConstants;
import net.md_5.bungee.forge.ForgeServerHandler;
import net.md_5.bungee.forge.ForgeUtils;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtil;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.MinecraftOutput;
import net.md_5.bungee.protocol.NetworkState;
import net.md_5.bungee.protocol.Packet;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.EntityStatus;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LegacyClientCommand;
import net.md_5.bungee.protocol.packet.LegacyLoginRequest;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.SetCompression;
import net.md_5.bungee.util.BufUtil;
import net.md_5.bungee.util.QuietException;

@RequiredArgsConstructor
public class ServerConnector extends PacketHandler {

	private final ProxyServer bungee;
	private ChannelWrapper ch;
	private final UserConnection user;
	private final BungeeServerInfo target;
	private State thisState = State.UNDEF;
	@Getter
	private ForgeServerHandler handshakeHandler;
	private boolean obsolete;

	private SecretKey secret;

	private enum State {
		UNDEF, LOGIN_REQUEST, LOGIN_SUCCESS, ENCRYPT_RESPONSE, LOGIN, FINISHED;
	}

	@Override
	public void exception(Throwable t) throws Exception {
		if (obsolete)
			return;

		String message = "Exception Connecting:" + Util.exception(t);
		
		if (user.getServer() == null)
			user.disconnect(message);
		else
			user.sendMessage(ChatColor.RED + message);
	}

	@Override
	public void connected(ChannelWrapper channel) throws Exception {
		this.ch = channel;

		this.handshakeHandler = new ForgeServerHandler(user, ch, target);
		Handshake originalHandshake = user.getPendingConnection().getHandshake();
		Handshake copiedHandshake = new Handshake(originalHandshake.getProtocol(), originalHandshake.getHost(), originalHandshake.getPort(), NetworkState.LOGIN);

		if (BungeeCord.getInstance().config.isIpForward()) {
			String newHost = copiedHandshake.getHost() + "\00" + user.getAddress().getHostString() + "\00" + user.getUUID();

			// Handle properties.
			LoginResult.Property[] properties = new LoginResult.Property[0];

			LoginResult profile = user.getPendingConnection().getLoginProfile();
			if (profile != null && profile.getProperties() != null && profile.getProperties().length > 0) {
				properties = profile.getProperties();
			}

			if (user.getForgeClientHandler().isFmlTokenInHandshake()) {
				// Get the current properties and copy them into a slightly bigger array.
				LoginResult.Property[] newp = Arrays.copyOf(properties, properties.length + 2);
				// Add a new profile property that specifies that this user is a Forge user.
				newp[newp.length - 2] = new LoginResult.Property(ForgeConstants.FML_LOGIN_PROFILE, "true", null);
				// If we do not perform the replacement, then the IP Forwarding code in Spigot
				// et. al. will try to split on this prematurely.
				newp[newp.length - 1] = new LoginResult.Property(ForgeConstants.EXTRA_DATA, user.getExtraDataInHandshake().replaceAll("\0", "\1"), "");
				// All done.
				properties = newp;
			}
			// If we touched any properties, then append them
			if (properties.length > 0) {
				newHost += "\00" + BungeeCord.getInstance().gson.toJson(properties);
			}

			copiedHandshake.setHost(newHost);
		} else if (!user.getExtraDataInHandshake().isEmpty()) {
			// Restore the extra data
			copiedHandshake.setHost(copiedHandshake.getHost() + user.getExtraDataInHandshake());
		}

		if (!originalHandshake.getProtocol().isLegacy()) {
			channel.write(copiedHandshake);
			channel.setConnectionStatus(NetworkState.LOGIN);
			thisState = State.LOGIN_SUCCESS;
			channel.write(new LoginRequest(user.getName()));
		} else {
			LegacyLoginRequest lr = new LegacyLoginRequest();
			lr.setHost(copiedHandshake.getHost());
			lr.setPort(copiedHandshake.getPort());
			lr.setProtocolVersion(copiedHandshake.getProtocol().version);
			lr.setUserName(user.getName());
			thisState = State.LOGIN_REQUEST;
			channel.write(lr);
		}
	}

	@Override
	public void disconnected(ChannelWrapper channel) throws Exception {
		user.getPendingConnects().remove(target);
	}

	@Override
	public void handle(PacketWrapper packet) throws Exception {
		if (packet.packet == null)
			throw new QuietException("Unexpected packet received during server login process!\n" +
				"id: " + packet.id +
				", dump(16): " + BufUtil.dump(packet.buf, 16) +
				", state: " + thisState.name() +
				", cs: " + ch.getConnectionStatus().name());
	}

	@Override
	public void handle(LoginSuccess loginSuccess) throws Exception {
		Preconditions.checkState(thisState == State.LOGIN_SUCCESS, "Not expecting LOGIN_SUCCESS");
		ch.setConnectionStatus(NetworkState.GAME);
		thisState = State.LOGIN;

		// Only reset the Forge client when:
		// 1) The user is switching servers (so has a current server)
		// 2) The handshake is complete
		// 3) The user is currently on a modded server (if we are on a vanilla server,
		// we may be heading for another vanilla server, so we don't need to reset.)
		//
		// user.getServer() gets the user's CURRENT server, not the one we are trying
		// to connect to.
		//
		// We will reset the connection later if the current server is vanilla, and
		// we need to switch to a modded connection. However, we always need to reset
		// the
		// connection when we have a modded server regardless of where we go - doing it
		// here makes sense.
		if (user.getServer() != null && user.getForgeClientHandler().isHandshakeComplete() && user.getServer().isForgeServer())
			user.getForgeClientHandler().resetHandshake();

		throw CancelSendSignal.INSTANCE;
	}

	@Override
	public void handle(SetCompression setCompression) throws Exception {
		ch.setCompressionThreshold(setCompression.getThreshold());
	}

	@Override
	public void handle(Login login) throws Exception {
		if(user.getPendingConnection().getProtocol().isLegacy() && thisState != State.LOGIN)
			thisState = State.LOGIN;
		Preconditions.checkState(thisState == State.LOGIN, "Not expecting " + thisState.name());

		ServerConnection server = new ServerConnection(ch, target);
		ServerConnectedEvent event = new ServerConnectedEvent(user, server);
		bungee.getPluginManager().callEvent(event);

		ch.write(BungeeCord.getInstance().registerChannels(user.getPendingConnection().getProtocol()));

		Queue<Packet> packetQueue = target.getPacketQueue();
		synchronized (packetQueue) {
			while (!packetQueue.isEmpty())
				ch.write(packetQueue.poll());
		}

		for (PluginMessage message : user.getPendingConnection().getRelayMessages()) {
			ch.write(message);
		}

		if (user.getSettings() != null) 
			ch.write(user.getSettings());

		if (user.getForgeClientHandler().getClientModList() == null && !user.getForgeClientHandler().isHandshakeComplete()) // Vanilla
			user.getForgeClientHandler().setHandshakeComplete();

		if (user.getServer() == null) {
			// Once again, first connection
			user.setClientEntityId(login.getEntityId());
			user.setServerEntityId(login.getEntityId());

			// Set tab list size, this sucks balls, TODO: what shall we do about packet
			// mutability
			// Forge allows dimension ID's > 127

			Login modLogin;
			if (handshakeHandler != null && handshakeHandler.isServerForge())
				modLogin = new Login(login.getEntityId(), login.getGameMode(), login.getDimension(), login.getDifficulty(), (byte) user.getPendingConnection().getListener().getTabListSize(), login.getLevelType(), login.isReducedDebugInfo(), login.getWorldHeight());
			else
				modLogin = new Login(login.getEntityId(), login.getGameMode(), (byte) login.getDimension(), login.getDifficulty(), (byte) user.getPendingConnection().getListener().getTabListSize(), login.getLevelType(), login.isReducedDebugInfo(), login.getWorldHeight());

			user.unsafe().sendPacket(modLogin);

			if (user.getPendingConnection().getProtocol().olderThan(Protocol.MC_1_8_0)) {
				MinecraftOutput out = new MinecraftOutput();
				out.writeStringUTF8WithoutLengthHeaderBecauseDinnerboneStuffedUpTheMCBrandPacket(ProxyServer.getInstance().getName() + " (" + ProxyServer.getInstance().getVersion() + ")");
				user.unsafe().sendPacket(new PluginMessage("MC|Brand", out.toArray(), handshakeHandler.isServerForge()));
			} else {
				ByteBuf brand = ByteBufAllocator.DEFAULT.heapBuffer();
				Packet.writeString(bungee.getName() + " (" + bungee.getVersion() + ")", brand);
				user.unsafe().sendPacket(new PluginMessage(user.getPendingConnection().getProtocol().newerOrEqual(Protocol.MC_1_13_0) ? "minecraft:brand" : "MC|Brand", Packet.toArray(brand), handshakeHandler.isServerForge()));
				brand.release();
			}

			user.setDimension(login.getDimension());
		} else {
			user.getServer().setObsolete(true);
			user.getTabListHandler().onServerChange();

			Scoreboard serverScoreboard = user.getServerSentScoreboard();
			for (Objective objective : serverScoreboard.getObjectives()) {
				// user.unsafe().sendPacket( new ScoreboardObjective( objective.getName(),
				// objective.getValue(), ScoreboardObjective.HealthDisplay.fromString(
				// objective.getType() ), (byte) 1 ) );
				user.unsafe().sendPacket(new ScoreboardObjective(objective.getName(), objective.getValue(), objective.getType() == null ? null : ScoreboardObjective.HealthDisplay.fromString(objective.getType()), (byte) 1)); // Travertine - 1.7
			}
			for (Score score : serverScoreboard.getScores())
				user.unsafe().sendPacket(new ScoreboardScore(score.getItemName(), (byte) 1, score.getScoreName(), score.getValue()));
			
			for (Team team : serverScoreboard.getTeams())
				user.unsafe().sendPacket(new net.md_5.bungee.protocol.packet.Team(team.getName()));
			
			serverScoreboard.clear();
			
			// Send remove bossbar packet
			for (UUID bossbar : user.getSentBossBars())
				user.unsafe().sendPacket(new net.md_5.bungee.protocol.packet.BossBar(bossbar, 1));
			
			user.getSentBossBars().clear();

			// Update debug info from login packet
			if(!user.getPendingConnection().isLegacy())
				user.unsafe().sendPacket(new EntityStatus(user.getClientEntityId(), login.isReducedDebugInfo() ? EntityStatus.DEBUG_INFO_REDUCED : EntityStatus.DEBUG_INFO_NORMAL));

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
		if (!user.isActive()) {
			server.disconnect("Quitting");
			// Silly server admins see stack trace and die
			bungee.getLogger().warning("No client connected for pending server!");
			return;
		}

		// Add to new server
		// TODO: Move this to the connected() method of DownstreamBridge
		target.addPlayer(user);
		user.getPendingConnects().remove(target);
		user.setServerJoinQueue(null);
		user.setDimensionChange(false);

		user.setServer(server);
		ch.getHandle().pipeline().get(HandlerBoss.class).setHandler(new DownstreamBridge(bungee, user, server));

		bungee.getPluginManager().callEvent(new ServerSwitchEvent(user));

		thisState = State.FINISHED;

		throw CancelSendSignal.INSTANCE;
	}

	@Override
	public void handle(EncryptionRequest encryptionRequest) throws Exception {
		Preconditions.checkState(thisState == State.LOGIN_REQUEST, "Not expecting LOGIN_REQUEST");

		PublicKey pub = EncryptionUtil.getPubkey(encryptionRequest);
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		kg.init(128);
		secret = kg.generateKey();

		thisState = State.LOGIN_SUCCESS;

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
		Preconditions.checkState(thisState == State.LOGIN_SUCCESS, "Not expecting LOGIN_SUCCESS");

		BungeeCipher encrypt = EncryptionUtil.getCipher(false, secret);
		ch.addBefore(PipelineUtil.PACKET_DEC, PipelineUtil.DECRYPT, new CipherDecoder(encrypt));

		thisState = State.LOGIN;

		ch.write(new LegacyClientCommand(0));

		throw CancelSendSignal.INSTANCE;
	}

	@Override
	public void handle(Kick kick) throws Exception {
		ServerInfo def = user.updateAndGetNextServer(target);
		ServerKickEvent event = new ServerKickEvent(user, target, ComponentSerializer.parse(kick.getMessage()), def, ServerKickEvent.State.CONNECTING);
		if (event.getKickReason().toLowerCase(Locale.ROOT).contains("outdated") && def != null) {
			// Pre cancel the event if we are going to try another server
			event.setCancelled(true);
		}
		bungee.getPluginManager().callEvent(event);
		if (event.isCancelled() && event.getCancelServer() != null) {
			obsolete = true;
			user.connect(event.getCancelServer(), ServerConnectEvent.Reason.KICK_REDIRECT);
			throw CancelSendSignal.INSTANCE;
		}

		String message = bungee.getTranslation("connect_kick", target.getName(), event.getKickReason());
		if (user.isDimensionChange())
			user.disconnect(message);
		else
			user.sendMessage(message);

		throw CancelSendSignal.INSTANCE;
	}

	@Override
	public void handle(PluginMessage pluginMessage) throws Exception {
		if (BungeeCord.getInstance().config.isForgeSupport()) {
			if (pluginMessage.getTag().equals(ForgeConstants.FML_REGISTER)) {
				Set<String> channels = ForgeUtils.readRegisteredChannels(pluginMessage);
				boolean isForgeServer = false;
				for (String channel : channels) {
					if (channel.equals(ForgeConstants.FML_HANDSHAKE_TAG)) {
						// If we have a completed handshake and we have been asked to register a FML|HS
						// packet, let's send the reset packet now. Then, we can continue the message
						// sending.
						// The handshake will not be complete if we reset this earlier.
						if (user.getServer() != null && user.getForgeClientHandler().isHandshakeComplete()) {
							user.getForgeClientHandler().resetHandshake();
						}

						isForgeServer = true;
						break;
					}
				}

				if (isForgeServer && !this.handshakeHandler.isServerForge()) {
					// We now set the server-side handshake handler for the client to this.
					handshakeHandler.setServerAsForgeServer();
					user.setForgeServerHandler(handshakeHandler);
				}
			}

			if (pluginMessage.getTag().equals(ForgeConstants.FML_HANDSHAKE_TAG) || pluginMessage.getTag().equals(ForgeConstants.FORGE_REGISTER)) {
				this.handshakeHandler.handle(pluginMessage);

				// We send the message as part of the handler, so don't send it here.
				throw CancelSendSignal.INSTANCE;
			}
		}

		// We have to forward these to the user, especially with Forge as stuff might
		// break
		// This includes any REGISTER messages we intercepted earlier.
		user.unsafe().sendPacket(pluginMessage);
	}

	@Override
	public String toString() {
		return "[" + user.getName() + "] <-> ServerConnector [" + target.getName() + "]";
	}
}
