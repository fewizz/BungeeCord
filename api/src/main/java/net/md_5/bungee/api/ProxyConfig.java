package net.md_5.bungee.api;

import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Collection;
import java.util.Map;

/**
 * Core configuration adaptor for the proxy api.
 *
 * This class is subject to rapid change between releases
 */
public interface ProxyConfig
{

    /**
     * Time before users are disconnected due to no network activity.
     */
    int getTimeout();

    /**
     * UUID used for metrics.
     */
    String getUuid();

    /**
     * Set of all listeners.
     */
    Collection<ListenerInfo> getListeners();

    /**
     * Set of all servers.
     */
    Map<String, ServerInfo> getServers();

    /**
     * Does the server authenticate with mojang
     */
    boolean isOnlineMode();

    /**
     * Whether proxy commands are logged to the proxy log
     */
    boolean isLogCommands();

    /**
     * Returns the player max.
     */
    int getPlayerLimit();

    /**
     * A collection of disabled commands.
     */
    Collection<String> getDisabledCommands();

    /**
     * The connection throttle delay.
     */
    @Deprecated
    int getThrottle();

    /**
     * Whether the proxy will parse IPs with spigot or not
     */
    @Deprecated
    boolean isIpForward();

    /**
     * The encoded favicon.
     *
     * @deprecated Use #getFaviconObject instead.
     */
    @Deprecated
    String getFavicon();

    /**
     * The favicon used for the server ping list.
     */
    Favicon getFaviconObject();
    
    /**
     * The server name for the server ping list.
     */
    String getCustomServerName();
    
    /**
     * Whether the server should handle packets before the player has connected to a server
     */
    boolean getAlwaysHandlePackets();
    
    boolean isLogQueryErrors();
}
