package net.md_5.bungee.protocol;

public enum Direction {
    TO_CLIENT, TO_SERVER;
	
	public Direction opposite() { return this == TO_CLIENT ? TO_SERVER : TO_CLIENT; }
}