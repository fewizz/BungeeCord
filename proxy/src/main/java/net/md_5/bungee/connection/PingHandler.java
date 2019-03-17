package net.md_5.bungee.connection;

import com.google.gson.Gson;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.PacketEncoder;
import net.md_5.bungee.protocol.ModernMinecraftPacketDecoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolGen;
import net.md_5.bungee.protocol.ProtocolVersion;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.protocol.packet.old.StatusRequestOld;
import net.md_5.bungee.protocol.packet.old.StatusResponseOld;
import net.md_5.bungee.util.BufUtil;
import net.md_5.bungee.util.QuietException;

@RequiredArgsConstructor
public class PingHandler extends PacketHandler
{

    private final ServerInfo target;
    private final Callback<ServerPing> callback;
    private final ProtocolVersion protocol;
    private ChannelWrapper channel;

    @Override
    public void connected(ChannelWrapper channel) throws Exception
    {
        this.channel = channel;
        //MinecraftPacketEncoder encoder = new MinecraftPacketEncoder( Protocol.HANDSHAKE, false, protocol );

        //channel.getHandle().pipeline().addAfter( PipelineUtils.FRAME_DECODER, PipelineUtils.PACKET_DECODER, new ModernMinecraftPacketDecoder( Protocol.STATUS, false, ProxyServer.getInstance().getProtocolVersion() ) );
        //channel.getHandle().pipeline().addAfter( PipelineUtils.FRAME_PREPENDER, PipelineUtils.PACKET_ENCODER, encoder );

        //channel.write( new Handshake( protocol, target.getAddress().getHostString(), target.getAddress().getPort(), 1 ) );

        //encoder.setProtocol( Protocol.STATUS );
        if(protocol.newerThan(ProtocolVersion.MC_1_6_4)) {
        	channel.setProtocol(Protocol.STATUS);
        	channel.write( new StatusRequest() );
        }
        else
        	channel.write( new StatusRequestOld() );
    }

    @Override
    public void exception(Throwable t) throws Exception
    {
        callback.done( null, t );
    }

    @Override
    public void handle(PacketWrapper packet) throws Exception
    {
        if ( packet.packet == null )
        {
            throw new QuietException( "Unexpected packet received during ping process! " + BufUtil.dump( packet.buf, 16 ) );
        }
    }

    @Override
    @SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
    public void handle(StatusResponse statusResponse) throws Exception
    {
        Gson gson = protocol == ProtocolVersion.MC_1_7_2 ? BungeeCord.getInstance().gsonLegacy : BungeeCord.getInstance().gson;
        callback.done( gson.fromJson( statusResponse.getResponse(), ServerPing.class ), null );
        channel.close();
    }
    
    @Override
    public void handle(StatusResponseOld resp) {
    	callback.done(
			new ServerPing(
				new ServerPing.Protocol("", resp.protocolVersion),
				new ServerPing.Players(resp.max, resp.players, new ServerPing.PlayerInfo[0]),
				resp.motd,
				(Favicon)null
			),
			null
		);
    }

    @Override
    public String toString()
    {
        return "[Ping Handler] -> " + target.getName();
    }
}
