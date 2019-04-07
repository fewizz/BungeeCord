package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
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
@EqualsAndHashCode(callSuper = false)
public class Chat extends DefinedPacket {

	private String message;
	private byte position;

	public Chat(String message) {
		this(message, (byte) 0);
	}

	@Override
	public void read(ByteBuf buf, Direction direction, Protocol protocolVersion) {
		message = readString(buf, protocolVersion);

		if (direction == Direction.TO_CLIENT && protocolVersion.newerOrEqual(Protocol.MC_1_8))
			position = buf.readByte();
	}

	@Override
	public void write(ByteBuf buf, Direction direction, Protocol protocolVersion) {
		if (protocolVersion.isLegacy())
			writeLegacyString(message, buf);
		else
			writeString(message, buf);

		if (direction == Direction.TO_CLIENT && protocolVersion.newerOrEqual(Protocol.MC_1_8))
			buf.writeByte(position);
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
