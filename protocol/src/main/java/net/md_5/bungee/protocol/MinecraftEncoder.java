package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
public class MinecraftEncoder extends MessageToByteEncoder<DefinedPacket>
{

    @Setter
    private Protocol protocol;
    private boolean server;
    @Setter
    private ProtocolVersion protocolVersion;

    @Override
    protected void encode(ChannelHandlerContext ctx, DefinedPacket msg, ByteBuf out) throws Exception
    {
        Protocol.DirectionData prot = ( server ) ? protocol.TO_CLIENT : protocol.TO_SERVER;
        if(protocolVersion.newerThan(ProtocolVersion.MC_1_6_4))
        	DefinedPacket.writeVarInt( prot.getId( msg.getClass(), protocolVersion ), out );
        else
        	out.writeByte(prot.getId(msg.getClass(), protocolVersion));;
        msg.write( out, prot.getDirection(), protocolVersion );
    }
}
