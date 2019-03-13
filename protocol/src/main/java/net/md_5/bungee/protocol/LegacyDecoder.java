package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.md_5.bungee.protocol.packet.old.LoginRequestOld;
import net.md_5.bungee.protocol.packet.old.StatusRequestOld;

public abstract class LegacyDecoder extends ChannelInboundHandlerAdapter
{

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf in = (ByteBuf) msg;
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
        	ctx.fireChannelRead(in);
        }
        if ( packetID == 0x02)
        {
        	LoginRequestOld lr = new LoginRequestOld();
        	lr.read(in);
        	onLegacy(lr.getProtocolVer());
        	in.resetReaderIndex();
        	ctx.fireChannelRead(in);
        }
        ctx.pipeline().remove(this);
    }
    
    public abstract void onLegacy(int protocolVersion);
}
