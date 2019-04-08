package net.md_5.bungee.entitymap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.Protocol;

import java.util.UUID;

class EntityMap_1_9_4 extends EntityMap
{

    static final EntityMap_1_9_4 INSTANCE = new EntityMap_1_9_4();

    EntityMap_1_9_4()
    {
        addRewrite( 0x00, Direction.TO_CLIENT, true ); // Spawn Object : PacketPlayOutSpawnEntity
        addRewrite( 0x01, Direction.TO_CLIENT, true ); // Spawn Experience Orb : PacketPlayOutSpawnEntityExperienceOrb
        addRewrite( 0x03, Direction.TO_CLIENT, true ); // Spawn Mob : PacketPlayOutSpawnEntityLiving
        addRewrite( 0x04, Direction.TO_CLIENT, true ); // Spawn Painting : PacketPlayOutSpawnEntityPainting
        addRewrite( 0x05, Direction.TO_CLIENT, true ); // Spawn Player : PacketPlayOutNamedEntitySpawn
        addRewrite( 0x06, Direction.TO_CLIENT, true ); // Animation : PacketPlayOutAnimation
        addRewrite( 0x08, Direction.TO_CLIENT, true ); // Block Break Animation : PacketPlayOutBlockBreakAnimation
        addRewrite( 0x1B, Direction.TO_CLIENT, false ); // Entity Status : PacketPlayOutEntityStatus
        addRewrite( 0x25, Direction.TO_CLIENT, true ); // Entity Relative Move : PacketPlayOutRelEntityMove
        addRewrite( 0x26, Direction.TO_CLIENT, true ); // Entity Look and Relative Move : PacketPlayOutRelEntityMoveLook
        addRewrite( 0x27, Direction.TO_CLIENT, true ); // Entity Look : PacketPlayOutEntityLook
        addRewrite( 0x28, Direction.TO_CLIENT, true ); // Entity : PacketPlayOutEntity
        addRewrite( 0x2F, Direction.TO_CLIENT, true ); // Use bed : PacketPlayOutBed
        addRewrite( 0x31, Direction.TO_CLIENT, true ); // Remove Entity Effect : PacketPlayOutRemoveEntityEffect
        addRewrite( 0x34, Direction.TO_CLIENT, true ); // Entity Head Look : PacketPlayOutEntityHeadRotation
        addRewrite( 0x36, Direction.TO_CLIENT, true ); // Camera : PacketPlayOutCamera
        addRewrite( 0x39, Direction.TO_CLIENT, true ); // Entity Metadata : PacketPlayOutEntityMetadata
        addRewrite( 0x3A, Direction.TO_CLIENT, false ); // Attach Entity : PacketPlayOutAttachEntity
        addRewrite( 0x3B, Direction.TO_CLIENT, true ); // Entity Velocity : PacketPlayOutEntityVelocity
        addRewrite( 0x3C, Direction.TO_CLIENT, true ); // Entity Equipment : PacketPlayOutEntityEquipment
        addRewrite( 0x40, Direction.TO_CLIENT, true ); // Attach Entity : PacketPlayOutMount
        addRewrite( 0x48, Direction.TO_CLIENT, true ); // Collect Item : PacketPlayOutCollect
        addRewrite( 0x49, Direction.TO_CLIENT, true ); // Entity Teleport : PacketPlayOutEntityTeleport
        addRewrite( 0x4A, Direction.TO_CLIENT, true ); // Entity Properties : PacketPlayOutUpdateAttributes
        addRewrite( 0x4B, Direction.TO_CLIENT, true ); // Entity Effect : PacketPlayOutEntityEffect

        addRewrite( 0x0A, Direction.TO_SERVER, true ); // Use Entity : PacketPlayInUseEntity
        addRewrite( 0x14, Direction.TO_SERVER, true ); // Entity Action : PacketPlayInEntityAction
    }

