package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<DefinedPacket>
{
    @Setter
    @Getter
    @NonNull
    private Protocol protocol;
    private final Direction direction;
    @Setter
    @Getter
    @NonNull
    private ProtocolVersion protocolVersion;

    @Override
    protected void encode(ChannelHandlerContext ctx, DefinedPacket msg, ByteBuf out) throws Exception {
        Protocol.DirectionData prot = protocol.getDirectionData(direction);
        
        int packetID = prot.getId( msg.getClass(), protocolVersion );
        //System.out.println("ENC, id: " + packetID + ", dir: " + direction.name());
        
        if(!protocolVersion.isLegacy())
        	DefinedPacket.writeVarInt( packetID, out );
        else
        	out.writeByte(packetID);
        
        msg.write( out, prot.getDirection(), protocolVersion );
    }
}
