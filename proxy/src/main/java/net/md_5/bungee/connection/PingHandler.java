package net.md_5.bungee.connection;

import com.google.gson.Gson;

import io.netty.channel.Channel;
import lombok.NonNull;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtil;
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

public class PingHandler extends PacketHandler {
	@NonNull
	private final ServerInfo target;
	@NonNull
	private final Callback<ServerPing> callback;
	final ChannelWrapper ch;
	
	public PingHandler(Channel ch, ServerInfo info, Callback<ServerPing> cb) {
		this.ch = PipelineUtil.getChannelWrapper(ch);
		this.target = info;
		this.callback = cb;
	}

	@Override
	public void connected() throws Exception {
		Protocol protocol = ch.getProtocol();
		if (protocol.isModern()) {
			ch.write(Handshake.builder()
				.protocolVersion(protocol.version)
				.host(target.getAddress().getHostString())
				.port(target.getAddress().getPort())
				.requestedNetworkState(NetworkState.STATUS)
				.build()
			);
			ch.setNetworkState(NetworkState.STATUS);
			ch.write(new StatusRequest());
		} 
		else {
			ch.write(LegacyStatusRequest.builder()
				.branding("MC|PingHost")
				.host(target.getAddress().getHostString())
				.port(target.getAddress().getPort())
				.protocolVersion(protocol.version)
				.build()
			);
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
		Gson gson = ch.getProtocol() == Protocol.MC_1_7_2 ? BungeeCord.getInstance().gsonLegacy : BungeeCord.getInstance().gson;
		callback.done(gson.fromJson(statusResponse.getResponse(), ServerPing.class), null);
		ch.close();
	}

	@Override
	public void handle(Kick kick) throws Exception {
		Kick.StatusResponce responce = new Kick.StatusResponce(kick.getMessage());

		callback.done(
			ServerPing.builder()
				.version(new ServerPing.Protocol("", responce.protocolVersion))
				.players(new ServerPing.Players(responce.max, responce.players, new ServerPing.PlayerInfo[0]))
				.description(new TextComponent(TextComponent.fromLegacyText(responce.motd)))
				.build()
			,
			null
		);
		
		ch.close();
	}

	@Override
	public String toString() {
		return "[Ping Handler] [-> " + target.getName() + "]";
	}
}
