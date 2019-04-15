package net.md_5.bungee.connection;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.NetworkState;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LegacyStatusRequest;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.util.BufUtil;
import net.md_5.bungee.util.QuietException;

@RequiredArgsConstructor
public class PingHandler extends PacketHandler {

	private final ServerInfo target;
	private final Callback<ServerPing> callback;
	private final Protocol protocol;
	private ChannelWrapper channel;

	@Override
	public void connected(ChannelWrapper channel) throws Exception {
		this.channel = channel;

		if (protocol.isModern()) {
			channel.write( new Handshake( protocol.version, target.getAddress().getHostString(), target.getAddress().getPort(), NetworkState.STATUS ) );
			channel.setNetworkState(NetworkState.STATUS);
			channel.write(new StatusRequest());
		} 
		else {
			LegacyStatusRequest lsr = new LegacyStatusRequest();
			if(protocol.newerThan(Protocol.MC_1_5_2)) {
				lsr.setBranding("MC|PingHost");
				lsr.setHost("");
				lsr.setProtocolVersion(protocol.version);
			}
			channel.write(lsr);
		}
	}

	@Override
	public void exception(Throwable t) throws Exception {
		callback.done(null, t);
	}

	@Override
	public void handle(PacketWrapper packet) throws Exception {
		if (packet.packet == null)
			throw new QuietException("Unexpected packet received during ping process! " + BufUtil.dump(packet.content(), 16));
	}

	@Override
	public void handle(StatusResponse statusResponse) throws Exception {
		Gson gson = protocol == Protocol.MC_1_7_2 ? BungeeCord.getInstance().gsonLegacy : BungeeCord.getInstance().gson;
		callback.done(gson.fromJson(statusResponse.getResponse(), ServerPing.class), null);
		
		channel.close();
	}

	@Override
	public void handle(Kick kick) throws Exception {
		Kick.StatusResponce responce = new Kick.StatusResponce(kick.getMessage());

		callback.done(
			new ServerPing(
				new ServerPing.Protocol("", responce.protocolVersion),
				new ServerPing.Players(responce.max, responce.players, new ServerPing.PlayerInfo[0]),
				responce.motd,
				(Favicon) null
			),
			null
		);
		
		channel.close();
	}

	@Override
	public String toString() {
		return "[Ping Handler] -> " + target.getName();
	}
}
