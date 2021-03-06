package net.md_5.bungee.protocol.packet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

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
public class TabCompleteResponse extends DefinedPacket
{

    private int transactionId;
    private Suggestions suggestions;
    //
    private List<String> commands;

    public TabCompleteResponse(int transactionId, Suggestions suggestions)
    {
        this.transactionId = transactionId;
        this.suggestions = suggestions;
    }

    public TabCompleteResponse(List<String> commands)
    {
        this.commands = commands;
    }

    @Override
    public void read(ByteBuf buf, Direction direction, Protocol protocolVersion)
    {
        if ( protocolVersion.newerOrEqual(Protocol.MC_1_13_0 ))
        {
            transactionId = readVarInt( buf );
            int start = readVarInt( buf );
            int length = readVarInt( buf );
            StringRange range = StringRange.between( start, start + length );

            int cnt = readVarInt( buf );
            List<Suggestion> matches = new LinkedList<>();
            for ( int i = 0; i < cnt; i++ )
            {
                String match = readString( buf );
                String tooltip = buf.readBoolean() ? readString( buf ) : null;

                matches.add( new Suggestion( range, match, new LiteralMessage( tooltip ) ) );
            }

            suggestions = new Suggestions( range, matches );
        }

        if(protocolVersion.isLegacy()) {
        	commands = new ArrayList<>();
        	commands.add(readLegacyString(buf, Short.MAX_VALUE));
        }
        else if ( protocolVersion.olderThan(Protocol.MC_1_13_0 ))
        {
            commands = readStringArray( buf );
        }
    }

    @Override
    public void write(ByteBuf buf, Direction direction, Protocol protocolVersion)
    {
        if ( protocolVersion.newerOrEqual(Protocol.MC_1_13_0 ))
        {
            writeVarInt( transactionId, buf );
            writeVarInt( suggestions.getRange().getStart(), buf );
            writeVarInt( suggestions.getRange().getLength(), buf );

            writeVarInt( suggestions.getList().size(), buf );
            for ( Suggestion suggestion : suggestions.getList() )
            {
                writeString( suggestion.getText(), buf );
                buf.writeBoolean( suggestion.getTooltip() != null && suggestion.getTooltip().getString() != null);
                if ( suggestion.getTooltip() != null && suggestion.getTooltip().getString() != null)
                {
                    writeString( suggestion.getTooltip().getString(), buf );
                }
            }
        }

        if(protocolVersion.isLegacy()) {
        	writeLegacyString(commands.get(0), buf);
        }
        else if ( protocolVersion.olderThan(Protocol.MC_1_13_0 ))
        {
            writeStringArray( commands, buf );
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception
    {
        handler.handle( this );
    }
}
