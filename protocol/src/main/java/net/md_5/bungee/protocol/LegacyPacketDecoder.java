package net.md_5.bungee.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class LegacyPacketDecoder extends ByteToMessageDecoder implements PacketDecoder
{
    @Setter
    @Getter
    private NetworkState connectionStatus = NetworkState.LEGACY;
    private final Direction direction;
    @Setter
    @Getter
    private Protocol protocolVersion;
    
    public LegacyPacketDecoder(Direction dir, int pv) {
		this.direction = dir;
		protocolVersion = Protocol.byNumber(pv, ProtocolGen.PRE_NETTY);
	}

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    	while(in.isReadable()) {
			int begin = in.readerIndex();
			
			try {
				int packetId = in.readUnsignedByte();

				Packet packet = protocolVersion.createPacket(connectionStatus, packetId, direction);
    		
				if(packet == null)
					throw new RuntimeException(
							"Don't know that packet" +
							", id: " + packetId +
							", direction: " + direction.name()
    					);
    			//System.out.println("DEC, id: " + packetId + ", dir: " + direction.name());
    		
    			packet.read( in, direction, protocolVersion );
    			out.add( new PacketWrapper( packet, in.copy(begin, in.readerIndex() - begin), packetId ) );
    		} catch(Exception e) {// Temp. solution. //TODO
    			in.readerIndex(begin);
    			if(!(e instanceof IndexOutOfBoundsException))
    				throw e;
    			break;
    		}	
    	} 
    }
}