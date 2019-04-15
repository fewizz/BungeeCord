package net.md_5.bungee;

import java.util.Map;

import lombok.NonNull;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.connection.LegacyInitialHandler;
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
		// TODO Auto-generated method stub
		return null;
	}

}
