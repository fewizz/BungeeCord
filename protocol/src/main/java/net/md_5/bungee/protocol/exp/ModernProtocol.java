package net.md_5.bungee.protocol.exp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ModernProtocol implements Protocol {
	_1_13_2(0, Generation.POST_NETTY);
	
	@Getter
	final int version;
	@Getter
	Generation generation;
}
