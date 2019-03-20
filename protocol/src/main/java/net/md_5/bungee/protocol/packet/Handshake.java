package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.md_5.bungee.protocol.Packet;
import net.md_5.bungee.protocol.ProtocolGen;
import net.md_5.bungee.protocol.ProtocolVersion;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.NetworkState;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Handshake extends Packet
{

    private ProtocolVersion protocolVersion;
    private String host;
    private int port;
    @Setter
    private NetworkState requestedConnectionStatus;

    @Override
    public void read(ByteBuf buf)
    {
        protocolVersion = ProtocolVersion.byNumber(readVarInt( buf ), ProtocolGen.MODERN );
        host = readString( buf );
        port = buf.readUnsignedShort();
        requestedConnectionStatus = NetworkState.byID(readVarInt( buf ));
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeVarInt( protocolVersion.version, buf );
        writeString( host, buf );
        buf.writeShort( port );
        writeVarInt( requestedConnectionStatus.getId(), buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
