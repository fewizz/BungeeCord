package net.md_5.bungee.modern;

import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.score.Objective;
import net.md_5.bungee.api.score.Score;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.forge.ForgeClientHandler;
import net.md_5.bungee.forge.ForgeConstants;
import net.md_5.bungee.forge.ForgeServerHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftOutput;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.EntityStatus;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;

public class ModernUserConnection extends UserConnection<ModernInitialHandler> {
	@Getter
	public final ForgeClientHandler forgeClientHandler;
	@Getter
	@Setter
	private ForgeServerHandler forgeServerHandler;
	
	public ModernUserConnection(ChannelWrapper ch, ModernInitialHandler pendingConnection) {
		super(ch, pendingConnection);
		
		forgeClientHandler = new ForgeClientHandler(this);

		// No-config FML handshake marker.
		// Set whether the connection has a 1.8 FML marker in the handshake.
		if (this.getPendingConnection().getExtraDataInHandshake().contains(ForgeConstants.FML_HANDSHAKE_TOKEN))
			forgeClientHandler.setFmlTokenInHandshake(true);
	}
	
	@Override
	public void sendData(String channel, byte[] data) {
		unsafe().sendPacket(new PluginMessage(channel, data, forgeClientHandler.isForgeUser()));
	}
	
	@Override
	public boolean isForgeUser() {
		return forgeClientHandler.isForgeUser();
	}

	@Override
	public Map<String, String> getModList() {
		if (forgeClientHandler.getClientModList() == null) {
			// Return an empty map, rather than a null, if the client hasn't got any mods,
			// or is yet to complete a handshake.
			return ImmutableMap.of();
		}

		return ImmutableMap.copyOf(forgeClientHandler.getClientModList());
	}

	public @NonNull String getExtraDataInHandshake() {
		return pendingConnection.getExtraDataInHandshake();
	}

	@Override
	protected ModernServerConnector createServerConnector(ChannelWrapper ch, BungeeServerInfo target) {
		return new ModernServerConnector(ch, this, target);
	}

	@Override
	protected void onServerConnectorLoggedIn0(ServerConnector<?> sc0, ServerConnection con) {
		ModernServerConnector sc = (ModernServerConnector) sc0;
		Login login = Preconditions.checkNotNull(sc.getLogin());
		ForgeServerHandler handshakeHandler = sc.getHandshakeHandler();
		
		if (forgeClientHandler.getClientModList() == null && !forgeClientHandler.isHandshakeComplete()) // Vanilla
			forgeClientHandler.setHandshakeComplete();
		
		if (server == null) {
			// Once again, first connection
			setClientEntityId(login.getEntityId());
			setServerEntityId(login.getEntityId());

			// Set tab list size, this sucks balls, TODO: what shall we do about packet
			// mutability
			// Forge allows dimension ID's > 127
			unsafe().sendPacket(login.clone().setMaxPlayers(pendingConnection.listener.getTabListSize()));

			if (pendingConnection.getProtocol().olderThan(Protocol.MC_1_8_0)) {
				MinecraftOutput out = new MinecraftOutput();
				out.writeStringUTF8WithoutLengthHeaderBecauseDinnerboneStuffedUpTheMCBrandPacket(bungee.getName() + " (" + bungee.getVersion() + ")");
				unsafe().sendPacket(new PluginMessage("MC|Brand", out.toArray(), handshakeHandler.isServerForge()));
			} else {
				ByteBuf brand = ByteBufAllocator.DEFAULT.heapBuffer();
				DefinedPacket.writeString(
					bungee.getName() + " (" + bungee.getVersion() + ")",
					brand
				);
				unsafe().sendPacket(
					new PluginMessage(
						pendingConnection.getProtocol().newerOrEqual(Protocol.MC_1_13_0) ?
						"minecraft:brand"
						:
						"MC|Brand"
						,
						DefinedPacket.toArray(brand), handshakeHandler.isServerForge()
					)
				);
				brand.release();
			}
		} else {
			getTabListHandler().onServerChange();

			Scoreboard serverScoreboard = getServerSentScoreboard();
			for (Objective objective : serverScoreboard.getObjectives())
				unsafe().sendPacket(new ScoreboardObjective(objective.getName(), objective.getValue(), objective.getType() == null ? null : ScoreboardObjective.HealthDisplay.fromString(objective.getType()), (byte) 1)); // Travertine - 1.7
			
			for (Score score : serverScoreboard.getScores())
				unsafe().sendPacket(new ScoreboardScore(score.getItemName(), (byte) 1, score.getScoreName(), score.getValue()));
			
			for (Team team : serverScoreboard.getTeams())
				unsafe().sendPacket(new net.md_5.bungee.protocol.packet.Team(team.getName()));
			
			serverScoreboard.clear();
			
			// Send remove bossbar packet
			for (UUID bossbar : getSentBossBars())
				unsafe().sendPacket(new net.md_5.bungee.protocol.packet.BossBar(bossbar, 1));
			
			getSentBossBars().clear();

			// Update debug info from login packet
			unsafe().sendPacket(new EntityStatus(getClientEntityId(), login.isReducedDebugInfo() ? EntityStatus.DEBUG_INFO_REDUCED : EntityStatus.DEBUG_INFO_NORMAL));

			//user.setDimensionChange(true);
			Respawn respawn = Respawn.builder()
				.dimension(login.getDimension())
				.difficulty(login.getDifficulty())
				.gameMode(login.getGameMode())
				.worldHeight(login.getWorldHeight())
				.levelType(login.getLevelType())
				.build();
			
			if (login.getDimension() == getDimension())
				unsafe().sendPacket(respawn.clone().setDimension(login.getDimension() >= 0 ? -1 : 0));

			setServerEntityId(login.getEntityId());
			unsafe().sendPacket(respawn);

			// Remove from old servers
			server.disconnect("Quitting");
		}
		
		setDimension(login.getDimension());
	}
}
