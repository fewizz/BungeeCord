package net.md_5.bungee.protocol;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.protocol.packet.BossBar;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.Commands;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.EntityStatus;
import net.md_5.bungee.protocol.packet.Handshake;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.LegacyPing;
import net.md_5.bungee.protocol.packet.Login;
import net.md_5.bungee.protocol.packet.LoginPayloadRequest;
import net.md_5.bungee.protocol.packet.LoginPayloadResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.protocol.packet.LoginSuccess;
import net.md_5.bungee.protocol.packet.PingPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.Respawn;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.SetCompression;
import net.md_5.bungee.protocol.packet.StatusRequest;
import net.md_5.bungee.protocol.packet.StatusResponse;
import net.md_5.bungee.protocol.packet.TabCompleteRequest;
import net.md_5.bungee.protocol.packet.TabCompleteResponse;
import net.md_5.bungee.protocol.packet.Team;
import net.md_5.bungee.protocol.packet.Title;

public enum Protocol
{

    // Undef
    HANDSHAKE
    {

        {
            TO_SERVER.registerPacket(
                    Handshake.class,
                    map( ProtocolVersion.MC_1_8, 0x00 )
            );
            TO_SERVER.registerPacket(
            		LegacyPing.class,
            		map( ProtocolVersion.MC_1_6_4, 0xFE)
            );
        }
    },
    // 0
    GAME
    {

        {
            TO_CLIENT.registerPacket(
                    KeepAlive.class,
                    map( ProtocolVersion.MC_1_8, 0x00 ),
                    map( ProtocolVersion.MC_1_9, 0x1F ),
                    map( ProtocolVersion.MC_1_12, 0x1F ),
                    map( ProtocolVersion.MC_1_13, 0x21 )
            );
            TO_CLIENT.registerPacket(
                    Login.class,
                    map( ProtocolVersion.MC_1_8, 0x01 ),
                    map( ProtocolVersion.MC_1_9, 0x23 ),
                    map( ProtocolVersion.MC_1_12, 0x23 ),
                    map( ProtocolVersion.MC_1_13, 0x25 )
            );
            TO_CLIENT.registerPacket(
                    Chat.class,
                    map( ProtocolVersion.MC_1_8, 0x02 ),
                    map( ProtocolVersion.MC_1_9, 0x0F ),
                    map( ProtocolVersion.MC_1_12, 0x0F ),
                    map( ProtocolVersion.MC_1_13, 0x0E )
            );
            TO_CLIENT.registerPacket(
                    Respawn.class,
                    map( ProtocolVersion.MC_1_8, 0x07 ),
                    map( ProtocolVersion.MC_1_9, 0x33 ),
                    map( ProtocolVersion.MC_1_12, 0x34 ),
                    map( ProtocolVersion.MC_1_12_1, 0x35 ),
                    map( ProtocolVersion.MC_1_13, 0x38 )
            );
            TO_CLIENT.registerPacket(
                    BossBar.class,
                    map( ProtocolVersion.MC_1_9, 0x0C ),
                    map( ProtocolVersion.MC_1_12, 0x0C ),
                    map( ProtocolVersion.MC_1_13, 0x0C )
            );
            TO_CLIENT.registerPacket(
                    PlayerListItem.class, // PlayerInfo
                    map( ProtocolVersion.MC_1_8, 0x38 ),
                    map( ProtocolVersion.MC_1_9, 0x2D ),
                    map( ProtocolVersion.MC_1_12, 0x2D ),
                    map( ProtocolVersion.MC_1_12_1, 0x2E ),
                    map( ProtocolVersion.MC_1_13, 0x30 )
            );
            TO_CLIENT.registerPacket(
                    TabCompleteResponse.class,
                    map( ProtocolVersion.MC_1_8, 0x3A ),
                    map( ProtocolVersion.MC_1_9, 0x0E ),
                    map( ProtocolVersion.MC_1_12, 0x0E ),
                    map( ProtocolVersion.MC_1_13, 0x10 )
            );
            TO_CLIENT.registerPacket(
                    ScoreboardObjective.class,
                    map( ProtocolVersion.MC_1_8, 0x3B ),
                    map( ProtocolVersion.MC_1_9, 0x3F ),
                    map( ProtocolVersion.MC_1_12, 0x41 ),
                    map( ProtocolVersion.MC_1_12_1, 0x42 ),
                    map( ProtocolVersion.MC_1_13, 0x45 )
            );
            TO_CLIENT.registerPacket(
                    ScoreboardScore.class,
                    map( ProtocolVersion.MC_1_8, 0x3C ),
                    map( ProtocolVersion.MC_1_9, 0x42 ),
                    map( ProtocolVersion.MC_1_12, 0x44 ),
                    map( ProtocolVersion.MC_1_12_1, 0x45 ),
                    map( ProtocolVersion.MC_1_13, 0x48 )
            );
            TO_CLIENT.registerPacket(
                    ScoreboardDisplay.class,
                    map( ProtocolVersion.MC_1_8, 0x3D ),
                    map( ProtocolVersion.MC_1_9, 0x38 ),
                    map( ProtocolVersion.MC_1_12, 0x3A ),
                    map( ProtocolVersion.MC_1_12_1, 0x3B ),
                    map( ProtocolVersion.MC_1_13, 0x3E )
            );
            TO_CLIENT.registerPacket(
                    Team.class,
                    map( ProtocolVersion.MC_1_8, 0x3E ),
                    map( ProtocolVersion.MC_1_9, 0x41 ),
                    map( ProtocolVersion.MC_1_12, 0x43 ),
                    map( ProtocolVersion.MC_1_12_1, 0x44 ),
                    map( ProtocolVersion.MC_1_13, 0x47 )
            );
            TO_CLIENT.registerPacket(
                    PluginMessage.class,
                    map( ProtocolVersion.MC_1_8, 0x3F ),
                    map( ProtocolVersion.MC_1_9, 0x18 ),
                    map( ProtocolVersion.MC_1_12, 0x18 ),
                    map( ProtocolVersion.MC_1_13, 0x19 )
            );
            TO_CLIENT.registerPacket(
                    Kick.class,
                    map( ProtocolVersion.MC_1_8, 0x40 ),
                    map( ProtocolVersion.MC_1_9, 0x1A ),
                    map( ProtocolVersion.MC_1_12, 0x1A ),
                    map( ProtocolVersion.MC_1_13, 0x1B )
            );
            TO_CLIENT.registerPacket(
                    Title.class,
                    map( ProtocolVersion.MC_1_8, 0x45 ),
                    map( ProtocolVersion.MC_1_12, 0x47 ),
                    map( ProtocolVersion.MC_1_12_1, 0x48 ),
                    map( ProtocolVersion.MC_1_13, 0x4B )
            );
            TO_CLIENT.registerPacket(
                    PlayerListHeaderFooter.class,
                    map( ProtocolVersion.MC_1_8, 0x47 ),
                    map( ProtocolVersion.MC_1_9, 0x48 ),
                    map( ProtocolVersion.MC_1_9_4, 0x47 ),
                    map( ProtocolVersion.MC_1_12, 0x49 ),
                    map( ProtocolVersion.MC_1_12_1, 0x4A ),
                    map( ProtocolVersion.MC_1_13, 0x4E )
            );
            TO_CLIENT.registerPacket(
                    SetCompression.class,
                    map( ProtocolVersion.MC_1_7_2, 0x46 , false ),
                    map( ProtocolVersion.MC_1_7_6, 0x46 , false ),
                    map( ProtocolVersion.MC_1_8, 0x46 , false )
            );
            TO_CLIENT.registerPacket(
                    EntityStatus.class,
                    map( ProtocolVersion.MC_1_8, 0x1A ),
                    map( ProtocolVersion.MC_1_9, 0x1B ),
                    map( ProtocolVersion.MC_1_12, 0x1B ),
                    map( ProtocolVersion.MC_1_13, 0x1C )
            );
            if ( Boolean.getBoolean( "net.md-5.bungee.protocol.register_commands" ) )
            {
                TO_CLIENT.registerPacket(
                        Commands.class,
                        map( ProtocolVersion.MC_1_13, 0x11 )
                );
            }

            TO_SERVER.registerPacket(
                    KeepAlive.class,
                    map( ProtocolVersion.MC_1_8, 0x00 ),
                    map( ProtocolVersion.MC_1_9, 0x0B ),
                    map( ProtocolVersion.MC_1_12, 0x0C ),
                    map( ProtocolVersion.MC_1_12_1, 0x0B ),
                    map( ProtocolVersion.MC_1_13, 0x0E )
            );
            TO_SERVER.registerPacket(
                    Chat.class,
                    map( ProtocolVersion.MC_1_8, 0x01 ),
                    map( ProtocolVersion.MC_1_9, 0x02 ),
                    map( ProtocolVersion.MC_1_12, 0x03 ),
                    map( ProtocolVersion.MC_1_12_1, 0x02 ),
                    map( ProtocolVersion.MC_1_13, 0x02 )
            );
            TO_SERVER.registerPacket(
                    TabCompleteRequest.class,
                    map( ProtocolVersion.MC_1_8, 0x14 ),
                    map( ProtocolVersion.MC_1_9, 0x01 ),
                    map( ProtocolVersion.MC_1_12, 0x02 ),
                    map( ProtocolVersion.MC_1_12_1, 0x01 ),
                    map( ProtocolVersion.MC_1_13, 0x05 )
            );
            TO_SERVER.registerPacket(
                    ClientSettings.class,
                    map( ProtocolVersion.MC_1_8, 0x15 ),
                    map( ProtocolVersion.MC_1_9, 0x04 ),
                    map( ProtocolVersion.MC_1_12, 0x05 ),
                    map( ProtocolVersion.MC_1_12_1, 0x04 ),
                    map( ProtocolVersion.MC_1_13, 0x04 )
            );
            TO_SERVER.registerPacket(
                    PluginMessage.class,
                    map( ProtocolVersion.MC_1_8, 0x17 ),
                    map( ProtocolVersion.MC_1_9, 0x09 ),
                    map( ProtocolVersion.MC_1_12, 0x0A ),
                    map( ProtocolVersion.MC_1_12_1, 0x09 ),
                    map( ProtocolVersion.MC_1_13, 0x0A )
            );
        }
    },
    // 1
    STATUS
    {

        {
            TO_CLIENT.registerPacket(
                    StatusResponse.class,
                    map( ProtocolVersion.MC_1_8, 0x00 )
            );
            TO_CLIENT.registerPacket(
                    PingPacket.class,
                    map( ProtocolVersion.MC_1_8, 0x01 )
            );

            TO_SERVER.registerPacket(
                    StatusRequest.class,
                    map( ProtocolVersion.MC_1_8, 0x00 )
            );
            TO_SERVER.registerPacket(
                    PingPacket.class,
                    map( ProtocolVersion.MC_1_8, 0x01 )
            );
        }
    },
    //2
    LOGIN
    {

        {
            TO_CLIENT.registerPacket(
                    Kick.class,
                    map( ProtocolVersion.MC_1_8, 0x00 )
            );
            TO_CLIENT.registerPacket(
                    EncryptionRequest.class,
                    map( ProtocolVersion.MC_1_8, 0x01 )
            );
            TO_CLIENT.registerPacket(
                    LoginSuccess.class,
                    map( ProtocolVersion.MC_1_8, 0x02 )
            );
            TO_CLIENT.registerPacket(
                    SetCompression.class,
                    map( ProtocolVersion.MC_1_8, 0x03 )
            );
            TO_CLIENT.registerPacket(
                    LoginPayloadRequest.class,
                    map( ProtocolVersion.MC_1_13, 0x04 )
            );

            TO_SERVER.registerPacket(
                    LoginRequest.class,
                    map( ProtocolVersion.MC_1_8, 0x00 )
            );
            TO_SERVER.registerPacket(
                    EncryptionResponse.class,
                    map( ProtocolVersion.MC_1_8, 0x01 )
            );
            TO_SERVER.registerPacket(
                    LoginPayloadResponse.class,
                    map( ProtocolVersion.MC_1_13, 0x02 )
            );
        }
    };
    /*========================================================================*/
    public static final int MAX_PACKET_ID = 0xFF;
    /*========================================================================*/
    public final DirectionData TO_SERVER = new DirectionData( this, Direction.TO_SERVER );
    public final DirectionData TO_CLIENT = new DirectionData( this, Direction.TO_CLIENT );

