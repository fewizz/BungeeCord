package net.md_5.bungee.util;

import gnu.trove.strategy.HashingStrategy;
import java.util.Locale;

class CaseInsensitiveHashingStrategy implements HashingStrategy<String> {
	private static final long serialVersionUID = 1L;

	static final CaseInsensitiveHashingStrategy INSTANCE = new CaseInsensitiveHashingStrategy();

	@Override
	public int computeHashCode(String str) {
		return str.toLowerCase(Locale.ROOT).hashCode();
	}

	@Override
	public boolean equals(String o1, String o2) {
		return o1.equals(o2) || (o1.toLowerCase(Locale.ROOT).equals(o2.toLowerCase(Locale.ROOT)));
	}
}
