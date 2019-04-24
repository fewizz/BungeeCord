package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.Protocol;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Respawn extends DefinedPacket implements Cloneable {

	public Respawn setDimension(int v) {this.dimension = v; return this;}
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
	
	@Override
	public Respawn clone() {
		return new Respawn(dimension, difficulty, gameMode, worldHeight, levelType);
	}
}
