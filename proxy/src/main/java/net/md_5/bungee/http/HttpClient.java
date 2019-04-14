package net.md_5.bungee.http;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.netty.NettyUtil;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpClient {
	public static final int TIMEOUT = 5000;
	private static final Cache<String, InetAddress> ADDRESS_CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

	public static void get(@NonNull String url, @NonNull EventLoop eventLoop, @NonNull final Callback<String> callback) {

		final URI uri = URI.create(url);

		Preconditions.checkNotNull(uri.getScheme(), "scheme");
		Preconditions.checkNotNull(uri.getHost(), "host");
		
		boolean ssl = uri.getScheme().equals("https");
		int port = uri.getPort();
		
		if (port == -1) {
			switch (uri.getScheme()) {
			case "http":
				port = 80;
				break;
			case "https":
				port = 443;
				break;
			default:
				throw new IllegalArgumentException("Unknown scheme " + uri.getScheme());
			}
		}

		InetAddress inetHost = ADDRESS_CACHE.getIfPresent(uri.getHost());
		if (inetHost == null) {
			try {
				inetHost = InetAddress.getByName(uri.getHost());
			} catch (UnknownHostException ex) {
				callback.done(null, ex);
				return;
			}
			ADDRESS_CACHE.put(uri.getHost(), inetHost);
		}

		ChannelFutureListener onConnected = future -> {
			if (future.isSuccess()) {
				String path = uri.getRawPath() + ((uri.getRawQuery() == null) ? "" : "?" + uri.getRawQuery());

				HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
				request.headers().set(HttpHeaderNames.HOST, uri.getHost());

				future.channel().writeAndFlush(request);
			} else {
				ADDRESS_CACHE.invalidate(uri.getHost());
				callback.done(null, future.cause());
			}			
		};

		new Bootstrap()
			.channel(NettyUtil.bestSocketChannel())
			.group(eventLoop)
			.handler(new HttpInitializer(callback, ssl, uri.getHost(), port))
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT)
			.remoteAddress(inetHost, port)
			.connect()
			.addListener(onConnected);
	}
}
