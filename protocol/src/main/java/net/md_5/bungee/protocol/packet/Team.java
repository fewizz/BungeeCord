package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.Packet;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.ProtocolVersion;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Team extends Packet {

	private String name;
	/**
	 * 0 - create, 1 remove, 2 info update, 3 player add, 4 player remove.
	 */
	private byte mode;
	private String displayName;
	private String prefix;
	private String suffix;
	private String nameTagVisibility;
	private String collisionRule;
	private int color;
	private byte friendlyFire;
	private String[] players;

	/**
	 * Packet to destroy a team.
	 */
	public Team(String name) {
		this.name = name;
		this.mode = 1;
	}

	@Override
	public void read(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion) {
		name = readString(buf, protocolVersion);

		mode = buf.readByte();
		if (mode == 0 || mode == 2) {
			displayName = readString(buf, protocolVersion);
			
			if (protocolVersion.olderThan(ProtocolVersion.MC_1_13_0)) {
				prefix = readString(buf, protocolVersion);
				suffix = readString(buf, protocolVersion);
			}
			friendlyFire = buf.readByte();
			if (protocolVersion.newerOrEqual(ProtocolVersion.MC_1_8_0)) {
				nameTagVisibility = readString(buf);
				if (protocolVersion.newerOrEqual(ProtocolVersion.MC_1_9_0)) {
					collisionRule = readString(buf);
				}
				color = (protocolVersion.newerOrEqual(ProtocolVersion.MC_1_13_0)) ? readVarInt(buf) : buf.readByte();
				if (protocolVersion.newerOrEqual(ProtocolVersion.MC_1_13_0)) {
					prefix = readString(buf);
					suffix = readString(buf);
				}
			}
		}
		if (mode == 0 || mode == 3 || mode == 4) {
			int len = (protocolVersion.newerOrEqual(ProtocolVersion.MC_1_8_0)) ? readVarInt(buf) : buf.readShort();
			players = new String[len];
			for (int i = 0; i < len; i++) {
				players[i] = readString(buf, protocolVersion);
			}
		}
	}

	@Override
	public void write(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion) {
		writeString(name, buf, protocolVersion);
		
		buf.writeByte(mode);
		
		if (mode == 0 || mode == 2) {
			writeString(displayName, buf, protocolVersion);
			
			if (protocolVersion.olderThan(ProtocolVersion.MC_1_13_0)) {
				writeString(prefix, buf, protocolVersion);
				writeString(suffix, buf, protocolVersion);
			}
			buf.writeByte(friendlyFire);
			if (protocolVersion.newerOrEqual(ProtocolVersion.MC_1_8_0)) {
				writeString(nameTagVisibility, buf);
				if (protocolVersion.newerOrEqual(ProtocolVersion.MC_1_9_0)) {
					writeString(collisionRule, buf);
				}

				if (protocolVersion.newerOrEqual(ProtocolVersion.MC_1_13_0)) {
					writeVarInt(color, buf);
					writeString(prefix, buf);
					writeString(suffix, buf);
				} else {
					buf.writeByte(color);
				}
			}
		}
		if (mode == 0 || mode == 3 || mode == 4) {
			if (protocolVersion.newerOrEqual(ProtocolVersion.MC_1_8_0))
				writeVarInt(players.length, buf);
			else
				buf.writeShort(players.length);
			
			for (String player : players) {
				writeString(player, buf, protocolVersion);
			}
		}
	}

	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