    @Override
    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    public void rewriteClientbound(ByteBuf packet, int oldId, int newId, Protocol pv)
    {
        super.rewriteClientbound( packet, oldId, newId, pv );

        // Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;
        int jumpIndex = packet.readerIndex();
        switch ( packetId )
        {
            case 0x3A /* Attach Entity : PacketPlayOutAttachEntity */:
                rewriteInt( packet, oldId, newId, readerIndex + packetIdLength + 4 );
                break;
            case 0x48 /* Collect Item : PacketPlayOutCollect */:
                DefinedPacket.readVarInt( packet );
                rewriteVarInt( packet, oldId, newId, packet.readerIndex() );
                break;
            case 0x40 /* Attach Entity : PacketPlayOutMount */:
                DefinedPacket.readVarInt( packet );
                jumpIndex = packet.readerIndex();
            // Fall through on purpose to int array of IDs
            case 0x30 /* Destroy Entities : PacketPlayOutEntityDestroy */:
                int count = DefinedPacket.readVarInt( packet );
                int[] ids = new int[ count ];
                for ( int i = 0; i < count; i++ )
                {
                    ids[i] = DefinedPacket.readVarInt( packet );
                }
                packet.readerIndex( jumpIndex );
                packet.writerIndex( jumpIndex );
                DefinedPacket.writeVarInt( count, packet );
                for ( int id : ids )
                {
                    if ( id == oldId )
                    {
                        id = newId;
                    } else if ( id == newId )
                    {
                        id = oldId;
                    }
                    DefinedPacket.writeVarInt( id, packet );
                }
                break;
            case 0x00 /* Spawn Object : PacketPlayOutSpawnEntity */:
                DefinedPacket.readVarInt( packet );
                DefinedPacket.readUUID( packet );
                int type = packet.readUnsignedByte();

                if ( type == 60 || type == 90 || type == 91 )
                {
                    if ( type == 60 || type == 91 )
                    {
                        oldId = oldId + 1;
                        newId = newId + 1;
                    }

                    packet.skipBytes( 26 ); // double, double, double, byte, byte
                    int position = packet.readerIndex();
                    int readId = packet.readInt();
                    if ( readId == oldId )
                    {
                        packet.setInt( position, newId );
                    } else if ( readId == newId )
                    {
                        packet.setInt( position, oldId );
                    }
                }
                break;
            case 0x05 /* Spawn Player : PacketPlayOutNamedEntitySpawn */:
                DefinedPacket.readVarInt( packet ); // Entity ID
                int idLength = packet.readerIndex() - readerIndex - packetIdLength;
                UUID uuid = DefinedPacket.readUUID( packet );
                ProxiedPlayer player;
                if ( ( player = BungeeCord.getInstance().getPlayerByOfflineUUID( uuid ) ) != null )
                {
                    int previous = packet.writerIndex();
                    packet.readerIndex( readerIndex );
                    packet.writerIndex( readerIndex + packetIdLength + idLength );
                    DefinedPacket.writeUUID( player.getUniqueId(), packet );
                    packet.writerIndex( previous );
                }
                break;
            case 0x2C /* Combat Event : PacketPlayOutCombatEvent */:
                int event = packet.readUnsignedByte();
                if ( event == 1 /* End Combat*/ )
                {
                    DefinedPacket.readVarInt( packet );
                    rewriteInt( packet, oldId, newId, packet.readerIndex() );
                } else if ( event == 2 /* Entity Dead */ )
                {
                    int position = packet.readerIndex();
                    rewriteVarInt( packet, oldId, newId, packet.readerIndex() );
                    packet.readerIndex( position );
                    DefinedPacket.readVarInt( packet );
                    rewriteInt( packet, oldId, newId, packet.readerIndex() );
                }
                break;
            case 0x39 /* EntityMetadata : PacketPlayOutEntityMetadata */:
                DefinedPacket.readVarInt( packet ); // Entity ID
                rewriteMetaVarInt( packet, oldId + 1, newId + 1, 5, pv ); // fishing hook
                break;
        }
        packet.readerIndex( readerIndex );
    }

    @Override
    public void rewriteServerbound(ByteBuf packet, int oldId, int newId, Protocol pv)
    {
        super.rewriteServerbound( packet, oldId, newId, pv );
        // Special cases
        int readerIndex = packet.readerIndex();
        int packetId = DefinedPacket.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;

        if ( packetId == 0x1B /* Spectate : PacketPlayInSpectate */ && !BungeeCord.getInstance().getConfig().isIpForward() )
        {
            UUID uuid = DefinedPacket.readUUID( packet );
            ProxiedPlayer player;
            if ( ( player = BungeeCord.getInstance().getPlayer( uuid ) ) != null )
            {
                int previous = packet.writerIndex();
                packet.readerIndex( readerIndex );
                packet.writerIndex( readerIndex + packetIdLength );
                DefinedPacket.writeUUID( ( (UserConnection) player ).getPendingConnection().getOfflineId(), packet );
                packet.writerIndex( previous );
            }
        }
        packet.readerIndex( readerIndex );
    }
}
