package net.md_5.bungee.protocol;

public class OverflowPacketException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public OverflowPacketException(String message) {
		super(message);
	}
}
