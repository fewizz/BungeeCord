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
public class ClientSettings extends DefinedPacket
{

    private String locale;
    private byte viewDistance;
    private int chatFlags;
    private boolean chatColours;
    private byte difficulty;
    private byte skinParts;
    private int mainHand;

    @Override
    public void read(ByteBuf buf, Direction direction, Protocol protocolVersion)
    {
        locale = readString( buf, protocolVersion );
        viewDistance = buf.readByte();
        
        if(protocolVersion.isLegacy()) {
        	int b = buf.readUnsignedByte();
        	chatFlags = b & 0b111;
    		chatColours = (b & 0b1000) == 0b1000;
        }
        else {
        	if(protocolVersion.newerOrEqual(Protocol.MC_1_9_0)) {
            	chatFlags = DefinedPacket.readVarInt( buf );
            	chatColours = buf.readBoolean();
            }
            else {
        		chatFlags = buf.readUnsignedByte();
        		chatColours = buf.readBoolean();
            }
        }
        
        if ( protocolVersion.olderOrEqual(Protocol.MC_1_7_6 ))
        {
            difficulty = buf.readByte();
        }
        skinParts = buf.readByte();
        if ( protocolVersion.newerOrEqual(Protocol.MC_1_9_0 ))
        {
            mainHand = DefinedPacket.readVarInt( buf );
        }
    }

    @Override
    public void write(ByteBuf buf, Direction direction, Protocol protocolVersion)
    {
        writeString( locale, buf, protocolVersion );
        buf.writeByte( viewDistance );
        if(protocolVersion.isLegacy()) 
        	buf.writeByte(chatFlags | ((chatColours ? 1 : 0) << 3));
        else {
        	if ( protocolVersion.newerOrEqual(Protocol.MC_1_9_0 ))
        		DefinedPacket.writeVarInt( chatFlags, buf );
    		else
        		buf.writeByte( chatFlags );
        	buf.writeBoolean( chatColours );
    	}
        
        if ( protocolVersion.olderOrEqual(Protocol.MC_1_7_6) )
        {
            buf.writeByte( difficulty );
        }
        buf.writeByte( skinParts );
        if ( protocolVersion.newerOrEqual(Protocol.MC_1_9_0) )
        {
            DefinedPacket.writeVarInt( mainHand, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
