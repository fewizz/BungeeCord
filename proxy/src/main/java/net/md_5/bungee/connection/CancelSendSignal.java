package net.md_5.bungee.connection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CancelSendSignal extends Error {
	private static final long serialVersionUID = 1L;
	public static final CancelSendSignal INSTANCE = new CancelSendSignal();

	@Override
	public Throwable initCause(Throwable cause) {
		return this;
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}
