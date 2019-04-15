package net.md_5.bungee.legacy;

import java.util.Map;

import lombok.NonNull;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.netty.ChannelWrapper;

public class LegacyUserConnection extends UserConnection<LegacyInitialHandler> {

	public LegacyUserConnection(@NonNull ProxyServer bungee, @NonNull ChannelWrapper ch, @NonNull String name,
			LegacyInitialHandler pendingConnection) {
		super(bungee, ch, name, pendingConnection);
	}

	@Override
	public boolean isForgeUser() {
		return getPendingConnection().getForgeLogin() != null;
	}

	@Override
	public Map<String, String> getModList() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected LegacyServerConnector createServerConnector(BungeeServerInfo target) {
		return new LegacyServerConnector(this, target);
	}

}
