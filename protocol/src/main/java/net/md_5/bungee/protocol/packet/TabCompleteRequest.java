package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.Protocol;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TabCompleteRequest extends DefinedPacket {
	private int transactionId;
	private String cursor;
	private boolean assumeCommand;
	private boolean hasPositon;
	private long position;

	public TabCompleteRequest(int transactionId, String cursor) {
		this.transactionId = transactionId;
		this.cursor = cursor;
	}

	public TabCompleteRequest(String cursor, boolean assumeCommand, boolean hasPosition, long position) {
		this.cursor = cursor;
		this.assumeCommand = assumeCommand;
		this.hasPositon = hasPosition;
		this.position = position;
	}

	@Override
	public void read(ByteBuf buf, Direction direction, Protocol protocolVersion) {
		if (protocolVersion.newerOrEqual(Protocol.MC_1_13_0))
			transactionId = readVarInt(buf);
		

		cursor = readString(buf, protocolVersion);

		if (protocolVersion.newerOrEqual(Protocol.MC_1_8_0) && protocolVersion.olderThan(Protocol.MC_1_13_0)) {
			if (protocolVersion.newerOrEqual(Protocol.MC_1_9_0))
				assumeCommand = buf.readBoolean();
			
			if (hasPositon = buf.readBoolean())
				position = buf.readLong();
		}
	}

	@Override
	public void write(ByteBuf buf, Direction direction, Protocol protocolVersion) {
		if (protocolVersion.newerOrEqual(Protocol.MC_1_13_0))
			writeVarInt(transactionId, buf);
		

		writeString(cursor, buf, protocolVersion);

		if (protocolVersion.newerOrEqual(Protocol.MC_1_8_0) && protocolVersion.olderThan(Protocol.MC_1_13_0)) {
			if (protocolVersion.newerOrEqual(Protocol.MC_1_9_0))
				buf.writeBoolean(assumeCommand);

			buf.writeBoolean(hasPositon);
			if (hasPositon)
				buf.writeLong(position);
		}
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
