package net.md_5.bungee.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NetworkState {
	HANDSHAKE(-1),
	GAME(0),
	STATUS(1),
	LOGIN(2),
	LEGACY(3);
	
	@Getter
	int id;
	
	public static NetworkState byID(int id) {
		return values()[id + 1];
	}
}
