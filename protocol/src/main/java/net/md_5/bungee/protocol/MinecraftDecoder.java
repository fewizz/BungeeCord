package net.md_5.bungee.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf>
{

    @Setter
    private Protocol protocol;
    private final boolean server;
    @Setter
    private ProtocolVersion protocolVersion;
    
    //@Setter
    //boolean legacy = false;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        Protocol.DirectionData prot = ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
        ByteBuf slice = in.copy(); // Can't slice this one due to EntityMap :(

        try
    	{
        	while(true) {
        		int packetId = -1;
            
        		if(protocolVersion != null && protocolVersion.newerThan(ProtocolVersion.MC_1_6_4))
        			packetId = DefinedPacket.readVarInt( in );
        		else
        			packetId = in.readUnsignedByte();

        		DefinedPacket packet = prot.createPacket( packetId, protocolVersion );
        		if ( packet != null )
        		{
        			packet.read( in, prot.getDirection(), protocolVersion );

        			if ( in.isReadable() )
        			{
        				if(protocolVersion.newerThan(ProtocolVersion.MC_1_6_4))
        					throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot.getDirection() );
        				else
        					continue;
        			}
        		} else
        		{
        			in.skipBytes( in.readableBytes() );
        		}

        		out.add( new PacketWrapper( packet, slice ) );
        		slice = null;
        		break;
        	}
    	} finally
    	{
    		if ( slice != null )
    		{
    			slice.release();
    		}
    	}
    }
}
