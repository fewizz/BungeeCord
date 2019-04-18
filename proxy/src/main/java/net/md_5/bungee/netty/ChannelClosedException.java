package net.md_5.bungee.netty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ChannelClosedException extends RuntimeException {
	@Getter
	ChannelWrapper channel;
	
	private static final long serialVersionUID = 1L;

}
