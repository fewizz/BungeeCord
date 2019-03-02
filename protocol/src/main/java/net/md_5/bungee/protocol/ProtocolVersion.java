package net.md_5.bungee.protocol;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProtocolVersion {
	MC_1_6_4(78, ProtocolGen.PRE_NETTY),
	MC_1_7_2(4, ProtocolGen.MODERN),
	MC_1_7_6(5, ProtocolGen.MODERN),
	MC_1_8(47, ProtocolGen.MODERN),
	MC_1_9(107, ProtocolGen.MODERN),
	MC_1_9_1(108, ProtocolGen.MODERN),
	MC_1_9_2(19, ProtocolGen.MODERN),
	MC_1_9_4(110, ProtocolGen.MODERN),
	MC_1_10(210, ProtocolGen.MODERN),
	MC_1_11(315, ProtocolGen.MODERN),
	MC_1_11_1(316, ProtocolGen.MODERN),
	MC_1_12(335, ProtocolGen.MODERN),
	MC_1_12_1(338, ProtocolGen.MODERN),
	MC_1_12_2(340, ProtocolGen.MODERN),
	MC_1_13(393, ProtocolGen.MODERN),
	MC_1_13_1(401, ProtocolGen.MODERN),
	MC_1_13_2(404, ProtocolGen.MODERN);
	
	public final int version;
	public final ProtocolGen generation;
	
	public boolean newerThan(ProtocolVersion ver) {return ordinal() > ver.ordinal();}
	public boolean newerOrEqual(ProtocolVersion ver) {return ordinal() >= ver.ordinal();}
	public boolean olderThan(ProtocolVersion ver) {return ordinal() < ver.ordinal();}
	public boolean olderOrEqual(ProtocolVersion ver) {return ordinal() <= ver.ordinal();}
	
	public static ProtocolVersion getByNumber(int num, ProtocolGen gen) {
		for(ProtocolVersion v : values())
			if(v.version == num && gen == v.generation)
				return v;
		return null;
	}
	
	public static final List<String> GAME_VERSIONS = new ArrayList<>();
	
	static {
		for(ProtocolVersion v : values())
			GAME_VERSIONS.add(v.name().substring("MC_".length()).replace('_', '.'));
	}
}
