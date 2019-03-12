package net.md_5.bungee.protocol.packet;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.util.Locale;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.MinecraftInput;
import net.md_5.bungee.protocol.ProtocolVersion;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PluginMessage extends DefinedPacket
{

    public static final Function<String, String> MODERNISE = new Function<String, String>()
    {
        @Override
        public String apply(String tag)
        {
            // Transform as per Bukkit
            if ( tag.equals( "BungeeCord" ) )
            {
                return "bungeecord:main";
            }
            if ( tag.equals( "bungeecord:main" ) )
            {
                return "BungeeCord";
            }

            // Code that gets to here is UNLIKELY to be viable on the Bukkit side of side things,
            // but we keep it anyway. It will eventually be enforced API side.
            if ( tag.indexOf( ':' ) != -1 )
            {
                return tag;
            }

            return "legacy:" + tag.toLowerCase( Locale.ROOT );
        }
    };
    public static final Predicate<PluginMessage> SHOULD_RELAY = new Predicate<PluginMessage>()
    {
        @Override
        public boolean apply(PluginMessage input)
        {
            return ( input.getTag().equals( "REGISTER" ) || input.getTag().equals( "minecraft:register" ) || input.getTag().equals( "MC|Brand" ) || input.getTag().equals( "minecraft:brand" ) ) && input.getData().length < Byte.MAX_VALUE;
        }
    };
    //
    private String tag;
    private byte[] data;

    /**
     * Allow this packet to be sent as an "extended" packet.
     */
    private boolean allowExtendedPacket = false;

    @Override
    public void read(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
    	if(protocolVersion.olderOrEqual(ProtocolVersion.MC_1_6_4)) {
    		tag = readLegacyString(buf, 20);
    		readLegacyByteArray(buf);
    	}
    	else if ( protocolVersion.olderThan(ProtocolVersion.MC_1_8_0) )
        {
        	tag = readString( buf );
            data = readArrayLegacy( buf );
        } else
        {
        	tag = ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_13_0 )) ? MODERNISE.apply( readString( buf ) ) : readString( buf );
            int maxSize = direction == Direction.TO_SERVER ? Short.MAX_VALUE : 0x100000;
            Preconditions.checkArgument( buf.readableBytes() < maxSize );
            data = new byte[ buf.readableBytes() ];
            buf.readBytes( data );
        }
    }

    @Override
    public void write(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
    	if(protocolVersion.olderOrEqual(ProtocolVersion.MC_1_6_4)) {
    		writeLegacyString(tag, buf);
    		writeLegacyByteArray(buf, data);
    	}
    	else if ( protocolVersion.olderThan(ProtocolVersion.MC_1_8_0 ))
        {
        	writeString( tag, buf );
            writeArrayLegacy( data, buf, allowExtendedPacket );
        } else
        {
        	writeString( ( protocolVersion.newerOrEqual(ProtocolVersion.MC_1_13_0 )) ? MODERNISE.apply( tag ) : tag, buf );
            buf.writeBytes( data );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }

    public DataInput getStream()
    {
        return new DataInputStream( new ByteArrayInputStream( data ) );
    }
    
    public MinecraftInput getMCStream()
    {
        return new MinecraftInput( Unpooled.wrappedBuffer( data ) );
    }
}