    /*public static void main(String[] args)
    {
        for ( int version : ProtocolConstants.SUPPORTED_VERSION_IDS )
        {
            dump( version );
        }
    }

    private static void dump(int version)
    {
        for ( Protocol protocol : Protocol.values() )
        {
            dump( version, protocol );
        }
    }

    private static void dump(int version, Protocol protocol)
    {
        dump( version, protocol.TO_CLIENT );
        dump( version, protocol.TO_SERVER );
    }

    private static void dump(int version, DirectionData data)
    {
        for ( int id = 0; id < MAX_PACKET_ID; id++ )
        {
            DefinedPacket packet = data.createPacket( id, version );
            if ( packet != null )
            {
                System.out.println( version + " " + data.protocolPhase + " " + data.direction + " " + id + " " + packet.getClass().getSimpleName() );
            }
        }
    }*/

    @RequiredArgsConstructor
    private static class ProtocolData
    {
        private final ProtocolVersion version;
        private final TObjectIntMap<Class<? extends DefinedPacket>> packetMap = new TObjectIntHashMap<>( MAX_PACKET_ID );
        private final TIntObjectMap<Constructor<? extends DefinedPacket>> packetConstructors = new TIntObjectHashMap<>( MAX_PACKET_ID );
    }

    @RequiredArgsConstructor
    private static class ProtocolMapping
    {
        private final ProtocolVersion protocolVersion;
        private final int packetID;
        private final boolean inherit;
    }

