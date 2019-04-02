package net.md_5.bungee.protocol;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import net.md_5.bungee.protocol.Protocol.Factory;

public class PacketMap {
	
	@Data
	@EqualsAndHashCode
	public class PacketInfo {
		final NetworkState networkState;
		final int id;
		final Direction direction;
		final Factory factory;
		final Class<? extends DefinedPacket> clazz;
	}
	
	private final Map<NetworkState, TIntObjectMap<EnumMap<Direction, PacketInfo>>> byNSIDD = new HashMap<>();
	private final Map<NetworkState, Map<Class<? extends DefinedPacket>, EnumMap<Direction, PacketInfo>>> byNSCD = new HashMap<>();
	
	public void add(NetworkState ns, int id, Direction dir, Factory factory) {
		add(new PacketInfo(ns, id, dir, factory, factory.create().getClass()));
	}
	
	public void add(PacketInfo pi) {
		if(getInfo(pi.networkState, pi.id, pi.direction) != null)
			throw new RuntimeException("Packet with class " + pi.clazz + " is already registered");
		
		byNSCD.computeIfAbsent(pi.networkState, (ns) -> new HashMap<>())
			.computeIfAbsent(pi.clazz, c -> new EnumMap<Direction, PacketInfo>(Direction.class))
			.put(pi.direction, pi);
		
		byNSIDD.computeIfAbsent(pi.networkState, ns -> new TIntObjectHashMap<>())
			.putIfAbsent(pi.id, new EnumMap<>(Direction.class));
		byNSIDD.get(pi.networkState).get(pi.id).put(pi.direction, pi);
	}
	
	public void addFrom(PacketMap pm, Predicate<PacketInfo> p) {
		pm.byNSIDD.forEach((ns, map0) -> {
			map0.forEachEntry((id, map1) -> {
				map1.forEach((dir, pi) -> {
					if(!p.test(pi))
						return;
					add(pi);
				});
				return true;
			});
		});
	}
	
	public PacketInfo getInfo(NetworkState ns, Class<? extends DefinedPacket> clazz, Direction dir) {
		val v0 = byNSCD.get(ns);
		if(v0 == null) return null;
		val v1 = v0.get(clazz);
		if(v1 == null) return null;
		return v1.get(dir);
	}
	
	public PacketInfo getInfo(NetworkState ns, int id, Direction dir) {
		val v0 = byNSIDD.get(ns);
		if(v0 == null) return null;
		val v1 = v0.get(id);
		if(v1 == null) return null;
		return v1.get(dir);
	}
	
	public PacketInfo remove(PacketInfo i) {
		byNSIDD.get(i.networkState).get(i.id).remove(i.direction);
		byNSCD.get(i.networkState).get(i.clazz).remove(i.direction);
		return i;
	}
	
	public PacketInfo remove(NetworkState ns, Class<? extends DefinedPacket> c, Direction dir) {
		return remove(getInfo(ns, c, dir));
	}
	
	public PacketInfo remove(NetworkState ns, int id, Direction dir) {
		return remove(getInfo(ns, id, dir));
	}
}
