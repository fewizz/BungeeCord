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
@EqualsAndHashCode(callSuper = false)
public class Kick extends DefinedPacket
{

    private String message;

    @Override
    public void read(ByteBuf buf, Direction d, Protocol pv)
    {
    	if(pv.isLegacy())
			message = readLegacyString(buf, 256);
    	else
    		message = readString( buf );
    }

    @Override
    public void write(ByteBuf buf, Direction d, Protocol pv)
    {
    	if(pv.isLegacy())
    		writeLegacyString(message, buf);
    	else
    		writeString( message, buf );
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
    
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StatusResponce {
    	public int protocolVersion;
    	public String mcVersion;
    	public String motd;
    	public int players;
    	public int max;
    	
    	@Override
    	public String toString() {
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
    		
    		return r.toString();
    	}
    	
    	public void parse(String str) {
    		if(str.charAt(0) != '\u00a7' && str.charAt(1) != '\u0000')
    			throw new RuntimeException();
    		
    		String[] a = str.substring(3).split("\u0000");
    		protocolVersion = Integer.valueOf(a[0]);
    		mcVersion = a[1];
    		motd = a[2];
    		players = Integer.valueOf(a[3]);
    		max = Integer.valueOf(a[4]);
    	}
    }
}
