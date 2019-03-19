package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

@EqualsAndHashCode(callSuper=false)
@Data
public class RespawnOld extends DefinedPacket {
	int dimension;
	int difficulty;
	int worldHeight;
	int gameMode;
	String levelType;
	
	@Override
	public void read(ByteBuf buf) {
		dimension = buf.readInt();
		difficulty = buf.readByte();
		gameMode = buf.readByte();
		worldHeight = buf.readShort();
		levelType = readLegacyString(buf, 16);
	}
	
	@Override
	public void write(ByteBuf buf) {
		buf.writeInt(dimension);
		buf.writeByte(difficulty);
		buf.writeByte(gameMode);
		buf.writeShort(worldHeight);
		writeLegacyString(levelType, buf);
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
