package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.Packet;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ScoreboardDisplay extends Packet
{

    /**
     * 0 = list, 1 = side, 2 = below.
     */
    private byte position;
    private String name;

    @Override
    public void read(ByteBuf buf, Direction d, Protocol v)
    {
        position = buf.readByte();
        name = readString(buf, v);
    }

    @Override
    public void write(ByteBuf buf, Direction d, Protocol v)
    {
        buf.writeByte( position );
        writeString(name, buf, v);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
