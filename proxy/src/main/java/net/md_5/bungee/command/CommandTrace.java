package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.netty.PipelineUtil;
import net.md_5.bungee.protocol.PacketDecoder;

public class CommandTrace extends Command {

	public CommandTrace() {
		super("trace");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String what = args[0];
		
		if(what.equals("packets")) {
			String userName = args[1];
			UserConnection uc = (UserConnection) BungeeCord.getInstance().getPlayer(userName);
			PacketDecoder dec = (PacketDecoder) uc.getCh().handle.pipeline().get(PipelineUtil.PACKET_DEC);
			dec.setTrace(!dec.isTrace());
			
			ServerConnection server = uc.getServer();
			if(server != null) {
				dec = (PacketDecoder) server.getCh().handle.pipeline().get(PipelineUtil.PACKET_DEC);
				dec.setTrace(!dec.isTrace());
			}
		}
	}

}
