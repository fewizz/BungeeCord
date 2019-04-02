package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.Protocol;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EncryptionResponse extends DefinedPacket
{

    private byte[] sharedSecret;
    private byte[] verifyToken;
    
    public EncryptionResponse() {
    	sharedSecret = new byte[0];
    	verifyToken = new byte[0];
    }

    @Override
    public void read(ByteBuf buf, Direction direction, Protocol protocolVersion)
    {
    	if (protocolVersion.olderOrEqual(Protocol.MC_1_6_4)) {
    		sharedSecret = readLegacyByteArray(buf);
    		verifyToken = readLegacyByteArray(buf);
    	}
    	else if ( protocolVersion.olderThan(Protocol.MC_1_8_0) )
        {
            sharedSecret = readArrayLegacy( buf );
            verifyToken = readArrayLegacy( buf );
        } else
        {
            sharedSecret = readArray( buf, 128 );
            verifyToken = readArray( buf, 128 );
        }
    }

    @Override
    public void write(ByteBuf buf, Direction direction, Protocol protocolVersion)
    {
    	if(protocolVersion.olderOrEqual(Protocol.MC_1_6_4)) {
    		writeLegacyByteArray(buf, sharedSecret);
    		writeLegacyByteArray(buf, verifyToken);
    	}
    	else if ( protocolVersion.olderThan(Protocol.MC_1_8_0) )
        {
            writeArrayLegacy( sharedSecret, buf, false );
            writeArrayLegacy( verifyToken, buf, false );
        } else
        {
            writeArray( sharedSecret, buf );
            writeArray( verifyToken, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
