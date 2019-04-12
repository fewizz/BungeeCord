package net.md_5.bungee.protocol;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.val;

public class Packets {
	
	private static final class Maps {
		final TObjectIntMap<Class<? extends Packet>> classToId = new TObjectIntHashMap<>();
		final TIntObjectMap<Constructor<? extends Packet>> idToFactory = new TIntObjectHashMap<>();
		
		public Class<? extends Packet> put(int id, Class<? extends Packet> clazz) {
			Constructor<? extends Packet> c;
			
			try {
				c = clazz.getDeclaredConstructor();
				c.setAccessible(true);
				classToId.put(clazz, id);
				val prevConstr = idToFactory.put(id, c);
				return prevConstr != null ? prevConstr.newInstance().getClass() : null;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public int idOf(Class<? extends Packet> clazz) {
			if(!classToId.containsKey(clazz))
				return -1;
			return classToId.get(clazz);
		}
		
		public void putAll(Maps m) {
			classToId.putAll(m.classToId);
			idToFactory.putAll(m.idToFactory);
		}
		
		public Class<? extends Packet> remove(int id) {
			try {
				Class<? extends Packet> cl = idToFactory.remove(id).newInstance().getClass();
				classToId.remove(cl);
				return cl;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public int remove(Class<? extends Packet> clazz) {
			int id = -1;
			if(!classToId.containsKey(clazz))
				throw new RuntimeException();
			id = classToId.get(clazz);
			idToFactory.remove(id);
			return id;
		}
	}
	
	private final Map<NetworkState, EnumMap<Direction, Maps>> map = new HashMap<>();
	
	public Class<? extends Packet> put(NetworkState networkState, Direction direction, int id, Class<? extends Packet> clazz) {
		return createIfAbsent(networkState, direction).put(id, clazz);
	}
	
	public int idOf(NetworkState ns, Direction dir, Class<? extends Packet> clazz) {
		Maps maps = get(ns, dir);
		int id = -1;
		if(maps != null) id = maps.idOf(clazz);
		
		if(id == -1)
			throw new RuntimeException(
				"There's no such packet for networkState: " + ns.name()
				+ ", direction: " + dir.name()
				+ ", class" + clazz );
		
		return id;
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
		createIfAbsent(ns, dir).putAll(c.get(ns, dir));
	}
	
	public void addAll(Packets c, NetworkState ns) {
		for(Direction dir : c.map.get(ns).keySet())
			addAll(c, ns, dir);
	}
	
	public void addAll(Packets c) {
		for(NetworkState ns : c.map.keySet())
			addAll(c, ns);
	}
	
	public Class<? extends Packet> remove(NetworkState ns, Direction d, int id) {
		return get(ns, d).remove(id);
	}
	
	public int remove(NetworkState ns, Direction d, Class<? extends Packet> clazz) {
		return get(ns, d).remove(clazz);
	}
	
	public void replace(NetworkState ns, Direction d, int oldId, int newId) {
		put(ns, d, newId, remove(ns, d, oldId));
	}
	
	public TIntObjectMap<Constructor<? extends Packet>> getIdToConstructorUnmodifiableMap(NetworkState ns, Direction dir) {
		Maps m = get(ns, dir);
		if(m == null || m.idToFactory == null) return null;
		return TCollections.unmodifiableMap(m.idToFactory);
	}
	
	public TObjectIntMap<Class<? extends Packet>> getClassToIdUnmodifiableMap(NetworkState ns, Direction dir) {
		Maps m = get(ns, dir);
		if(m == null || m.classToId == null) return null;
		return TCollections.unmodifiableMap((get(ns, dir).classToId));
	}
}
