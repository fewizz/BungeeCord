package net.md_5.bungee.protocol;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public abstract class DefinedPacket
{

    public static void writeString(String s, ByteBuf buf)
    {
        if ( s.length() > Short.MAX_VALUE )
        {
            throw new OverflowPacketException( String.format( "Cannot send string longer than Short.MAX_VALUE (got %s characters)", s.length() ) );
        }

        byte[] b = s.getBytes( Charsets.UTF_8 );
        writeVarInt( b.length, buf );
        buf.writeBytes( b );
    }

    public static String readString(ByteBuf buf)
    {
        int len = readVarInt( buf );
        if ( len > Short.MAX_VALUE )
        {
            throw new OverflowPacketException( String.format( "Cannot receive string longer than Short.MAX_VALUE (got %s characters)", len ) );
        }

        byte[] b = new byte[ len ];
        buf.readBytes( b );

        return new String( b, Charsets.UTF_8 );
    }

    public static void writeArray(byte[] b, ByteBuf buf)
    {
        if ( b.length > Short.MAX_VALUE )
        {
            throw new OverflowPacketException( String.format( "Cannot send byte array longer than Short.MAX_VALUE (got %s bytes)", b.length ) );
        }
        writeVarInt( b.length, buf );
        buf.writeBytes( b );
    }

    public static byte[] toArray(ByteBuf buf)
    {
        byte[] ret = new byte[ buf.readableBytes() ];
        buf.readBytes( ret );

        return ret;
    }

    public static byte[] readArray(ByteBuf buf)
    {
        return readArray( buf, buf.readableBytes() );
    }

    public static byte[] readArray(ByteBuf buf, int limit)
    {
        int len = readVarInt( buf );
        if ( len > limit )
        {
            throw new OverflowPacketException( String.format( "Cannot receive byte array longer than %s (got %s bytes)", limit, len ) );
        }
        byte[] ret = new byte[ len ];
        buf.readBytes( ret );
        return ret;
    }
    
    public static void writeArrayLegacy(byte[] b, ByteBuf buf, boolean allowExtended)
    {
        // (Integer.MAX_VALUE & 0x1FFF9A ) = 2097050 - Forge's current upper limit
        if ( allowExtended )
        {
            Preconditions.checkArgument( b.length <= ( Integer.MAX_VALUE & 0x1FFF9A ), "Cannot send array longer than 2097050 (got %s bytes)", b.length );
        } else
        {
            Preconditions.checkArgument( b.length <= Short.MAX_VALUE, "Cannot send array longer than Short.MAX_VALUE (got %s bytes)", b.length );
        }
        // Write a 2 or 3 byte number that represents the length of the packet. (3 byte "shorts" for Forge only)
        // No vanilla packet should give a 3 byte packet, this method will still retain vanilla behaviour.
        writeVarShort( buf, b.length );
        buf.writeBytes( b );
    }

    public static byte[] readArrayLegacy(ByteBuf buf)
    {
        // Read in a 2 or 3 byte number that represents the length of the packet. (3 byte "shorts" for Forge only)
        // No vanilla packet should give a 3 byte packet, this method will still retain vanilla behaviour.
        int len = readVarShort( buf );

        // (Integer.MAX_VALUE & 0x1FFF9A ) = 2097050 - Forge's current upper limit
        Preconditions.checkArgument( len <= ( Integer.MAX_VALUE & 0x1FFF9A ), "Cannot receive array longer than 2097050 (got %s bytes)", len );

        byte[] ret = new byte[ len ];
        buf.readBytes( ret );
        return ret;
    }

    public static int[] readVarIntArray(ByteBuf buf)
    {
        int len = readVarInt( buf );
        int[] ret = new int[ len ];

        for ( int i = 0; i < len; i++ )
        {
            ret[i] = readVarInt( buf );
        }

        return ret;
    }

    public static void writeStringArray(List<String> s, ByteBuf buf)
    {
        writeVarInt( s.size(), buf );
        for ( String str : s )
        {
            writeString( str, buf );
        }
    }

    public static List<String> readStringArray(ByteBuf buf)
    {
        int len = readVarInt( buf );
        List<String> ret = new ArrayList<>( len );
        for ( int i = 0; i < len; i++ )
        {
            ret.add( readString( buf ) );
        }
        return ret;
    }

    public static int readVarInt(ByteBuf input)
    {
        return readVarInt( input, 5 );
    }

    public static int readVarInt(ByteBuf input, int maxBytes)
    {
        int out = 0;
        int bytes = 0;
        byte in;
        while ( true )
        {
            in = input.readByte();

            out |= ( in & 0x7F ) << ( bytes++ * 7 );

            if ( bytes > maxBytes )
            {
                throw new RuntimeException( "VarInt too big" );
            }

            if ( ( in & 0x80 ) != 0x80 )
            {
                break;
            }
        }

        return out;
    }

    public static void writeVarInt(int value, ByteBuf output)
    {
        int part;
        while ( true )
        {
            part = value & 0x7F;

            value >>>= 7;
            if ( value != 0 )
            {
                part |= 0x80;
            }

            output.writeByte( part );

            if ( value == 0 )
            {
                break;
            }
        }
    }

    public static int readVarShort(ByteBuf buf)
    {
        int low = buf.readUnsignedShort();
        int high = 0;
        if ( ( low & 0x8000 ) != 0 )
        {
            low = low & 0x7FFF;
            high = buf.readUnsignedByte();
        }
        return ( ( high & 0xFF ) << 15 ) | low;
    }

    public static void writeVarShort(ByteBuf buf, int toWrite)
    {
        int low = toWrite & 0x7FFF;
        int high = ( toWrite & 0x7F8000 ) >> 15;
        if ( high != 0 )
        {
            low = low | 0x8000;
        }
        buf.writeShort( low );
        if ( high != 0 )
        {
            buf.writeByte( high );
        }
    }

    public static void writeUUID(UUID value, ByteBuf output)
    {
        output.writeLong( value.getMostSignificantBits() );
        output.writeLong( value.getLeastSignificantBits() );
    }

    public static UUID readUUID(ByteBuf input)
    {
        return new UUID( input.readLong(), input.readLong() );
    }

    public void read(ByteBuf buf)
    {
        throw new UnsupportedOperationException( "Packet must implement read method" );
    }

    public void read(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        read( buf );
    }

    public void write(ByteBuf buf)
    {
        throw new UnsupportedOperationException( "Packet must implement write method" );
    }

    public void write(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion)
    {
        write( buf );
    }

    public abstract void handle(AbstractPacketHandler handler) throws Exception;

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
    
    public static void writeCharArray(char[] arr, ByteBuf buf) {
    	for(int i = 0; i < arr.length; i++) buf.writeChar(arr[i]);
    }
    
    public static char[] readCharArray(ByteBuf buf, int size) {
    	char[] arr = new char[size];
    	for(int i = 0; i < size; i++)
    		arr[i]=buf.readChar();
    	return arr;
    }
    
    public static void writeLegacyByteArray(ByteBuf buf, byte[] arr) {
    	buf.writeShort(arr.length);
    	buf.writeBytes(arr);
    }
    
    public static byte[] readLegacyByteArray(ByteBuf buf) {
    	int size = buf.readShort();
    	byte[] arr = new byte[size];
    	buf.readBytes(arr);
    	return arr;
    }
    
    public static String readLegacyString(ByteBuf buf, int maxSize) {
    	int len = buf.readShort();
    	checkLegacyStringLen(len, maxSize);
    	return new String(readCharArray(buf, len));
    }
    
    public static void writeLegacyString(String str, ByteBuf buf) {
    	if(str.length() > Short.MAX_VALUE)
    		throw new RuntimeException("Legacy string is too wide to write");
    	buf.writeShort(str.length());
    	writeCharArray(str.toCharArray(), buf);
    }
    
    public static boolean canReadLegacyString(ByteBuf buf, int maxSize) {
    	if(!buf.isReadable(Short.BYTES))
    		return false;
    	int len = buf.getShort(buf.readerIndex());
    	checkLegacyStringLen(len, maxSize);
    	return buf.isReadable(Character.BYTES*len + Short.BYTES);
    }
    
    public static void skipLegacyString(ByteBuf buf, int maxSize) {
    	int len = buf.readShort();
    	checkLegacyStringLen(len, maxSize);
    	buf.skipBytes(len*Character.BYTES);
    }
    
    private static void checkLegacyStringLen(int len, int max) {
    	if(len > Short.MAX_VALUE || len > max)
    		throw new OverflowPacketException("Legacy string is too wide to read");
    	if(len < 0)
    		throw new RuntimeException("Legacy string size is less than zero");
    }
    
    public static void skipLegacyTag(ByteBuf buf) {
    	int len = buf.readShort();
    	if(len > 0) buf.skipBytes(len);
    }
    
    public static void skipLegacyItemStack(ByteBuf buf) {
    	int l = buf.readShort();
    	if(l >= 0) {
    		buf.skipBytes(1 + Short.BYTES);
    		skipLegacyTag(buf);
    	}
    }
    
    public static void skipLegacyWatchableObjects(ByteBuf buf) {
    	while(true) {
    		int b = buf.readUnsignedByte();
    		if(b == 127)
    			return;
    		
    		int type = (b & 0b11100000) >> 5;
            
            switch (type) {
			case 0:
				buf.skipBytes(1);
				break;
			case 1:
				buf.skipBytes(Short.BYTES);
				break;
			case 2:
				buf.skipBytes(Integer.BYTES);
				break;
			case 3:
				buf.skipBytes(Float.BYTES);
				break;
			case 4:
				skipLegacyString(buf, 64);
				break;
			case 5:
				skipLegacyItemStack(buf);
				break;
			case 6:
				buf.skipBytes(Integer.BYTES*3);
				break;
			default:
				throw new RuntimeException();
			}
    	}
    }
}
