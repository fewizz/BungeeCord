package net.md_5.bungee.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class LegacyPacketDecoder extends ByteToMessageDecoder
{
    @Setter
    @Getter
    private Protocol protocol = Protocol.HANDSHAKE;
    private final Direction direction;
    @Setter
    private ProtocolVersion protocolVersion;
    
    public LegacyPacketDecoder(Direction dir, int pv) {
		this.direction = dir;
		protocolVersion = ProtocolVersion.getByNumber(pv, ProtocolGen.PRE_NETTY);
	}

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    	while(in.isReadable()) {
    		ByteBuf slice = in.copy();
    		
    		try {
        		int packetId = in.readUnsignedByte();

        		DefinedPacket packet = protocol.getDirectionData(direction).createPacket( packetId, protocolVersion );
        		
        		if(packet == null)
        			in.skipBytes( in.readableBytes() );
        		else
        			packet.read( in, direction, protocolVersion );
        		
    			out.add( new PacketWrapper( packet, slice, packetId ) );
    			slice = null;
					
        	} finally {
        		if ( slice != null )
        			slice.release();
        	}
    	} 
    }
}