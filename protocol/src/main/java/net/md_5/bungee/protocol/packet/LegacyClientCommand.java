package net.md_5.bungee.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LegacyClientCommand extends DefinedPacket {
	public int commandCode;

	@Override
	public void write(ByteBuf buf) {
		buf.writeByte(commandCode & 0xFF);
	}
	
	@Override
	public void read(ByteBuf buf) {
		commandCode = buf.readByte();
	}
	
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}
	
	
}
