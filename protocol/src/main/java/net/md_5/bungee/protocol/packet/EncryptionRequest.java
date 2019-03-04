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
public class EncryptionRequest extends DefinedPacket
{

    private String serverId;
    private byte[] publicKey;
    private byte[] verifyToken;

    @Override
    public void read(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        serverId = readString( buf );
        if ( protocolVersion.olderThan(ProtocolVersion.MC_1_8_0) )
        {
            publicKey = readArrayLegacy( buf );
            verifyToken = readArrayLegacy( buf );
        } else
        {
            publicKey = readArray( buf );
            verifyToken = readArray( buf );
        }
    }

    @Override
    public void write(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        writeString( serverId, buf );
        if ( protocolVersion.olderThan(ProtocolVersion.MC_1_8_0) )
        {
            writeArrayLegacy( publicKey, buf, false );
            writeArrayLegacy( verifyToken, buf, false );
        } else
        {
            writeArray( publicKey, buf );
            writeArray( verifyToken, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