    // Helper method
    private static ProtocolMapping map(ProtocolVersion version, int id) {
        return map(version, id, true);
    }

    private static ProtocolMapping map(ProtocolVersion version, int id, boolean inherit) {
        return new ProtocolMapping(version, id, inherit);
    }

    @RequiredArgsConstructor
    public static class DirectionData
    {

        private final Protocol protocolPhase;
        //private final TIntObjectMap<ProtocolData> protocols = new TIntObjectHashMap<>();
        //Map<ProtocolVersion, ProtocolData> protocols = new HashMap<>();
        private final Map<ProtocolVersion, ProtocolData> protocols = new EnumMap<>(ProtocolVersion.class);
        {
            for ( ProtocolVersion protocol : ProtocolVersion.values() )
            {
            	protocols.put(protocol, new ProtocolData(protocol));
            }
        }
        //private final TIntObjectMap<List<Integer>> linkedProtocols = new TIntObjectHashMap<>();
        private final Map<ProtocolVersion, List<ProtocolVersion>> linkedProtocols = new EnumMap<>(ProtocolVersion.class);
        {
            linkedProtocols.put( ProtocolVersion.MC_1_7_2, Arrays.asList(
            		ProtocolVersion.MC_1_7_6
            ));
            linkedProtocols.put( ProtocolVersion.MC_1_8, Arrays.asList(
            		ProtocolVersion.MC_1_7_2,
            		ProtocolVersion.MC_1_9,
            		ProtocolVersion.MC_1_12,
            		ProtocolVersion.MC_1_13
            ) );
            linkedProtocols.put( ProtocolVersion.MC_1_9, Arrays.asList(
            		ProtocolVersion.MC_1_9_1,
            		ProtocolVersion.MC_1_9_2,
            		ProtocolVersion.MC_1_9_4
            ) );
            linkedProtocols.put( ProtocolVersion.MC_1_9_4, Arrays.asList(
            		ProtocolVersion.MC_1_10,
            		ProtocolVersion.MC_1_11,
            		ProtocolVersion.MC_1_11_1
            ) );
            linkedProtocols.put( ProtocolVersion.MC_1_12, Arrays.asList(
            		ProtocolVersion.MC_1_12_1
            ) );
            linkedProtocols.put( ProtocolVersion.MC_1_12_1, Arrays.asList(
            		ProtocolVersion.MC_1_12_2
            ) );
            linkedProtocols.put( ProtocolVersion.MC_1_13, Arrays.asList(
            		ProtocolVersion.MC_1_13_1,
            		ProtocolVersion.MC_1_13_2
            ) );
        }

