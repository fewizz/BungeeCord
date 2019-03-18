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
    private Protocol protocol = Protocol.LEGACY;
    private final Direction direction;
    @Setter
    @Getter
    private ProtocolVersion protocolVersion;
    
    public LegacyPacketDecoder(Direction dir, int pv) {
		this.direction = dir;
		protocolVersion = ProtocolVersion.getByNumber(pv, ProtocolGen.PRE_NETTY);
	}

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    	while(in.isReadable()) {
			int begin = in.readerIndex();
    		int packetId = in.readUnsignedByte();

    		DefinedPacket packet = protocol.getDirectionData(direction).createPacket( packetId, protocolVersion );
    		
    		if(packet == null)
    			throw new RuntimeException(
    				"Don't know that packet" +
					", id: " + packetId +
					", direction: " + direction.name()
    			);
    		System.out.println("DEC, id: " + packetId + ", dir: " + direction.name());
    		
    		try {
    			packet.read( in, direction, protocolVersion );
    		} catch(IndexOutOfBoundsException e) {// Temp. solution. //TODO
    			in.readerIndex(begin);
    			break;
    		}
    		out.add( new PacketWrapper( packet, in.copy(begin, in.readerIndex() - begin), packetId ) );	
    	} 
    }
}