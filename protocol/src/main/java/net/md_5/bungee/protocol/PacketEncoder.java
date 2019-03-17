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
    private Protocol protocol = Protocol.HANDSHAKE;
    private final Direction direction;
    @Setter
    @Getter
    @NonNull
    private ProtocolVersion protocolVersion;

    @Override
    protected void encode(ChannelHandlerContext ctx, DefinedPacket msg, ByteBuf out) throws Exception
    {
        Protocol.DirectionData prot = protocol.getDirectionData(direction);
        if(protocolVersion.newerThan(ProtocolVersion.MC_1_6_4))
        	DefinedPacket.writeVarInt( prot.getId( msg.getClass(), protocolVersion ), out );
        else
        	out.writeByte(prot.getId(msg.getClass(), protocolVersion));;
        msg.write( out, prot.getDirection(), protocolVersion );
    }
}
