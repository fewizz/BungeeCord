package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolGen;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Login extends DefinedPacket implements Cloneable {

	private int entityId;
	private short gameMode;
	private int dimension;
	private short difficulty;
	private short maxPlayers;
	private String levelType;
	private boolean reducedDebugInfo;
	private byte worldHeight = -1;
	private boolean fmlVanillaComp = true;
	
	public Login setMaxPlayers(int v) {
		maxPlayers = (short) v;
		return this;
	}

	@Override
	public void read(ByteBuf buf, Direction direction, Protocol protocolVersion) {
		entityId = buf.readInt();

		if (protocolVersion.isLegacy())
			levelType = readLegacyString(buf, 16);

		gameMode = buf.readUnsignedByte();

		if (protocolVersion.newerThan(Protocol.MC_1_9_0) || !fmlVanillaComp)
			dimension = buf.readInt();
		else
			dimension = buf.readByte();

		difficulty = buf.readUnsignedByte();

		if (protocolVersion.isLegacy())
			worldHeight = buf.readByte();

		maxPlayers = buf.readUnsignedByte();

		if (!protocolVersion.isLegacy())
			levelType = readString(buf);

		if (protocolVersion.generation == ProtocolGen.POST_NETTY && protocolVersion.version >= 29)
			reducedDebugInfo = buf.readBoolean();
	}

	@Override
	public void write(ByteBuf buf, Direction direction, Protocol protocolVersion) {
		buf.writeInt(entityId);

		if (protocolVersion.isLegacy())
			writeLegacyString(levelType, buf);

		buf.writeByte(gameMode);

		if (protocolVersion.newerThan(Protocol.MC_1_9_0) || !fmlVanillaComp)
			buf.writeInt(dimension);
		else
			buf.writeByte(dimension);

		buf.writeByte(difficulty);

		if (protocolVersion.isLegacy())
			buf.writeByte(worldHeight);

		buf.writeByte(maxPlayers);

		if (!protocolVersion.isLegacy())
			writeString(levelType, buf);

		if (protocolVersion.generation == ProtocolGen.POST_NETTY && protocolVersion.version >= 29)
			buf.writeBoolean(reducedDebugInfo);
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}

	@Override
	public Login clone() {
		try {
			return (Login) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
