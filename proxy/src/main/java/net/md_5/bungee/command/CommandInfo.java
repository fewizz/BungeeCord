package net.md_5.bungee.command;

import java.util.function.Function;

import io.netty.channel.Channel;
import io.netty.channel.socket.ServerSocketChannel;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandInfo extends Command {

	public CommandInfo() {
		super("info");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void execute(CommandSender sender, String[] args) {
		StringBuilder sb = new StringBuilder();
		sb.append("\nmemory: " +
			((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000)
			+ "/" +
			(Runtime.getRuntime().totalMemory() / 1000) + " (MB)\n");
		
		sb.append("threads:\n");
		for(Thread t : Thread.getAllStackTraces().keySet()) {
			sb.append("\t["+t.getName() +"] " + t.getState().name()+"\n");
		}
		
		sb.append("players:\n");
		BungeeCord.getInstance().getPlayers().forEach(p -> {
			UserConnection uc = (UserConnection) p;
			Channel ch = uc.getCh().getHandle();
			
			Function<Channel, String> s = ch_ -> {
				return "writeable: " + ch.isWritable()+
						(ch_.isWritable() ?
						", bytes before unwritable: " + ch_.bytesBeforeUnwritable() :
						", bytes before writable: " + ch_.bytesBeforeWritable());
			};
			
			sb.append("\t"+uc.getName()+":\n");
			sb.append("\t\t[client<->bungee] " + s.apply(uc.getCh().getHandle()) + "\n");
			ServerConnection server = uc.getServer();
			if(server != null)
				sb.append("\t\t[bungee<->server] " + s.apply(server.getCh().getHandle()) + "\n");
		});
		
		/*sb.append("listeners:\n");
		BungeeCord.getInstance().getListeners().forEach(ch -> {
			sb.append("\t"+ch.remoteAddress()+", active: "+ch.isActive()+"\n");
			
			if(ch instanceof ServerSocketChannel) {
				ServerSocketChannel ch0 = (ServerSocketChannel) ch;
			}
		});*/
		sender.sendMessage(sb.toString());
	}

}
