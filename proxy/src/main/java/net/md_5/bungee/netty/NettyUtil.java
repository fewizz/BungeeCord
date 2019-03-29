package net.md_5.bungee.netty;

import java.util.concurrent.ThreadFactory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyUtil {
	public static final boolean USE_EPOLL =
    		Boolean.valueOf(System.getProperty( "bungee.epoll", "true" )) && Epoll.isAvailable();
	
	public static Class<? extends SocketChannel> bestSocketChannel() {
		return USE_EPOLL ? EpollSocketChannel.class : NioSocketChannel.class;
	}
	
	public static Class<? extends ServerSocketChannel> bestServerSocketChannel() {
		return USE_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
	}
	
	public static Class<? extends DatagramChannel> bestDatagramChannel() {
		return USE_EPOLL ? EpollDatagramChannel.class : NioDatagramChannel.class;
	}
	
	public static EventLoopGroup bestEventLoopGroup(int th, ThreadFactory f) {
		return USE_EPOLL ? new EpollEventLoopGroup(th, f) : new NioEventLoopGroup(th, f);
	}
}
