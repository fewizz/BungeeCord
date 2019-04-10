package net.md_5.bungee.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.val;
import net.md_5.bungee.protocol.packet.LegacyStatusRequest;

public class LegacyStatusRequestHandler extends ChannelInboundHandlerAdapter {
	
	static class CantImagineNameInputStream extends InputStream {
		Semaphore sem = new Semaphore(0);
		CompositeByteBuf bytes = Unpooled.compositeBuffer();
		
		@Override
		public int read() throws IOException {
			try {
				if(!sem.tryAcquire(500, TimeUnit.MILLISECONDS)) {
					throw new InterruptedException();
				}
				synchronized (bytes) {
					return bytes.readUnsignedByte();
				}
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
		}
		
		public int readShort() throws IOException {
			int first = read();
			return (first << 8) | read();
		}
		
		public String readString() throws IOException {
			char[] str = new char[readShort()];
			for(int i = 0; i < str.length; i++) {
				str[i] = (char) readShort();
			}
			return str.toString();
		}
		
		public int readInt() throws IOException {
			int s1 = readShort();
			return (s1 << 16 | readShort());
		}
		
		public void put(ByteBuf buf) {
			int bs = buf.readableBytes();
			synchronized (bytes) {
				bytes.addComponent(true, buf);
			}
			sem.release(bs);
		}
	}
	
	final CantImagineNameInputStream is = new CantImagineNameInputStream();
	boolean firstTime = true;
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(firstTime) {
			firstTime = false;
			new Thread(() -> { 
				val lsr = new LegacyStatusRequest();
				try {
					is.skip(3);
					lsr.setBranding(is.readString());
					is.skip(2);
					lsr.setProtocolVersion(is.read());
					lsr.setHost(is.readString());
					lsr.setPort(is.readInt());
				} catch (IOException e) {
				}
				finally {
					ctx.channel().eventLoop().execute(() -> {
						if(!ctx.channel().isActive()) {
							return;
						}
						ctx.fireChannelRead(new PacketWrapper(lsr, is.bytes));
						ctx.pipeline().remove(ctx.handler());
					});
				}
			}, "Status Request IO, " + ctx.channel().remoteAddress()).start();
		}
		
		is.put(((ByteBuf)msg).copy());
	}

}
