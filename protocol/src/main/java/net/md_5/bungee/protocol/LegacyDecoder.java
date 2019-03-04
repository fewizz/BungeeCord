package net.md_5.bungee.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.md_5.bungee.protocol.packet.StatusRequestOld;

public abstract class LegacyDecoder extends ByteToMessageDecoder
{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if ( !in.isReadable() )
        {
            return;
        }

        in.markReaderIndex();
        short packetID = in.readUnsignedByte();

        if ( packetID == 0xFE )
        {
        	StatusRequestOld old = new StatusRequestOld();
        	old.read(in);
        	onLegacy(old.getProtocolVer());
        	in.resetReaderIndex();
        	out.add(in);
        }

        ctx.pipeline().remove( this );
    }
    
    public abstract void onLegacy(int protocolVersion);
}
