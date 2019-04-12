package net.md_5.bungee.protocol;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.val;

public class Packets {
	
	private static final class Maps {
		final TIntObjectMap<Class<? extends Packet>> idToClass = new TIntObjectHashMap<>();
		final TObjectIntMap<Class<? extends Packet>> classToId = new TObjectIntHashMap<>();
		
		public void addAll(Maps maps) {
			idToClass.putAll(maps.idToClass);
			classToId.putAll(maps.classToId);
		}
	}
	
	private final Map<NetworkState, EnumMap<Direction, Maps>> map = new HashMap<>();
	
	public Class<? extends Packet> put(NetworkState networkState, Direction direction, int id, Class<? extends Packet> clazz) {
		Maps maps = createIfAbsent(networkState, direction);
		
		maps.classToId.put(clazz, id);
		return maps.idToClass.put(id, clazz);
	}
	
	public int idOf(NetworkState ns, Direction dir, Class<? extends Packet> clazz) {
		Maps maps = get(ns, dir);
		if(maps == null || !maps.classToId.containsKey(clazz))
			throw new RuntimeException(
				"There's no such packet for networkState: " + ns.name()
				+ ", direction: " + dir.name()
				+ ", class" + clazz );
		
		return maps.classToId.get(clazz);
	}
	
	private Maps get(NetworkState ns, Direction d) { 
		val m0 = map.get(ns);
		if(m0 == null) return null;
		return m0.get(d);
	}
	
	private Maps createIfAbsent(NetworkState networkState, Direction d) {
		return computeIfAbsent(networkState, d, () -> new Maps());
	}
	
	private Maps computeIfAbsent(NetworkState networkState, Direction d, Supplier<Maps> s) {
		return map.computeIfAbsent(networkState, ns -> new EnumMap<>(Direction.class))
			.computeIfAbsent(d, dir -> s.get());
	}
	
	public void addAll(Packets c, NetworkState ns, Direction dir) {
		createIfAbsent(ns, dir).addAll(c.get(ns, dir));
	}
	
	public void addAll(Packets c, NetworkState ns) {
		for(Direction dir : c.map.get(ns).keySet())
			addAll(c, ns, dir);
	}
	
	public void addAll(Packets c) {
		for(NetworkState ns : c.map.keySet())
			addAll(c, ns);
	}
	
	public Class<? extends Packet> getClassStrictly(NetworkState ns, Direction dir, int id) {
		Maps m = get(ns, dir);
		if(m == null) throw new NoSuchElementException();
		return m.idToClass.get(id);
	}
	
	public int getIdStrictly(NetworkState ns, Direction dir, Class<? extends Packet> id) {
		Maps m = get(ns, dir);
		if(m == null) throw new NoSuchElementException();
		return m.classToId.get(id);
	}
	
	public Class<? extends Packet> remove(NetworkState ns, Direction d, int id) {
		Maps m = get(ns, d);
		val c = m.idToClass.remove(id);
		m.classToId.remove(c);
		return c;
	}
	
	public int remove(NetworkState ns, Direction d, Class<? extends Packet> clazz) {
		Maps m = get(ns, d);
		int id = m.classToId.remove(clazz);
		m.idToClass.remove(id);
		return id;
	}
	
	public void replace(NetworkState ns, Direction d, int oldId, int newId) {
		put(ns, d, newId, remove(ns, d, oldId));
	}
	
	public TIntObjectMap<Class<? extends Packet>> getIdToClassUnmodifiableMap(NetworkState ns, Direction dir) {
		Maps m = get(ns, dir);
		if(m == null || m.idToClass == null) return null;
		return TCollections.unmodifiableMap(m.idToClass);
	}
	
	public TObjectIntMap<Class<? extends Packet>> getClassToIdUnmodifiableMap(NetworkState ns, Direction dir) {
		Maps m = get(ns, dir);
		if(m == null || m.classToId == null) return null;
		return TCollections.unmodifiableMap((get(ns, dir).classToId));
	}
}
