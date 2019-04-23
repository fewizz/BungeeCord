package net.md_5.bungee.legacy;

import java.util.Map;

import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.netty.ChannelWrapper;

public class LegacyUserConnection extends UserConnection<LegacyInitialHandler> {

	public LegacyUserConnection(ChannelWrapper ch, LegacyInitialHandler pendingConnection) {
		super(ch, pendingConnection);
	}

	@Override
	public boolean isForgeUser() {
		return getPendingConnection().getForgeLogin() != null;
	}

	@Override
	public Map<String, String> getModList() {
		return null;
	}

	@Override
	protected LegacyServerConnector createServerConnector(ChannelWrapper ch, BungeeServerInfo target) {
		return new LegacyServerConnector(ch, this, target);
	}

}
