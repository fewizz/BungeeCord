package net.md_5.bungee.protocol.packet;

import net.md_5.bungee.protocol.Packet;
import net.md_5.bungee.protocol.Protocol;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.Direction;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Respawn extends Packet {

	private int dimension;
	private short difficulty;
	private short gameMode;
	private short worldHeight;
	private String levelType;

	@Override
	public void read(ByteBuf buf, Direction d, Protocol p) {
		dimension = buf.readInt();
		difficulty = buf.readUnsignedByte();
		gameMode = buf.readUnsignedByte();
		if(p.isLegacy())
			worldHeight = buf.readShort();
		levelType = readString(buf, p);
	}

	@Override
	public void write(ByteBuf buf, Direction d, Protocol p) {
		buf.writeInt(dimension);
		buf.writeByte(difficulty);
		buf.writeByte(gameMode);
		if(p.isLegacy())
			buf.writeShort(worldHeight);
		writeString(levelType, buf, p);
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
