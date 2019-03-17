package net.md_5.bungee.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.compress.PacketCompressor;
import net.md_5.bungee.compress.PacketDecompressor;
import net.md_5.bungee.protocol.ModernPacketDecoder;
import net.md_5.bungee.protocol.PacketDecoder;
import net.md_5.bungee.protocol.PacketEncoder;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolVersion;
import net.md_5.bungee.protocol.packet.Kick;

public class ChannelWrapper
{

    private final Channel ch;
    @Getter
    @Setter
    private InetSocketAddress remoteAddress;
    @Getter
    private volatile boolean closed;
    @Getter
    private volatile boolean closing;

    public ChannelWrapper(ChannelHandlerContext ctx)
    {
        this.ch = ctx.channel();
        this.remoteAddress = (InetSocketAddress) this.ch.remoteAddress();
    }

    public void setProtocol(Protocol protocol)
    {
        ((PacketDecoder)ch.pipeline().get( PipelineUtil.PACKET_DEC)).setProtocol( protocol );
        ((PacketEncoder)ch.pipeline().get( PipelineUtil.PACKET_ENC )).setProtocol( protocol );
    }
    
    public Protocol getProtocol() { return ch.pipeline().get(PacketEncoder.class).getProtocol(); }

    public void setVersion(ProtocolVersion protocol)
    {
    	PacketDecoder dec = (PacketDecoder) ch.pipeline().get(PipelineUtil.PACKET_DEC);
    	if(dec.getProtocolVersion().generation != protocol.generation)
    		throw new RuntimeException( "Incompatible decoder generation" );
    	
    	PacketEncoder enc = (PacketEncoder) ch.pipeline().get(PipelineUtil.PACKET_ENC);
    	if(enc.getProtocolVersion().generation != protocol.generation)
    		throw new RuntimeException( "Incompatible encoder generation" );
    	
        dec.setProtocolVersion(protocol);
        enc.setProtocolVersion(protocol);
    }

    public void write(Object packet)
    {
        if ( !closed )
        {
            if ( packet instanceof PacketWrapper )
            {
                ( (PacketWrapper) packet ).setReleased( true );
                ch.writeAndFlush( ( (PacketWrapper) packet ).buf, ch.voidPromise() );
            } else
            {
                ch.writeAndFlush( packet, ch.voidPromise() );
            }
        }
    }

    public void markClosed()
    {
        closed = closing = true;
    }

    public void close()
    {
        close( null );
    }

    public void close(Object packet)
    {
        if ( !closed )
        {
            closed = closing = true;

            if ( packet != null && ch.isActive() )
            {
                ch.writeAndFlush( packet ).addListeners( ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE, ChannelFutureListener.CLOSE );
                ch.eventLoop().schedule( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ch.close();
                    }
                }, 250, TimeUnit.MILLISECONDS );
            } else
            {
                ch.flush();
                ch.close();
            }
        }
    }

    public void delayedClose(final Kick kick)
    {
        if ( !closing )
        {
            closing = true;

            // Minecraft client can take some time to switch protocols.
            // Sending the wrong disconnect packet whilst a protocol switch is in progress will crash it.
            // Delay 250ms to ensure that the protocol switch (if any) has definitely taken place.
            ch.eventLoop().schedule( new Runnable()
            {

                @Override
                public void run()
                {
                    close( kick );
                }
            }, 250, TimeUnit.MILLISECONDS );
        }
    }

    public void addBefore(String baseName, String name, ChannelHandler handler)
    {
        Preconditions.checkState( ch.eventLoop().inEventLoop(), "cannot add handler outside of event loop" );
        ch.pipeline().flush();
        ch.pipeline().addBefore( baseName, name, handler );
    }

    public Channel getHandle()
    {
        return ch;
    }

    public void setCompressionThreshold(int compressionThreshold)
    {
        if ( ch.pipeline().get( PacketCompressor.class ) == null && compressionThreshold != -1 )
        {
            addBefore( PipelineUtil.PACKET_ENC, "compress", new PacketCompressor() );
        }
        if ( compressionThreshold != -1 )
        {
            ch.pipeline().get( PacketCompressor.class ).setThreshold( compressionThreshold );
        } else
        {
            ch.pipeline().remove( "compress" );
        }

        if ( ch.pipeline().get( PacketDecompressor.class ) == null && compressionThreshold != -1 )
        {
            addBefore( PipelineUtil.PACKET_DEC, "decompress", new PacketDecompressor() );
        }
        if ( compressionThreshold == -1 )
        {
            ch.pipeline().remove( "decompress" );
        }
    }
}
