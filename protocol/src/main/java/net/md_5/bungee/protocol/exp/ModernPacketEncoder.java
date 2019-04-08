package net.md_5.bungee.protocol.exp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public interface ModernPacketEncoder<DATA> extends PacketEncoder<DATA> {
	public static void writeVarInt(ByteBuf buf, int value) {
		while((value & 0x80) != 0) {
			buf.writeByte((value & 0x7F) | (1 << 8));
			value >>= 7;
		}
		buf.writeByte(value);
	}
	
	public static void writeString(ByteBuf buf, String str) {
		writeVarInt(buf, str.length());
		ByteBufUtil.writeUtf8(buf, str);
	}
}
