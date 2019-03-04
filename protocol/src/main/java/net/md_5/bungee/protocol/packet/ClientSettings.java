package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.ProtocolVersion;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ClientSettings extends DefinedPacket
{

    private String locale;
    private byte viewDistance;
    private int chatFlags;
    private boolean chatColours;
    private byte difficulty;
    private byte skinParts;
    private int mainHand;

    @Override
    public void read(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        locale = readString( buf );
        viewDistance = buf.readByte();
        chatFlags = protocolVersion.newerOrEqual(ProtocolVersion.MC_1_9_0) ? DefinedPacket.readVarInt( buf ) : buf.readUnsignedByte();
        chatColours = buf.readBoolean();
        if ( protocolVersion.olderOrEqual(ProtocolVersion.MC_1_7_6 ))
        {
            difficulty = buf.readByte();
        }
        skinParts = buf.readByte();
        if ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_9_0 ))
        {
            mainHand = DefinedPacket.readVarInt( buf );
        }
    }

    @Override
    public void write(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        writeString( locale, buf );
        buf.writeByte( viewDistance );
        if ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_9_0 ))
        {
            DefinedPacket.writeVarInt( chatFlags, buf );
        } else
        {
            buf.writeByte( chatFlags );
        }
        buf.writeBoolean( chatColours );
        if ( protocolVersion.olderOrEqual(ProtocolVersion.MC_1_7_6) )
        {
            buf.writeByte( difficulty );
        }
        buf.writeByte( skinParts );
        if ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_9_0) )
        {
            DefinedPacket.writeVarInt( mainHand, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
