package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<Packet>
{
    @Setter
    @Getter
    @NonNull
    private NetworkState connectionStatus;
    private final Direction direction;
    @Setter
    @Getter
    @NonNull
    private ProtocolVersion protocolVersion;

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
        int packetID = protocolVersion.idOf(msg, direction);
        //System.out.println("ENC, id: " + packetID + ", dir: " + direction.name());
        
        if(!protocolVersion.isLegacy())
        	Packet.writeVarInt( packetID, out );
        else
        	out.writeByte(packetID);
        
        msg.write( out, direction, protocolVersion );
    }
}
