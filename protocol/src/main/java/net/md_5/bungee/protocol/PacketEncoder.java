package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<Packet>
{
    @Setter
    @Getter
    @NonNull
    private NetworkState networkState;
    private final Side side;
    @Setter
    @Getter
    @NonNull
    private Protocol protocol;

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
    	int packetID = -1;
    	try {
    		packetID = protocol.getClassToIdUnmodifiableMap(networkState, side.getInboundDirection()).get(msg.getClass());
    	} catch(Exception e) {
    		throw new RuntimeException("Can't find id of packet " + msg.getClass().getName());
    	}
        //System.out.println("ENC, id: " + packetID + ", dir: " + direction.name());
        
        if(protocol.isModern())
        	DefinedPacket.writeVarInt( packetID, out );
        else
        	out.writeByte(packetID);
        
        msg.write( out, side.getInboundDirection(), protocol );
    }
}
