package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class PacketWrapper
{

    public final Packet packet;
    public final ByteBuf buf;
    public final int id;
    @Setter
    private boolean released;

    public void trySingleRelease()
    {
        if ( !released )
        {
            ReferenceCountUtil.release(buf);
            released = true;
        }
    }
}
