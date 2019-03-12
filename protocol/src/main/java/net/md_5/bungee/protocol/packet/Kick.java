package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.ProtocolVersion;
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
public class Kick extends DefinedPacket
{

    private String message;

    @Override
    public void read(ByteBuf buf, Direction d, ProtocolVersion pv)
    {
    	if(pv.olderOrEqual(ProtocolVersion.MC_1_6_4))
    		message = readLegacyString(buf, 256);
    	else
    		message = readString( buf );
    }

    @Override
    public void write(ByteBuf buf, Direction d, ProtocolVersion pv)
    {
    	if(pv.olderOrEqual(ProtocolVersion.MC_1_6_4))
    		writeLegacyString(message, buf);
    	else
    		writeString( message, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
