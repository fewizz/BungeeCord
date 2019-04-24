package net.md_5.bungee;

import com.google.common.base.Preconditions;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.Kick;

public abstract class ServerConnector<UC extends UserConnection<?>> extends PacketHandler {
	protected final BungeeCord bungee = BungeeCord.getInstance();
	protected final UC user;
	@Getter
	public final BungeeServerInfo target;
	public final Promise<Void> loginFuture;
	public final ChannelWrapper channel;
	
	public ServerConnector(ChannelWrapper channel, UC user, BungeeServerInfo info) {
		this.channel = channel;
		this.user = user;
		this.target = info;
		this.loginFuture = new DefaultPromise<>(channel.handle.eventLoop());
	}
	
	@Override
	public String toString() {
		return "["+user.getAddress()+"/"+user.getName()+"] [SC]";
	}
	
	@Override
	public void handle(Kick kick) throws Exception {
		channel.close();
		loginFuture.setFailure(new Exception("Kicked =( "));
	}
	
	@Override
	public void handle(PacketWrapper packet) throws Exception {
		Preconditions.checkState(packet.packet != null, "Unexpected packet received during server login process! Packet id: " + packet.id);
	}
	
	@Override
	public void exception(Throwable t) throws Exception {
		channel.close();
		if(!loginFuture.isDone())
			loginFuture.setFailure(new Exception("Exception while connecting"));
	}
	
	protected void finish(@NonNull ServerConnection server) {
		loginFuture.setSuccess(null);
	}

}
