package net.md_5.bungee.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.protocol.Protocol.Factory;

public class PacketMap {
	@Data @RequiredArgsConstructor
	private static class NSIDD { final NetworkState networkState; final int id; final Direction direction;}
	
	@Data @RequiredArgsConstructor
	private static class CD { final Class<? extends Packet> clazz; final Direction direction;}
	
	@Data
	@EqualsAndHashCode(callSuper=false)
	public class PacketInfo extends NSIDD {
		public PacketInfo(NetworkState ns, int id, Direction d, Factory f) {
			super(ns, id, d);
			this.factory = f;
			this.clazz = f.create().getClass();
		}
		
		final Factory factory;
		final Class<? extends Packet> clazz;
	}
	
	private final Map<NSIDD, PacketInfo> byNSIDD = new HashMap<>();
	private final Map<CD, PacketInfo> byCD = new HashMap<>();
	
	public void add(PacketInfo pi) {
		add(pi, pi.factory);
	}
	
	public void add(NSIDD nsidd, Factory factory) {
		add(nsidd.networkState, nsidd.id, nsidd.direction, factory);
	}
	
	public void add(NetworkState ns, int id, Direction dir, Factory factory) {
		PacketInfo pi = new PacketInfo(ns, id, dir, factory);
		Class<? extends Packet> clazz = factory.create().getClass();
		
		if(byNSIDD.containsKey(new NSIDD(ns, id, dir)))
			throw new RuntimeException("Packet with class " + clazz + " is already registered");
		byCD.put(new CD(clazz, dir), pi);
		byNSIDD.put(new NSIDD(ns, id, dir), pi);
	}
	
	public void addFrom(PacketMap pm, Predicate<PacketInfo> p) {
		pm.byNSIDD.forEach((k, v) -> {
			byNSIDD.put(k, v);
			byCD.put(new CD(v.clazz, v.direction), v);
		});
	}
	
	public PacketInfo getInfo(Class<? extends Packet> c, Direction dir) {
		return byCD.get(new CD(c, dir));
	}
	
	public PacketInfo getInfo(NetworkState ns, int id, Direction dir) {
		return byNSIDD.get(new NSIDD(ns, id, dir));
	}
	
	public PacketInfo remove(Class<? extends Packet> c, Direction dir) {
		PacketInfo i = getInfo(c, dir);
		
		byNSIDD.remove(i);
		byCD.remove(new CD(c, dir));
		return i;
	}
	
	public PacketInfo remove(NetworkState ns, int id, Direction dir) {
		PacketInfo i = getInfo(ns, id, dir);
		
		byNSIDD.remove(i);
		byCD.remove(new CD(i.getClazz(), dir));
		return i;
	}
	
	/*public void forEach(Consumer<PacketInfo> c) {
		byCD.forEach((cl, i) -> c.accept(i));
	}*/
}
