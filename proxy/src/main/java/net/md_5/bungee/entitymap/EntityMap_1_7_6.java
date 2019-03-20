package net.md_5.bungee.entitymap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.Packet;
import net.md_5.bungee.protocol.ProtocolVersion;

class EntityMap_1_7_6 extends EntityMap_1_7_2
{

    static final EntityMap_1_7_6 INSTANCE = new EntityMap_1_7_6();

    @Override
    @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
    public void rewriteClientbound(ByteBuf packet, int oldId, int newId, ProtocolVersion pv)
    {
        super.rewriteClientbound( packet, oldId, newId, pv );

        int readerIndex = packet.readerIndex();
        int packetId = Packet.readVarInt( packet );
        int packetIdLength = packet.readerIndex() - readerIndex;
        if ( packetId == 0x0C /* Spawn Player */ )
        {
            Packet.readVarInt( packet );
            int idLength = packet.readerIndex() - readerIndex - packetIdLength;
            String uuid = Packet.readString( packet );
            String username = Packet.readString( packet );
            int props = Packet.readVarInt( packet );
            if ( props == 0 )
            {
                UserConnection player = (UserConnection) BungeeCord.getInstance().getPlayer( username );
                if ( player != null )
                {
                    LoginResult profile = player.getPendingConnection().getLoginProfile();
                    if ( profile != null && profile.getProperties() != null
                            && profile.getProperties().length >= 1 )
                    {
                        ByteBuf rest = packet.copy();
                        packet.readerIndex( readerIndex );
                        packet.writerIndex( readerIndex + packetIdLength + idLength );
                        Packet.writeString( player.getUniqueId().toString(), packet );
                        Packet.writeString( username, packet );
                        Packet.writeVarInt( profile.getProperties().length, packet );
                        for ( LoginResult.Property property : profile.getProperties() )
                        {
                            Packet.writeString( property.getName(), packet );
                            Packet.writeString( property.getValue(), packet );
                            Packet.writeString( property.getSignature(), packet );
                        }
                        packet.writeBytes( rest );
                        rest.release();
                    }
                }
            }
        }
        packet.readerIndex( readerIndex );
    }
}
