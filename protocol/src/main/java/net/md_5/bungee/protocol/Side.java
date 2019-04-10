package net.md_5.bungee.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Side {
	CLIENT(Direction.TO_CLIENT, Direction.TO_SERVER),
	SERVER(Direction.TO_SERVER, Direction.TO_CLIENT);
	
	@Getter
	final Direction inboundDirection;
	@Getter
	final Direction outboundDirection;
}