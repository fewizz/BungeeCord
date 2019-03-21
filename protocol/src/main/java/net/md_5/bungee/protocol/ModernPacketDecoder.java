package net.md_5.bungee.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.Getter;
import lombok.Setter;

public class ModernPacketDecoder extends MessageToMessageDecoder<ByteBuf> implements PacketDecoder
{
    @Setter
    @Getter
    private NetworkState connectionState = NetworkState.HANDSHAKE;
    private final Direction direction;
    @Setter
    @Getter
    private Protocol protocol;
    
    public ModernPacketDecoder(Direction dir, int pv) {
		this.direction = dir;
		protocol = Protocol.byNumber(pv, ProtocolGen.POST_NETTY);
	}

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		ByteBuf slice = in.copy();
		
		try {
    		int packetId = Packet.readVarInt( in );

    		Packet packet = protocol.createPacket(connectionState, packetId, direction);
    		
    		if(packet == null)
    			in.skipBytes( in.readableBytes() );
    		else
    			packet.read( in, direction, protocol );
    		
			out.add( new PacketWrapper( packet, slice, packetId ) );
			slice = null;
        		
			if ( in.isReadable() )
				throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " cs " + connectionState + " Direction " + direction );
					
    	} finally {
    		if ( slice != null )
    			slice.release();
    	}

    }
}
