package net.md_5.bungee.protocol;

public class BadPacketException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BadPacketException(String message) {
		super(message);
	}

	public BadPacketException(String message, Throwable cause) {
		super(message, cause);
	}
}
