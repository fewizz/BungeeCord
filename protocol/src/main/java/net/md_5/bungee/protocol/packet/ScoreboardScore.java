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
public class ScoreboardScore extends DefinedPacket
{

    private String itemName;
    /**
     * 0 = create / update, 1 = remove.
     */
    private byte action;
    private String scoreName;
    private int value;

    @Override
    public void read(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
    	
        itemName = readString( buf, protocolVersion );
        action = buf.readByte();
        if ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_8_0 ))
        {
            scoreName = readString( buf );
            if ( action != 1 )
            {
                value = readVarInt( buf );
            }
        } else
        {
            if ( action != 1 )
            {
                scoreName = readString( buf, protocolVersion );
                value = buf.readInt();
            }
        }
    }

    @Override
    public void write(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        writeString( itemName, buf, protocolVersion );
        buf.writeByte( action );
        if ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_8_0 ))
        {
            writeString( scoreName, buf );
            if ( action != 1 )
            {
                writeVarInt( value, buf );
            }
        } else
        {
            if ( action != 1 )
            {
                writeString( scoreName, buf, protocolVersion );
                buf.writeInt( value );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
