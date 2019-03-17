package net.md_5.bungee.protocol.packet.old;

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
public class StatusResponseOld extends DefinedPacket {
	public int protocolVersion;
	public String mcVersion;
	public String motd;
	public int players;
	public int max;
	
	@Override
	public void write(ByteBuf buf) {
		StringBuilder r = new StringBuilder();
		
		r.append('\u00a7');
		r.append('1');
		r.append('\u0000');
		r.append(String.valueOf(protocolVersion));
		r.append('\u0000');
		r.append(mcVersion);
		r.append('\u0000');
		r.append(motd);
		r.append('\u0000');
		r.append(String.valueOf(players));
		r.append('\u0000');
		r.append(String.valueOf(max));
		
		writeLegacyString(r.toString(), buf);
	}
	
	@Override
	public void handle(AbstractPacketHandler handler) throws Exception {
		handler.handle(this);
	}

}