        @Getter
        private final Direction direction;

        private ProtocolData getProtocolData(ProtocolVersion version )
        {
            ProtocolData protocol = protocols.get( version );
            if ( protocol == null && ( protocolPhase != Protocol.GAME ) )
            {
                protocol = Iterables.getFirst( protocols.values(), null );
            }
            return protocol;
        }

        public final DefinedPacket createPacket(int id, ProtocolVersion version)
        {
            ProtocolData protocolData = getProtocolData( version );
            if ( protocolData == null )
            {
                throw new BadPacketException( "Unsupported protocol version" );
            }
            if ( id > MAX_PACKET_ID )
            {
                throw new BadPacketException( "Packet with id " + id + " outside of range " );
            }

            Constructor<? extends DefinedPacket> constructor = protocolData.packetConstructors.get( id );
            try
            {
                return ( constructor == null ) ? null : constructor.newInstance();
            } catch ( ReflectiveOperationException ex )
            {
                throw new BadPacketException( "Could not construct packet with id " + id, ex );
            }
        }

        protected final void registerPacket(Class<? extends DefinedPacket> packetClass, ProtocolMapping... mappings)
        {
            try
            {
                Constructor<? extends DefinedPacket> constructor = packetClass.getDeclaredConstructor();
                for ( ProtocolMapping mapping : mappings )
                {
                    ProtocolData data = protocols.get( mapping.protocolVersion );
                    data.packetMap.put( packetClass, mapping.packetID );
                    data.packetConstructors.put( mapping.packetID, constructor );

                    if (mapping.inherit)
                    {
                        List<ProtocolVersion> links = linkedProtocols.get( mapping.protocolVersion );
                        if ( links != null )
                        {
                            links: for ( ProtocolVersion link : links )
                            {
                                // Check for manual mappings
                                for ( ProtocolMapping m : mappings )
                                {
                                    if ( m == mapping ) continue;
                                    if ( m.protocolVersion == link ) continue links;
                                    List<ProtocolVersion> innerLinks = linkedProtocols.get( m.protocolVersion );
                                    if ( innerLinks != null && innerLinks.contains( link ) ) continue links;
                                }
                                registerPacket( packetClass, map( link, mapping.packetID ) );
                            }
                        }
                    }
                }
            } catch ( NoSuchMethodException ex )
            {
                throw new BadPacketException( "No NoArgsConstructor for packet class " + packetClass );
            }
        }

        final int getId(Class<? extends DefinedPacket> packet, ProtocolVersion version)
        {

            ProtocolData protocolData = getProtocolData( version );
            if ( protocolData == null )
            {
                throw new BadPacketException( "Unsupported protocol version" );
            }
            Preconditions.checkArgument( protocolData.packetMap.containsKey( packet ), "Cannot get ID for packet %s in phase %s with direction %s", packet, protocolPhase, direction );

            return protocolData.packetMap.get( packet );
        }
    }
}
