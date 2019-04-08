package net.md_5.bungee.protocol.exp;

public interface Protocol {
	public int getVersion();
	public Generation getGeneration();
	
	public static enum Generation {
		PRE_NETTY,
		POST_NETTY
	}
}
