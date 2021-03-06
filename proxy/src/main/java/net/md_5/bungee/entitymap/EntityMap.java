package net.md_5.bungee.entitymap;

import static net.md_5.bungee.protocol.Protocol.MC_1_13_0;
import static net.md_5.bungee.protocol.Protocol.MC_1_13_2;

import java.io.IOException;

import com.flowpowered.nbt.stream.NBTInputStream;
import com.google.common.base.Throwables;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import lombok.NonNull;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.Protocol;

/**
 * Class to rewrite integers within packets.
 */
public abstract class EntityMap
{

    private final boolean[] clientboundInts = new boolean[ 256 ];
    private final boolean[] clientboundVarInts = new boolean[ 256 ];

    private final boolean[] serverboundInts = new boolean[ 256 ];
    private final boolean[] serverboundVarInts = new boolean[ 256 ];

    // Returns the correct entity map for the protocol version
    public static EntityMap getEntityMap(Protocol version)
    {
        switch ( version )
        {
        	case MC_1_5_2:
        	case MC_1_6_4:
        		return EntityMap_1_6_4.INSTANCE;
            case MC_1_7_2:
                return EntityMap_1_7_2.INSTANCE;
            case MC_1_7_6:
                return EntityMap_1_7_6.INSTANCE;
            case MC_1_8_0:
                return EntityMap_1_8.INSTANCE;
            case MC_1_9_0:
            case MC_1_9_1:
            case MC_1_9_2:
                return EntityMap_1_9.INSTANCE;
            case MC_1_9_3:
                return EntityMap_1_9_4.INSTANCE;
            case MC_1_10_0:
                return EntityMap_1_10.INSTANCE;
            case MC_1_11_0:
            case MC_1_11_1:
                return EntityMap_1_11.INSTANCE;
            case MC_1_12_0:
                return EntityMap_1_12.INSTANCE;
            case MC_1_12_1:
            case MC_1_12_2:
                return EntityMap_1_12_1.INSTANCE;
            case MC_1_13_0:
            case MC_1_13_1:
            case MC_1_13_2:
                return EntityMap_1_13.INSTANCE;
            case MC_1_14_0:
            case MC_1_14_1:
            	return EntityMap_1_14.INSTANCE;
        }
        throw new RuntimeException( "Version " + version + " has no entity map" );
    }
    
    protected void addRewrite(int id, Direction direction) {
    	addRewrite(id, direction, false);
    }
    
    protected void addRewrite(int id, Direction direction, boolean varint)
    {
        if ( direction == Direction.TO_CLIENT )
        {
            if ( varint )
            {
                clientboundVarInts[id] = true;
            } else
            {
                clientboundInts[id] = true;
            }
        } else if ( varint )
        {
            serverboundVarInts[id] = true;
        } else
        {
            serverboundInts[id] = true;
        }
    }

    public void rewriteServerbound(ByteBuf packet, int oldId, int newId, Protocol pv)
    {
        rewrite( packet, oldId, newId, serverboundInts, serverboundVarInts, pv);
    }

    public void rewriteClientbound(ByteBuf packet, int oldId, int newId, Protocol pv)
    {
        rewrite( packet, oldId, newId, clientboundInts, clientboundVarInts, pv );
    }

    protected static void rewriteInt(ByteBuf packet, int oldId, int newId, int offset)
    {
        int readId = packet.getInt( offset );
        if ( readId == oldId )
        {
            packet.setInt( offset, newId );
        } else if ( readId == newId )
        {
            packet.setInt( offset, oldId );
        }
    }

    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    protected static void rewriteVarInt(ByteBuf packet, int oldId, int newId, int offset)
    {
        // Need to rewrite the packet because VarInts are variable length
        int readId = DefinedPacket.readVarInt( packet );
        int readIdLength = packet.readerIndex() - offset;
        if ( readId == oldId || readId == newId )
        {
            ByteBuf data = packet.copy();
            packet.readerIndex( offset );
            packet.writerIndex( offset );
            DefinedPacket.writeVarInt( readId == oldId ? newId : oldId, packet );
            packet.writeBytes( data );
            data.release();
        }
    }

