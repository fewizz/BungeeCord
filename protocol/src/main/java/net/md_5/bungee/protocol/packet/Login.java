package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.Packet;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.ProtocolVersion;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Login extends Packet
{

    private int entityId;
    private short gameMode;
    private int dimension;
    private short difficulty;
    private short maxPlayers;
    private String levelType;
    private boolean reducedDebugInfo;

    @Override
    public void read(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        entityId = buf.readInt();
        gameMode = buf.readUnsignedByte();
        if ( protocolVersion.newerThan(ProtocolVersion.MC_1_9_0 ))
        {
            dimension = buf.readInt();
        } else
        {
            dimension = buf.readByte();
        }
        difficulty = buf.readUnsignedByte();
        maxPlayers = buf.readUnsignedByte();
        levelType = readString( buf );
        if ( protocolVersion.version >= 29 )
        {
            reducedDebugInfo = buf.readBoolean();
        }
    }

    @Override
    public void write(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        buf.writeInt( entityId );
        buf.writeByte( gameMode );
        if ( protocolVersion.newerThan(ProtocolVersion.MC_1_9_0 ))
        {
            buf.writeInt( dimension );
        } else
        {
            buf.writeByte( dimension );
        }
        buf.writeByte( difficulty );
        buf.writeByte( maxPlayers );
        writeString( levelType, buf );
        if ( protocolVersion.version >= 29 )
        {
            buf.writeBoolean( reducedDebugInfo );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
