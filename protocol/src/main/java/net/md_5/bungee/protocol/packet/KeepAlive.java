package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.Packet;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.Protocol;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class KeepAlive extends Packet
{

    private long randomId;

    @Override
    public void read(ByteBuf buf, Direction direction, Protocol protocolVersion)
    {
        if ( protocolVersion.newerOrEqual(Protocol.MC_1_12_2 ))
        {
            randomId = buf.readLong();
        } else if ( protocolVersion.newerOrEqual(Protocol.MC_1_8_0 ))
        {
            randomId = readVarInt( buf );
        } else
        {
            randomId = buf.readInt();
        }
    }

    @Override
    public void write(ByteBuf buf, Direction direction, Protocol protocolVersion)
    {
        if ( protocolVersion.newerOrEqual(Protocol.MC_1_12_2 ))
        {
            buf.writeLong( randomId );
        } else if ( protocolVersion.newerOrEqual(Protocol.MC_1_8_0 ))
        {
            writeVarInt( (int) randomId, buf );
        } else
        {
            buf.writeInt( (int) randomId );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