    protected static void rewriteMetaVarInt(ByteBuf packet, int oldId, int newId, int metaIndex, @NonNull Protocol protocolVersion)
    {
        int readerIndex = packet.readerIndex();

        short index;
        while ( ( index = packet.readUnsignedByte() ) != 0xFF )
        {
            int type = DefinedPacket.readVarInt( packet );
            if ( protocolVersion.newerOrEqual(MC_1_13_0) )
            {
                switch ( type )
                {
                    case 5: // optional chat
                        if ( packet.readBoolean() )
                        {
                            DefinedPacket.readString( packet );
                        }
                        continue;
                    case 15: // particle
                        int particleId = DefinedPacket.readVarInt( packet );
                        if(protocolVersion.newerOrEqual(Protocol.MC_1_14_0)) {
                        	switch ( particleId )
                            {
                                case 3: // minecraft:block
                                case 23: // minecraft:falling_dust
                                    DefinedPacket.readVarInt( packet ); // block state
                                    break;
                                case 14: // minecraft:dust
                                    packet.skipBytes( 16 ); // float, float, float, flat
                                    break;
                                case 32: // minecraft:item
                                    readSkipSlot( packet, protocolVersion );
                                    break;
                            }
                        }
                        else {
                        	switch ( particleId )
                        	{
                            	case 3:
                            	case 20:
                                	DefinedPacket.readVarInt( packet ); // block state
                                	break;
                            	case 11: // dust
                                	packet.skipBytes( 16 ); // float, float, float, flat
                                	break;
                            	case 27: // item
                                	readSkipSlot( packet, protocolVersion );
                                	break;
                        	}
                        }
                        continue;
                    default:
                        if ( type >= 6 )
                        {
                            type--;
                        }
                        break;
                }
            }

            switch ( type )
            {
                case 0:
                    packet.skipBytes( 1 ); // byte
                    break;
                case 1:
                    if ( index == metaIndex )
                    {
                        int position = packet.readerIndex();
                        rewriteVarInt( packet, oldId, newId, position );
                        packet.readerIndex( position );
                    }
                    DefinedPacket.readVarInt( packet );
                    break;
                case 2:
                    packet.skipBytes( 4 ); // float
                    break;
                case 3:
                case 4:
                    DefinedPacket.readString( packet );
                    break;
                case 5:
                    readSkipSlot( packet, protocolVersion );
                    break;
                case 6:
                    packet.skipBytes( 1 ); // boolean
                    break;
                case 7:
                    packet.skipBytes( 12 ); // float, float, float
                    break;
                case 8:
                    packet.readLong();
                    break;
                case 9:
                    if ( packet.readBoolean() )
                    {
                        packet.skipBytes( 8 ); // long
                    }
                    break;
                case 10:
                    DefinedPacket.readVarInt( packet );
                    break;
                case 11:
                    if ( packet.readBoolean() )
                    {
                        packet.skipBytes( 16 ); // long, long
                    }
                    break;
                case 12:
                    DefinedPacket.readVarInt( packet );
                    break;
                case 13:
                    try
                    {
                        new NBTInputStream( new ByteBufInputStream( packet ), false ).readTag();
                    } catch ( IOException ex )
                    {
                        throw Throwables.propagate( ex );
                    }
                    break;
                case 15:
                    DefinedPacket.readVarInt( packet );
                    DefinedPacket.readVarInt( packet );
                    DefinedPacket.readVarInt( packet );
                    break;
                case 16:
                    if ( index == metaIndex )
                    {
                        int position = packet.readerIndex();
                        rewriteVarInt( packet, oldId + 1, newId + 1, position );
                        packet.readerIndex( position );
                    }
                    DefinedPacket.readVarInt( packet );
                    break;
                case 17:
                    DefinedPacket.readVarInt( packet );
                    break;
                default:
                    throw new IllegalArgumentException( "Unknown meta type " + type );
            }
        }

        packet.readerIndex( readerIndex );
    }

    private static void readSkipSlot(ByteBuf packet, @NonNull Protocol protocolVersion)
    {
        if ( protocolVersion.newerOrEqual(MC_1_13_2) ? packet.readBoolean() : packet.readShort() != -1 )
        {
            if ( protocolVersion.newerOrEqual(MC_1_13_2) )
            {
                DefinedPacket.readVarInt( packet );
            }
            packet.skipBytes( ( protocolVersion.newerOrEqual(MC_1_13_0) ) ? 1 : 3 ); // byte vs byte, short

            int position = packet.readerIndex();
            if ( packet.readByte() != 0 )
            {
                packet.readerIndex( position );

                try
                {
                    new NBTInputStream( new ByteBufInputStream( packet ), false ).readTag();
                } catch ( IOException ex )
                {
                    throw Throwables.propagate( ex );
                }
            }
        }
    }

    // Handles simple packets
    private static void rewrite(ByteBuf packet, int oldId, int newId, boolean[] ints, boolean[] varints, Protocol pv)
    {
        int readerIndex = packet.readerIndex();
        int packetId = pv.isModern() ? DefinedPacket.readVarInt( packet ) : packet.readUnsignedByte();
        int packetIdLength = packet.readerIndex() - readerIndex;

        if(packetId>=0) {
            if ( ints[ packetId ] )
            {
                rewriteInt( packet, oldId, newId, readerIndex + packetIdLength );
            } else if ( varints[ packetId ] )
            {
                rewriteVarInt( packet, oldId, newId, readerIndex + packetIdLength );
            }
        }
        packet.readerIndex( readerIndex );
    }
}
