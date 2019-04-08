package net.md_5.bungee.protocol.exp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum LegacyProtocol implements Protocol {
	_1_5_2(0, Generation.PRE_NETTY);
	
	@Getter
	final int version;
	@Getter
	Generation generation;
}
