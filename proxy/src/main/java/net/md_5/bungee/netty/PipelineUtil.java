package net.md_5.bungee.netty;

import java.util.concurrent.TimeUnit;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.protocol.*;

public class PipelineUtil {
    public static final AttributeKey<ListenerInfo> LISTENER = AttributeKey.valueOf( "ListerInfo" );
    public static final AttributeKey<UserConnection> USER = AttributeKey.valueOf( "User" );
    public static final AttributeKey<BungeeServerInfo> TARGET = AttributeKey.valueOf( "Target" );
    
    private static final int 
    	LOW_MARK = Integer.getInteger( "net.md_5.bungee.low_mark", 2 << 18 ), // 0.5 mb
    	HIGH_MARK = Integer.getInteger( "net.md_5.bungee.high_mark", 2 << 20 ); // 2 mb
    private static final WriteBufferWaterMark MARK = new WriteBufferWaterMark( LOW_MARK, HIGH_MARK );
    
    public static final String
    	BOSS = "inbound-boss",
    	FRAME_DEC = "frame_decoder",
    	PACKET_DEC = "packet_decoder",
    	FRAME_ENC = "frame_encoder",
    	PACKET_ENC = "packet_encoder",
    	TIMEOUT = "timeout",
    	DECRYPT = "decrypt",
    	ENCRYPT = "encrypt";
    
    public static void modernPacketHandlers(Channel ch, int protocolVersion, Side side) {
		ch.pipeline().addFirst(FRAME_DEC, new Varint21FrameDecoder());
		ch.pipeline().addAfter(
			FRAME_DEC,
			PACKET_DEC,
			new ModernPacketDecoder(
				side,
				protocolVersion
			)
		);
		
		ch.pipeline().addFirst(FRAME_ENC, new Varint21LengthFieldPrepender());
		ch.pipeline().addAfter(
			FRAME_ENC,
			PACKET_ENC,
			new PacketEncoder(
				NetworkState.HANDSHAKE,
				side,
				Protocol.byNumber(protocolVersion, ProtocolGen.POST_NETTY)
			)
		);
    }
    
    public static void legacyPacketHandlers(Channel ch, int protocolVersion, Side side) {
		ch.pipeline().addFirst(
			PACKET_DEC,
			new LegacyPacketDecoder(
				side,
				protocolVersion
			)
		);
		
		ch.pipeline().addFirst(
			PACKET_ENC,
			new PacketEncoder(
				NetworkState.LEGACY,
				side,
				Protocol.byNumber(protocolVersion, ProtocolGen.PRE_NETTY)
			)
		);
    }
    
    public static void basicHandlers(Channel ch, PacketHandler ph) {
    	try {
            ch.config().setOption( ChannelOption.IP_TOS, 0x18 );
        } catch ( ChannelException ex ) {/* IP_TOS is not supported (Windows XP / Windows Server 2003)*/}
    	
        ch.config().setAllocator( PooledByteBufAllocator.DEFAULT );
        ch.config().setWriteBufferWaterMark( MARK );

        ch.pipeline().addLast(
    		TIMEOUT,
    		new ReadTimeoutHandler( BungeeCord.getInstance().config.getTimeout(), TimeUnit.MILLISECONDS )
    	);
        
    	ch.pipeline().addLast(PipelineUtil.BOSS, new HandlerBoss().setHandler(ph));
    }
    
    public static void packetHandlers(Channel ch, Protocol pv, Side side) {
    	switch (pv.generation) {
		case POST_NETTY:
			modernPacketHandlers(ch, pv.version, side);
			break;

		case PRE_NETTY:
			legacyPacketHandlers(ch, pv.version, side);
			break;
		}
    }
    
    public static void addHandlers(Channel ch, Protocol pv, Side side, PacketHandler ph) {
    	basicHandlers(ch, ph);
    	packetHandlers(ch, pv, side);
    }
}
