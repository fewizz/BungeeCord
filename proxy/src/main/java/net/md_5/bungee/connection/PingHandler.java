package net.md_5.bungee.connection;

import com.google.gson.Gson;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

		if (!protocol.isLegacy()) {
			channel.setConnectionState(NetworkState.STATUS);
			channel.write(new StatusRequest());
		} 
		else {
			LegacyStatusRequest lsr = new LegacyStatusRequest();
			lsr.setBranding("MC");
			lsr.setIp("");
			lsr.setOlderOrEqual_1_5(protocol.olderOrEqual(Protocol.MC_1_5_2));
			lsr.setProtocolVersion(protocol.version);
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
	@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
	public void handle(StatusResponse statusResponse) throws Exception {
		Gson gson = protocol == Protocol.MC_1_7_2 ? BungeeCord.getInstance().gsonLegacy : BungeeCord.getInstance().gson;
		callback.done(gson.fromJson(statusResponse.getResponse(), ServerPing.class), null);
		channel.close();
	}

	@Override
	public void handle(Kick kick) throws Exception {
		Kick.StatusResponce r = new Kick.StatusResponce();
		r.parse(kick.getMessage());

		callback.done(new ServerPing(new ServerPing.Protocol("", r.protocolVersion), new ServerPing.Players(r.max, r.players, new ServerPing.PlayerInfo[0]), r.motd, (Favicon) null), null);
	}

	@Override
	public String toString() {
		return "[Ping Handler] -> " + target.getName();
	}
}
