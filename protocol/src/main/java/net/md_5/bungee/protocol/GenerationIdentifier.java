package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public abstract class GenerationIdentifier extends ChannelInboundHandlerAdapter
{

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf in = (ByteBuf) msg;
        if ( !in.isReadable() )
            return;

        short packetID = in.getUnsignedByte(in.readerIndex());

        onIdentified(packetID == 0xFE || packetID == 0x02 ? ProtocolGen.PRE_NETTY : ProtocolGen.MODERN, ctx);
        ctx.pipeline().remove(this);
    }
    
    public abstract void onIdentified(ProtocolGen gen, ChannelHandlerContext ctx);
}
