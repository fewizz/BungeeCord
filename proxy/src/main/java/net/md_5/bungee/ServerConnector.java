package net.md_5.bungee;

import com.google.common.base.Preconditions;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.util.QuietException;

public abstract class ServerConnector<UC extends UserConnection<?>> extends PacketHandler {
	protected final BungeeCord bungee = BungeeCord.getInstance();
	protected final UC user;
	//protected ServerConnection server;
	@Getter
	public final BungeeServerInfo target;
	public final Promise<ServerConnection> loginFuture;
	public final ChannelWrapper channel;
	
	public ServerConnector(ChannelWrapper channel, UC user, BungeeServerInfo info) {
		this.channel = channel;
		this.user = user;
		this.target = info;
		this.loginFuture = new DefaultPromise<>(channel.handle.eventLoop());
	}
	
	protected boolean ipForward() {
		return target
			.getIpForward() != null ?
			target.getIpForward()
			:
			bungee.config.isIpForward()
		;
	}
	
	protected boolean forgeSupport() {
		return getTarget()
			.getForgeSupport() != null ? 
			target.getForgeSupport() 
			:
			bungee.config.isForgeSupport()
		;
	}
	
	@Override
	public String toString() {
		return "["+user.getAddress()+"/"+user.getName()+"] [SC]";
	}
	
	@Override
	public void handle(Kick kick) throws Exception {
		loginFuture.setFailure(new Exception("Kicked =( "));
	}
	
	@Override
	public void handle(PacketWrapper packet) throws Exception {
		if (packet.packet == null)
			throw new QuietException("Unexpected packet received during server login process!\n" +
				"id, cs: " + channel
					.getConnectionState()
					.name()
			);
	}
	
	@Override
	public void exception(Throwable t) throws Exception {
		//t.printStackTrace();
		channel.handle.close();
		if(!loginFuture.isDone())
			loginFuture.setFailure(new Exception("Exception while connecting"));
	}
	
	protected void finish(ServerConnection server) {
		Preconditions.checkNotNull(server);
		loginFuture.setSuccess(server);
	}

}
