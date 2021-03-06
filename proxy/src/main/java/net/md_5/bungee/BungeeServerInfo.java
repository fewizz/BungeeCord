package net.md_5.bungee;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

import com.google.common.base.Preconditions;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.ToString;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.connection.PingHandler;
import net.md_5.bungee.netty.NettyUtil;
import net.md_5.bungee.netty.PipelineUtil;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.Side;
import net.md_5.bungee.protocol.packet.PluginMessage;

@RequiredArgsConstructor
@ToString(of =
{
    "name", "address", "restricted"
})
public class BungeeServerInfo implements ServerInfo
{

    @Getter
    private final String name;
    @Getter
    private final InetSocketAddress address;
    private final Collection<ProxiedPlayer> players = new ArrayList<>();
    @Getter
    private final String motd;
    @Getter
    private final boolean restricted;
    @Getter
    private final Queue<DefinedPacket> packetQueue = new LinkedList<>();
    @Getter
    private final Boolean ipForward;
    @Getter
    private final Boolean forgeSupport;

    @Synchronized("players")
    public void addPlayer(ProxiedPlayer player)
    {
        players.add( player );
    }

    @Synchronized("players")
    public void removePlayer(ProxiedPlayer player)
    {
        players.remove( player );
    }

    @Synchronized("players")
    @Override
    public Collection<ProxiedPlayer> getPlayers()
    {
        return Collections.unmodifiableCollection( new HashSet<>( players ) );
    }

    @Override
    public boolean canAccess(CommandSender player)
    {
        Preconditions.checkNotNull( player, "player" );
        return !restricted || player.hasPermission( getPermission() );
    }

    @Override
    public boolean equals(Object obj)
    {
        return ( obj instanceof ServerInfo ) && Objects.equals( getAddress(), ( (ServerInfo) obj ).getAddress() );
    }

    @Override
    public int hashCode()
    {
        return address.hashCode();
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        sendData( channel, data, true );
    }

    // TODO: Don't like this method
    @Override
    public boolean sendData(String channel, byte[] data, boolean queue)
    {
        Preconditions.checkNotNull( channel, "channel" );
        Preconditions.checkNotNull( data, "data" );

        synchronized ( packetQueue )
        {
            Server server = ( players.isEmpty() ) ? null : players.iterator().next().getServer();
            if ( server != null )
            {
                server.sendData( channel, data );
                return true;
            } else if ( queue )
            {
                packetQueue.add( new PluginMessage( channel, data, false ) );
            }
            return false;
        }
    }

    @Override
    public void ping(final Callback<ServerPing> callback) {
        ping( callback, ProxyServer.getInstance().getProtocolVersion() );
    }

    public void ping(final Callback<ServerPing> callback, final Protocol protocolVersion) {
        Preconditions.checkNotNull( callback, "callback" );

        new Bootstrap()
            .channel(NettyUtil.bestSocketChannel())
            .group( BungeeCord.getInstance().eventLoops )
            .handler( new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					PipelineUtil.addHandlers(ch, protocolVersion, Side.SERVER, new PingHandler( BungeeServerInfo.this, callback, protocolVersion ));
				}
			} )
            .option( ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000 ) // TODO: Configurable
            .remoteAddress( getAddress() )
            .connect()
            .addListener( (ChannelFuture future) -> {
            	if ( !future.isSuccess() )
            		callback.done( null, future.cause() );
            });
    }

	@Override
	public String getPermission() {
		return "bungeecord.server." + name;
	}
}
