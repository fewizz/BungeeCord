package net.md_5.bungee.conf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.google.common.base.Preconditions;

import gnu.trove.map.TMap;
import lombok.Getter;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.util.CaseInsensitiveMap;
import net.md_5.bungee.util.CaseInsensitiveSet;

/**
 * Core configuration for the proxy.
 */
@Getter
public class Configuration implements ProxyConfig
{

    /**
     * Time before users are disconnected due to no network activity.
     */
    private int timeout = 30000;
    /**
     * UUID used for metrics.
     */
    private String uuid = UUID.randomUUID().toString();
    /**
     * Set of all listeners.
     */
    private Collection<ListenerInfo> listeners;
    /**
     * Set of all servers.
     */
    private TMap<String, ServerInfo> servers;
    /**
     * Should we check minecraft.net auth.
     */
    private boolean onlineMode = true;
    /**
     * Whether we log proxy commands to the proxy log
     */
    private boolean logCommands;
    private boolean logPings = true;
    private int playerLimit = -1;
    private Collection<String> disabledCommands;
    private int throttle = 4000;
    private int throttleLimit = 3;
    private boolean ipForward;
    private Favicon favicon;
    private int compressionThreshold = 256;
    private String customServerName = "HexaCord";
    private boolean alwaysHandlePackets = false;
    private boolean preventProxyConnections;
    private boolean forgeSupport;
    private boolean metrics = false;
    
    private boolean initialProtocol = false;
    private boolean protocolChange = false;
    private boolean handshake = false;
    private boolean serverChange = false;
    private boolean logErrors = false;
    private boolean logQueryErrors = false;

    public void load()
    {
        ConfigurationAdapter adapter = ProxyServer.getInstance().getConfigurationAdapter();
        adapter.load();

        File fav = new File( "server-icon.png" );
        if ( fav.exists() ) {
            try {
                favicon = Favicon.create( ImageIO.read( fav ) );
            } catch ( IOException | IllegalArgumentException ex )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load server icon", ex );
            }
        }

        listeners = adapter.getListeners();
        timeout = adapter.getInt( "timeout", timeout );
        uuid = adapter.getString( "stats", uuid );
        onlineMode = adapter.getBoolean( "online_mode", onlineMode );
        logCommands = adapter.getBoolean( "log_commands", logCommands );
        logPings = adapter.getBoolean( "log_pings", logPings );
        playerLimit = adapter.getInt( "player_limit", playerLimit );
        throttle = adapter.getInt( "connection_throttle", throttle );
        throttleLimit = adapter.getInt( "connection_throttle_limit", throttleLimit );
        ipForward = adapter.getBoolean( "ip_forward", ipForward );
        compressionThreshold = adapter.getInt( "network_compression_threshold", compressionThreshold );
        customServerName = adapter.getString( "custom_server_name", "HexaCord" );
        alwaysHandlePackets = adapter.getBoolean( "always_handle_packets", false );
        preventProxyConnections = adapter.getBoolean( "prevent_proxy_connections", preventProxyConnections );
        forgeSupport = adapter.getBoolean( "forge_support", forgeSupport );
        metrics = adapter.getBoolean( "metrics", metrics );

        disabledCommands = new CaseInsensitiveSet( (Collection<String>) adapter.getList( "disabled_commands", Arrays.asList( "disabledcommandhere" ) ) );
        
        initialProtocol = adapter.getBoolean( "debug.initial_protocol", initialProtocol);
        protocolChange = adapter.getBoolean("debug.protocol_change", protocolChange);
        handshake = adapter.getBoolean("debug.handshake", handshake);
        serverChange = adapter.getBoolean("debug.server_change", serverChange);
        logQueryErrors = adapter.getBoolean("debug.log_query_errors", logQueryErrors);
        logErrors = adapter.getBoolean("debug.log_errors", logErrors);

        Preconditions.checkArgument( listeners != null && !listeners.isEmpty(), "No listeners defined." );

        Map<String, ServerInfo> newServers = adapter.getServers();
        Preconditions.checkArgument( newServers != null && !newServers.isEmpty(), "No servers defined" );

        if ( servers == null )
            servers = new CaseInsensitiveMap<>( newServers );
        else {
            for ( ServerInfo oldServer : servers.values() )
            {
                // Don't allow servers to be removed
                Preconditions.checkArgument( newServers.containsKey( oldServer.getName() ), "Server %s removed on reload!", oldServer.getName() );
            }

            // Add new servers
            for ( Map.Entry<String, ServerInfo> newServer : newServers.entrySet() ) {
                if ( !servers.containsValue( newServer.getValue() ) )
                    servers.put( newServer.getKey(), newServer.getValue() );
            }
        }

        for ( ListenerInfo listener : listeners )
        {
            for ( int i = 0; i < listener.getServerPriority().size(); i++ )
            {
                String server = listener.getServerPriority().get( i );
                Preconditions.checkArgument( servers.containsKey( server ), "Server %s (priority %s) is not defined", server, i );
            }
            for ( String server : listener.getForcedHosts().values() )
            {
                if ( !servers.containsKey( server ) )
                {
                    ProxyServer.getInstance().getLogger().log( Level.WARNING, "Forced host server {0} is not defined", server );
                }
            }
        }
    }

    @Override
    @Deprecated
    public String getFavicon()
    {
        return getFaviconObject().getEncoded();
    }

    @Override
    public Favicon getFaviconObject()
    {
        return favicon;
    }

    @Override
    public String getCustomServerName()
    {
        return customServerName;
    }

	@Override
	public boolean getAlwaysHandlePackets() {
        return alwaysHandlePackets;
	}
}
