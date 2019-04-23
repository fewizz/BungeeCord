package net.md_5.bungee.modern;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.forge.ForgeClientHandler;
import net.md_5.bungee.forge.ForgeConstants;
import net.md_5.bungee.forge.ForgeServerHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.packet.PluginMessage;

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
}
