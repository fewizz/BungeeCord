package net.md_5.bungee.protocol.packet;

import java.util.Locale;

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
public class ScoreboardObjective extends DefinedPacket
{

    private String name;
    private String value;
    private HealthDisplay type;
    /**
     * 0 to create, 1 to remove, 2 to update display text.
     */
    private byte action;

    @Override
    public void read(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        name = readString( buf );
        if ( protocolVersion.olderOrEqual(ProtocolVersion.MC_1_7_6 ))
        {
            value = readString( buf );
        }
        action = buf.readByte();
        if ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_8) && ( action == 0 || action == 2 ) )
        {
            value = readString( buf );
            if ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_13 ))
            {
                type = HealthDisplay.values()[readVarInt( buf )];
            } else
            {
                type = HealthDisplay.fromString( readString( buf ) );
            }
        }
    }

    @Override
    public void write(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        writeString( name, buf );
        if ( protocolVersion.olderOrEqual(ProtocolVersion.MC_1_7_6 ))
        {
            writeString( value, buf );
        }
        buf.writeByte( action );
        if ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_8 ) && ( action == 0 || action == 2 ) )
        {
            writeString( value, buf );
            if ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_13 ))
            {
                writeVarInt( type.ordinal(), buf );
            } else
            {
                writeString( type.toString(), buf );
            }
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public enum HealthDisplay
    {

        INTEGER, HEARTS;

        @Override
        public String toString()
        {
            return super.toString().toLowerCase( Locale.ROOT );
        }

        public static HealthDisplay fromString(String s)
        {
            return valueOf( s.toUpperCase( Locale.ROOT ) );
        }
    }
}
