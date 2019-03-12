package net.md_5.bungee.protocol.packet.old;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.ProtocolVersion;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LoginOld extends DefinedPacket {
    public int entityId;
    public String levelType;
    public boolean hardcore;
    public int gameMode;
    public int dimension;
    public byte difficulty;
    public byte worldH;
    public byte maxPlayers;
    
    @Override
    public void read(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion) {
    	if(protocolVersion.newerThan(ProtocolVersion.MC_1_6_4))
    		throw new RuntimeException();
    	entityId = buf.readInt();
		levelType = readLegacyString(buf, 16);
		int mode = buf.readUnsignedByte();
		hardcore = (mode & 0x1000) != 0;
		gameMode = (short) (mode & 0b111);
		dimension = buf.readByte();
		difficulty = buf.readByte();
		worldH = buf.readByte();
		maxPlayers = buf.readByte();
    }
    
    @Override
    public void write(ByteBuf buf, Direction direction, ProtocolVersion protocolVersion) {
    	if(protocolVersion.newerThan(ProtocolVersion.MC_1_6_4))
    		throw new RuntimeException();
    	buf.writeInt(entityId);
    	writeLegacyString(levelType, buf);
    	buf.writeByte(gameMode | (hardcore ? 0b1000 : 0));
    	buf.writeByte(dimension);
    	buf.writeByte(difficulty);
    	buf.writeByte(worldH);
    	buf.writeByte(maxPlayers);
    }
    
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
}
