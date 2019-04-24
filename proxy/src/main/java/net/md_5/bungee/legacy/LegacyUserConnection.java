package net.md_5.bungee.legacy;

import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;

import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.ServerConnector;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.score.Objective;
import net.md_5.bungee.api.score.Score;
import net.md_5.bungee.api.score.Scoreboard;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.MinecraftOutput;
import net.md_5.bungee.protocol.packet.BossBar;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;

public class LegacyUserConnection extends UserConnection<LegacyInitialHandler> {

	public LegacyUserConnection(ChannelWrapper ch, LegacyInitialHandler pendingConnection) {
		super(ch, pendingConnection);
	}

	@Override
	public boolean isForgeUser() {
		return pendingConnection.getForgeLogin() != null;
	}

	@Override
	public Map<String, String> getModList() {
		return null;
	}

	@Override
	protected LegacyServerConnector createServerConnector(ChannelWrapper ch, BungeeServerInfo target) {
		return new LegacyServerConnector(ch, this, target);
	}

	@Override
	protected void onServerConnectorLoggedIn0(ServerConnector<?> sc0, ServerConnection con) {
		LegacyServerConnector sc = (LegacyServerConnector) sc0;
		Login login = Preconditions.checkNotNull(sc.getLogin());
		
		if (server == null) {
			// Once again, first connection
			setClientEntityId(login.getEntityId());
			setServerEntityId(login.getEntityId());
	
			// Set tab list size, this sucks balls, TODO: what shall we do about packet
			// mutability
			// Forge allows dimension ID's > 127
			unsafe().sendPacket(
				login.clone()
				.setMaxPlayers(pendingConnection.listener.getTabListSize())
			);

			MinecraftOutput out = new MinecraftOutput();
			out.writeStringUTF8WithoutLengthHeaderBecauseDinnerboneStuffedUpTheMCBrandPacket(bungee.getName() + " (" + bungee.getVersion() + ")");
			unsafe().sendPacket(new PluginMessage("MC|Brand", out.toArray(), false));
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
				unsafe().sendPacket(new BossBar(bossbar, 1));
			
			getSentBossBars().clear();
	
			//user.setDimensionChange(true);
			Respawn respawn = Respawn.builder()
				.dimension(login.getDimension())
				.difficulty(login.getDifficulty())
				.gameMode(login.getGameMode())
				.worldHeight(login.getWorldHeight())
				.levelType(login.getLevelType())
				.build();
			
			if (login.getDimension() == getDimension()) {
				unsafe().sendPacket(respawn.clone().setDimension(login.getDimension() >= 0 ? -1 : 0));
			}
	
			setServerEntityId(login.getEntityId());
			unsafe().sendPacket(respawn);
	
			// Remove from old servers
			server.disconnect("Quitting");
		}
		
		setDimension(login.getDimension());
	}

}
