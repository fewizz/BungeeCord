package net.md_5.bungee.protocol.exp;

import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;

public interface ModernPacketDecoder<DATA> extends PacketDecoder<DATA> {
	
	public static int readVarInt(ByteBuf buf) {
		int result = 0;
		
		for(;;) {
			int b = buf.readUnsignedByte();
			result |= b & 0x7F;
			if((b & 0x80) == 0)
				break;
		}
		
		return result;
	}
	
	public static String readString(ByteBuf buf) {
		int size = readVarInt(buf);
		return buf.toString(buf.readerIndex(), size, Charsets.UTF_8);
	}
}
