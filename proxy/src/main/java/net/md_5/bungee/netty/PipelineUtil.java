package net.md_5.bungee.netty;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;
import lombok.NonNull;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.protocol.LegacyPacketDecoder;
import net.md_5.bungee.protocol.ModernPacketDecoder;
import net.md_5.bungee.protocol.NetworkState;
import net.md_5.bungee.protocol.PacketEncoder;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.Side;
import net.md_5.bungee.protocol.Varint21FrameDecoder;
import net.md_5.bungee.protocol.Varint21LengthFieldPrepender;

public class PipelineUtil {
    public static final AttributeKey<ListenerInfo> LISTENER = AttributeKey.valueOf("ListerInfo");
    public static final AttributeKey<UserConnection<?>> USER = AttributeKey.valueOf("User");
    public static final AttributeKey<BungeeServerInfo> TARGET = AttributeKey.valueOf("Target");
    
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
    
    public static void modernPacketHandlers(@NonNull Channel ch,@NonNull Protocol protocol, @NonNull Side side) {
    	Preconditions.checkArgument(protocol.isModern());
    	
		ch.pipeline().addFirst(FRAME_DEC, new Varint21FrameDecoder());
		ch.pipeline().addAfter(
			FRAME_DEC,
			PACKET_DEC,
			new ModernPacketDecoder(
				side,
				protocol
			)
		);
		
		ch.pipeline().addFirst(FRAME_ENC, new Varint21LengthFieldPrepender());
		ch.pipeline().addAfter(
			FRAME_ENC,
			PACKET_ENC,
			new PacketEncoder(
				NetworkState.HANDSHAKE,
				side,
				protocol
			)
		);
    }
    
    public static void legacyPacketHandlers(@NonNull Channel ch, @NonNull Protocol protocol, @NonNull Side side) {
		ch.pipeline().addFirst(
			PACKET_DEC,
			new LegacyPacketDecoder(
				side,
				protocol
			)
		);
		
		ch.pipeline().addFirst(
			PACKET_ENC,
			new PacketEncoder(
				NetworkState.LEGACY,
				side,
				protocol
			)
		);
    }
    
    public static void basicConfig(Channel ch) {
    	try {
            ch.config().setOption(ChannelOption.IP_TOS, 0x18);
        } catch (ChannelException ex ) {/* IP_TOS is not supported (Windows XP / Windows Server 2003)*/}
    	
        ch.config().setAllocator(PooledByteBufAllocator.DEFAULT);
        ch.config().setWriteBufferWaterMark(MARK);

    }
    
    public static void readTimeoutHandler(Channel ch) {
        ch.pipeline().addFirst(
    		TIMEOUT,
    		new ReadTimeoutHandler(BungeeCord.getInstance().config.getTimeout(), TimeUnit.MILLISECONDS)
    	);
    }
    
    public static void packetHandlers(@NonNull Channel ch, @NonNull Protocol pv, @NonNull Side side) {
    	switch (pv.generation) {
		case POST_NETTY:
			modernPacketHandlers(ch, pv, side);
			break;

		case PRE_NETTY:
			legacyPacketHandlers(ch, pv, side);
			break;
		}
    }
    
    public static void addHandlers(Protocol pv, Side side, PacketHandler ph) {
    	Channel ch = ph.getCh().getHandle();
    	basicConfig(ch);
    	packetHandlers(ch, pv, side);
    	readTimeoutHandler(ch);
    	
    	ch.pipeline().addLast(BOSS, new HandlerBoss(ph));
    }
}
