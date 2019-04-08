package net.md_5.bungee.protocol;

import static net.md_5.bungee.protocol.DefinedPacket.skipLegacyItemStack;
import static net.md_5.bungee.protocol.DefinedPacket.skipLegacyString;
import static net.md_5.bungee.protocol.DefinedPacket.skipLegacyTag;
import static net.md_5.bungee.protocol.DefinedPacket.skipLegacyWatchableObjects;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import net.md_5.bungee.protocol.packet.*;

public enum Protocol {
	MC_1_3(0, ProtocolGen.PRE_NETTY, "1.3") { void postInit() { // For pinging only
		packet(NetworkState.LEGACY, Direction.TO_SERVER, 254, LegacyStatusRequest.class);
		packet(NetworkState.LEGACY, Direction.TO_CLIENT, 255, Kick.class);
	}},
	MC_1_5_2(61, ProtocolGen.PRE_NETTY, "1.5.2") { void postInit() {
		forStatus(NetworkState.LEGACY, new Do() { void apply() {
			both(0, KeepAlive.class);
			both(1, Login.class);
			serverbound(2, LegacyLoginRequest.class);
			both(3, Chat.class);
			this.<SkipPacket>clientbound(4, buf -> buf.skipBytes(Long.BYTES*2));
			this.<SkipPacket>clientbound(5, buf -> {
				buf.skipBytes(Integer.BYTES + Short.BYTES);
				skipLegacyItemStack(buf);
			});
			this.<SkipPacket>clientbound(6, buf -> buf.skipBytes(Integer.BYTES*3));
			this.<SkipPacket>serverbound(7, buf -> buf.skipBytes(Integer.BYTES*2 + 1));
			this.<SkipPacket>clientbound(8, buf -> buf.skipBytes(Float.BYTES + Short.BYTES*2));
			both(9, Respawn.class);
			this.<SkipPacket>both(10, buf-> buf.skipBytes(1));
			this.<SkipPacket>both(11, buf -> buf.skipBytes(Double.BYTES*4 + 1));
			this.<SkipPacket>both(12, buf -> buf.skipBytes(Float.BYTES*2 + 1));
			this.<SkipPacket>both(13, buf -> buf.skipBytes(Double.BYTES*4 + Float.BYTES*2 + 1));
			this.<SkipPacket>serverbound(14, buf -> buf.skipBytes(3 + Integer.BYTES*2));
			this.<SkipPacket>serverbound(15, buf -> {
				buf.skipBytes(Integer.BYTES*2 + 2);
				skipLegacyItemStack(buf);
				buf.skipBytes(3);
			});
			this.<SkipPacket>both(16, buf -> buf.skipBytes(Short.BYTES));
			this.<SkipPacket>clientbound(17, buf -> buf.skipBytes(Integer.BYTES*3 + 2));
			this.<SkipPacket>both(18, buf -> buf.skipBytes(Integer.BYTES + 1));
			this.<SkipPacket>serverbound(19, buf -> buf.skipBytes(Integer.BYTES + 1));
			this.<SkipPacket>clientbound(20, buf -> {
				buf.skipBytes(Integer.BYTES);
				skipLegacyString(buf, 16);
				buf.skipBytes(Integer.BYTES*3 + 2 + Short.BYTES);
				skipLegacyWatchableObjects(buf);
			});
			this.<SkipPacket>clientbound(22, buf -> buf.skipBytes(Integer.BYTES*2));
			this.<SkipPacket>clientbound(23, buf -> {
				buf.skipBytes(Integer.BYTES*4 + 3);
				int i = buf.readInt();
				if(i > 0)
					buf.skipBytes(Short.BYTES*3);
			});
			this.<SkipPacket>clientbound(24, buf -> {
				buf.skipBytes(Integer.BYTES*4 + 4 + Short.BYTES*3);
				skipLegacyWatchableObjects(buf);
			});
			this.<SkipPacket>clientbound(25, buf -> {
				buf.skipBytes(Integer.BYTES);
				skipLegacyString(buf, 13);
				buf.skipBytes(Integer.BYTES*4);
			});
			this.<SkipPacket>clientbound(26, buf -> buf.skipBytes(Integer.BYTES*4 + Short.BYTES));
			this.<SkipPacket>clientbound(28, buf -> buf.skipBytes(Integer.BYTES + Short.BYTES*3));
			this.<SkipPacket>clientbound(29, buf -> buf.skipBytes(buf.readByte() * Integer.BYTES));
			this.<SkipPacket>clientbound(30, buf -> buf.skipBytes(Integer.BYTES));
			this.<SkipPacket>clientbound(31, buf -> buf.skipBytes(Integer.BYTES + 3));
			this.<SkipPacket>clientbound(32, buf -> buf.skipBytes(Integer.BYTES + 2));
			this.<SkipPacket>clientbound(33, buf -> buf.skipBytes(Integer.BYTES + 5));
			this.<SkipPacket>clientbound(34, buf -> buf.skipBytes(Integer.BYTES*4 + 2));
			this.<SkipPacket>clientbound(35, buf -> buf.skipBytes(Integer.BYTES + 1));
			this.<SkipPacket>clientbound(38, buf -> buf.skipBytes(Integer.BYTES + 1));
			this.<SkipPacket>clientbound(39, buf -> buf.skipBytes(Integer.BYTES*2));
			this.<SkipPacket>clientbound(40, buf -> {
				buf.skipBytes(Integer.BYTES);
				skipLegacyWatchableObjects(buf);
			});
			this.<SkipPacket>clientbound(41, buf -> buf.skipBytes(Integer.BYTES + 2 + Short.BYTES));
			this.<SkipPacket>clientbound(42, buf -> buf.skipBytes(Integer.BYTES + 1));
			this.<SkipPacket>clientbound(43, buf -> buf.skipBytes(Float.BYTES + Short.BYTES*2));
			this.<SkipPacket>clientbound(51, buf -> {
				buf.skipBytes(Integer.BYTES*2 + 1 + Short.BYTES*2);
				buf.skipBytes(buf.readInt());
			});
			this.<SkipPacket>clientbound(52, buf -> {
				buf.skipBytes(Integer.BYTES*2 + Short.BYTES);
				buf.skipBytes(buf.readInt());
			});
			this.<SkipPacket>clientbound(53, buf -> buf.skipBytes(Integer.BYTES*2 + 2 + Short.BYTES));
			this.<SkipPacket>clientbound(54, buf -> buf.skipBytes(Integer.BYTES*2 + Short.BYTES*2 + 2));
			this.<SkipPacket>clientbound(55, buf -> buf.skipBytes(Integer.BYTES*4 + 1));
			this.<SkipPacket>clientbound(56, buf -> {
				int count = buf.readShort();
				int data = buf.readInt();
				buf.skipBytes(1);
				buf.skipBytes(data);
				buf.skipBytes(count * (Integer.BYTES*2 + Short.BYTES*2));
			});
			this.<SkipPacket>clientbound(60, buf -> {
				buf.skipBytes(Double.BYTES*3 + Float.BYTES);
				buf.skipBytes(3*buf.readInt());
				buf.skipBytes(Float.BYTES*3);
			});
			this.<SkipPacket>clientbound(61, buf -> buf.skipBytes(Integer.BYTES*4 + 2));
			this.<SkipPacket>clientbound(62, buf -> {
				skipLegacyString(buf, 256);
				buf.skipBytes(Integer.BYTES*3 + Float.BYTES + 1);
			});
			this.<SkipPacket>clientbound(63, buf -> {
				skipLegacyString(buf, 64);
				buf.skipBytes(Float.BYTES*7 + Integer.BYTES);
			});
			this.<SkipPacket>clientbound(70, buf -> buf.skipBytes(2));
			this.<SkipPacket>clientbound(71, buf -> buf.skipBytes(Integer.BYTES*4 + 1));
			this.<SkipPacket>clientbound(100, buf -> {
				buf.skipBytes(2);
				skipLegacyString(buf, 32);
				buf.skipBytes(2);
			});
			this.<SkipPacket>both(101, buf -> buf.skipBytes(1));
			this.<SkipPacket>serverbound(102, buf -> {
				buf.skipBytes(3 + Short.BYTES*2);
				skipLegacyItemStack(buf);
			});
			this.<SkipPacket>clientbound(103, buf -> {
				buf.skipBytes(Short.BYTES + 1);
				skipLegacyItemStack(buf);
			});
			this.<SkipPacket>clientbound(104, buf -> {
				buf.skipBytes(1);
				short count = buf.readShort();
				while(count-- != 0)
					skipLegacyItemStack(buf);
			});
			this.<SkipPacket>clientbound(105, buf -> buf.skipBytes(Short.BYTES*2 + 1));
			this.<SkipPacket>both(106, buf -> buf.skipBytes(2 + Short.BYTES));
			this.<SkipPacket>both(107, buf -> {
				buf.skipBytes(Short.BYTES);
				skipLegacyItemStack(buf);
			});
			this.<SkipPacket>serverbound(108, buf -> buf.skipBytes(2));
			this.<SkipPacket>both(130, buf -> {
				buf.skipBytes(Integer.BYTES*2 + Short.BYTES);
				for(int i = 0; i < 4; i++) skipLegacyString(buf, 15);
			});
			this.<SkipPacket>both(131, buf -> {
				buf.skipBytes(Short.BYTES*2);
				buf.skipBytes(buf.readUnsignedShort());
			});
			this.<SkipPacket>clientbound(132, buf -> {
				buf.skipBytes(Integer.BYTES*2 + Short.BYTES + 1);
				skipLegacyTag(buf);
			});
			this.<SkipPacket>clientbound(200, buf -> buf.skipBytes(Integer.BYTES + 1));
			clientbound(201, PlayerListItem.class);
			this.<SkipPacket>both(202, buf -> buf.skipBytes(3));
			serverbound(203, TabCompleteRequest.class);
			clientbound(203, TabCompleteResponse.class);
			serverbound(204, ClientSettings.class);
			serverbound(205, LegacyClientCommand.class);
			clientbound(206, ScoreboardObjective.class);
			clientbound(207, ScoreboardScore.class);
			clientbound(208, ScoreboardDisplay.class);
			clientbound(209, Team.class);
			both(250, PluginMessage.class);
			both(252, EncryptionResponse.class);
			clientbound(253, EncryptionRequest.class);
			serverbound(254, LegacyStatusRequest.class);
			both(255, Kick.class);
		}});
	}},
	MC_1_6_4(78, ProtocolGen.PRE_NETTY, "1.6.4") { void postInit() {
		inherit(MC_1_5_2);
		
		forStatus(NetworkState.LEGACY, new Do() {void apply() {
			this.<SkipPacket>replace(Direction.TO_CLIENT, 8, buf -> buf.skipBytes(Float.BYTES*2 + Short.BYTES));
			this.<SkipPacket>replace(Direction.TO_SERVER, 19, buf -> buf.skipBytes(Integer.BYTES*2 + 1));
			this.<SkipPacket>serverbound(27, buf -> buf.skipBytes(Float.BYTES*2 + 2));
			this.<SkipPacket>replace(Direction.TO_CLIENT, 39, buf -> buf.skipBytes(Integer.BYTES*2 + 1));
			this.<SkipPacket>clientbound(44, buf -> {
				buf.skipBytes(Integer.BYTES);
				int count = buf.readInt();
				while(count-- != 0) {
					skipLegacyString(buf, 64);
					buf.skipBytes(Double.BYTES);
					buf.skipBytes(buf.readShort()*(Long.BYTES*2+Double.BYTES+1));
				}
			});
			this.<SkipPacket>replace(Direction.TO_CLIENT, 100, buf -> {
				buf.skipBytes(1);
				int v = buf.readByte();
				skipLegacyString(buf, 32);
				buf.skipBytes(2);
				if(v == 11) buf.skipBytes(Integer.BYTES);
			});
			this.<SkipPacket>clientbound(133, buf -> buf.skipBytes(Integer.BYTES*3 + 1));
			this.<SkipPacket>replace(Direction.TO_CLIENT, 200, buf -> buf.skipBytes(Integer.BYTES*2));
			this.<SkipPacket>replace(202, buf -> buf.skipBytes(1 + Float.BYTES*2));
		}});
	}},
	MC_1_7_2(4, ProtocolGen.POST_NETTY, "1.7.2", "1.7.4", "1.7.5"){ void postInit(){
		forStatus(NetworkState.HANDSHAKE, new Do() { void apply() {
			serverbound(0x00, Handshake.class);
		};});
		forStatus(NetworkState.STATUS, new Do() { void apply() {
			serverbound(0x00, StatusRequest.class);
			clientbound(0x00, StatusResponse.class);
			both(0x01, PingPacket.class);
		};});
		forStatus(NetworkState.LOGIN, new Do() { void apply() {
			clientbound(0x00, Kick.class);
			serverbound(0x00, LoginRequest.class);
			
			clientbound(0x01, EncryptionRequest.class);
			serverbound(0x01, EncryptionResponse.class);
			
			clientbound(0x02, LoginSuccess.class);
			clientbound(0x03, SetCompression.class);
		};});
		forStatus(NetworkState.GAME, new Do() { void apply() {
			both(0x00, KeepAlive.class);
			clientbound(0x01, Login.class);
			serverbound(0x01, Chat.class);
			clientbound(0x02, Chat.class);
			clientbound(0x07, Respawn.class);
			serverbound(0x14, TabCompleteRequest.class);
			serverbound(0x15, ClientSettings.class);
			serverbound(0x17, PluginMessage.class);
			clientbound(0x1A, EntityStatus.class);
			clientbound(0x38, PlayerListItem.class);
			clientbound(0x3A, TabCompleteResponse.class);
			clientbound(0x3B, ScoreboardObjective.class);
			clientbound(0x3C, ScoreboardScore.class);
			clientbound(0x3D, ScoreboardDisplay.class);
			clientbound(0x3E, Team.class);
			clientbound(0x3F, PluginMessage.class);
			clientbound(0x40, Kick.class);
			clientbound(0x45, Title.class);
			clientbound(0x46, SetCompression.class);
			clientbound(0x47, PlayerListHeaderFooter.class);
		};});
	}},
	MC_1_7_6(5, ProtocolGen.POST_NETTY, "1.7.6", "1.7.7", "1.7.8", "1.7.9", "1.7.10") { void postInit() {
		inherit(MC_1_7_2);
	}},
	MC_1_8(47, ProtocolGen.POST_NETTY,
		"1.8", "1.8.1", "1.8.2", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9") { 
	void postInit() {
		inherit(MC_1_7_2);
	}},
	MC_1_9(107, ProtocolGen.POST_NETTY, "1.9") { void postInit() {
		inheritStatesFromProtocol(MC_1_8, NetworkState.HANDSHAKE, NetworkState.LOGIN, NetworkState.STATUS);
		
		forStatus(NetworkState.GAME, new Do() { void apply() {
			serverbound(0x01, TabCompleteRequest.class);
			serverbound(0x02, Chat.class);
			serverbound(0x04, ClientSettings.class);
			serverbound(0x09, PluginMessage.class);
			serverbound(0x0B, KeepAlive.class);
			clientbound(0x0C, BossBar.class);
			clientbound(0x0E, TabCompleteResponse.class);
			clientbound(0x0F, Chat.class);
			clientbound(0x18, PluginMessage.class);
			clientbound(0x1A, Kick.class);
			clientbound(0x1B, EntityStatus.class);
			clientbound(0x1F, KeepAlive.class);
			clientbound(0x23, Login.class);
			clientbound(0x2D, PlayerListItem.class);
			clientbound(0x33, Respawn.class);
			clientbound(0x38, ScoreboardDisplay.class);
			clientbound(0x3F, ScoreboardObjective.class);
			clientbound(0x41, Team.class);
			clientbound(0x42, ScoreboardScore.class);
			clientbound(0x45, Title.class);
			clientbound(0x46, SetCompression.class);
			clientbound(0x48, PlayerListHeaderFooter.class);
		};});
	}},
	MC_1_9_1(108, ProtocolGen.POST_NETTY, "1.9.1") { void postInit() {
		inherit(MC_1_9);
	}},
	MC_1_9_2(109, ProtocolGen.POST_NETTY, "1.9.2") { void postInit() {
		inherit(MC_1_9);
	}},
	MC_1_9_3(110, ProtocolGen.POST_NETTY, "1.9.3", "1.9.4") { void postInit() {
		inherit(MC_1_9);
		reassign(NetworkState.GAME, Direction.TO_CLIENT, PlayerListHeaderFooter.class, 0x47);
	}},
	MC_1_10(210, ProtocolGen.POST_NETTY, "1.10", "1.10.1", "1.10.2") { void postInit() {
		inherit(MC_1_9_3);
	}},
	MC_1_11(315, ProtocolGen.POST_NETTY, "1.11") { void postInit() {
		inherit(MC_1_10);
	}},
	MC_1_11_1(316, ProtocolGen.POST_NETTY, "1.11.1", "1.11.2") { void postInit() {
		inherit(MC_1_11);
	}},
	MC_1_12(335, ProtocolGen.POST_NETTY, "1.12") { void postInit() {
		inheritStatesFromProtocol(MC_1_11_1, NetworkState.HANDSHAKE, NetworkState.LOGIN, NetworkState.STATUS);
		
		forStatus(NetworkState.GAME, new Do() { void apply() {
			serverbound(0x02, TabCompleteRequest.class);
			serverbound(0x03, Chat.class);
			serverbound(0x05, ClientSettings.class);
			serverbound(0x0A, PluginMessage.class);
			serverbound(0x0C, KeepAlive.class);
			clientbound(0x0C, BossBar.class);
			clientbound(0x0E, TabCompleteResponse.class);
			clientbound(0x0F, Chat.class);
			clientbound(0x18, PluginMessage.class);
			clientbound(0x1A, Kick.class);
			clientbound(0x1B, EntityStatus.class);
			clientbound(0x1F, KeepAlive.class);
			clientbound(0x23, Login.class);
			clientbound(0x2D, PlayerListItem.class);
			clientbound(0x34, Respawn.class);
			clientbound(0x3A, ScoreboardDisplay.class);
			clientbound(0x41, ScoreboardObjective.class);
			clientbound(0x43, Team.class);
			clientbound(0x44, ScoreboardScore.class);
			clientbound(0x46, SetCompression.class);
			clientbound(0x47, Title.class);
			clientbound(0x49, PlayerListHeaderFooter.class);
		};});
	}},
	MC_1_12_1(338, ProtocolGen.POST_NETTY, "1.12.1") { void postInit() {
		inheritStatesFromProtocol(MC_1_11_1, NetworkState.HANDSHAKE, NetworkState.LOGIN, NetworkState.STATUS);
		
		forStatus(NetworkState.GAME, new Do() { void apply() {
			serverbound(0x01, TabCompleteRequest.class);
			serverbound(0x02, Chat.class);
			serverbound(0x04, ClientSettings.class);
			serverbound(0x09, PluginMessage.class);
			serverbound(0x0B, KeepAlive.class);
			clientbound(0x0C, BossBar.class);
			clientbound(0x0E, TabCompleteResponse.class);
			clientbound(0x0F, Chat.class);
			clientbound(0x18, PluginMessage.class);
			clientbound(0x1A, Kick.class);
			clientbound(0x1B, EntityStatus.class);
			clientbound(0x1F, KeepAlive.class);
			clientbound(0x23, Login.class);
			clientbound(0x2E, PlayerListItem.class);
			clientbound(0x35, Respawn.class);
			clientbound(0x3B, ScoreboardDisplay.class);
			clientbound(0x42, ScoreboardObjective.class);
			clientbound(0x44, Team.class);
			clientbound(0x45, ScoreboardScore.class);
			clientbound(0x46, SetCompression.class);
			clientbound(0x48, Title.class);
			clientbound(0x4A, PlayerListHeaderFooter.class);
		};});
	}},
	MC_1_12_2(340, ProtocolGen.POST_NETTY, "1.12.2") { void postInit() {
		inherit(MC_1_12_1);
	}},
	MC_1_13(393, ProtocolGen.POST_NETTY, "1.13") { void postInit() {
		inheritStatesFromProtocol(MC_1_11_1, NetworkState.HANDSHAKE, NetworkState.LOGIN, NetworkState.STATUS);
		packet(NetworkState.LOGIN, Direction.TO_CLIENT, 0x04, LoginPayloadRequest.class);
		packet(NetworkState.LOGIN, Direction.TO_SERVER, 0x02, LoginPayloadResponse.class);
		
		forStatus(NetworkState.GAME, new Do() { void apply() {
			serverbound(0x02, Chat.class);
			serverbound(0x04, ClientSettings.class);
			serverbound(0x05, TabCompleteRequest.class);
			serverbound(0x0A, PluginMessage.class);
			serverbound(0x0E, KeepAlive.class);
			clientbound(0x0E, Chat.class);
			clientbound(0x0C, BossBar.class);
			clientbound(0x10, TabCompleteResponse.class);
			clientbound(0x11, Commands.class);
			clientbound(0x19, PluginMessage.class);
			clientbound(0x1B, Kick.class);
			clientbound(0x1C, EntityStatus.class);
			clientbound(0x21, KeepAlive.class);
			clientbound(0x25, Login.class);
			clientbound(0x30, PlayerListItem.class);
			clientbound(0x38, Respawn.class);
			clientbound(0x3E, ScoreboardDisplay.class);
			clientbound(0x45, ScoreboardObjective.class);
			clientbound(0x46, SetCompression.class);
			clientbound(0x47, Team.class);
			clientbound(0x48, ScoreboardScore.class);
			clientbound(0x4B, Title.class);
			clientbound(0x4E, PlayerListHeaderFooter.class);
		};});
	}},
	MC_1_13_1(401, ProtocolGen.POST_NETTY, "1.13.1") { void postInit() {
		inherit(MC_1_13);
	}},
	MC_1_13_2(404, ProtocolGen.POST_NETTY, "1.13.2") { void postInit() {
		inherit(MC_1_13_1);
	}};
	
	private Protocol(int ver, ProtocolGen gen, String... versions) {
		this.version = ver;
		this.generation = gen;
		this.versions = Arrays.asList(versions);
		
		postInit();
	}
	
	static abstract class Do {
		NetworkState s;
		Protocol pv;
		
		abstract void apply();
		
		<P extends Packet> void packet(Direction d, int id, Supplier<P> c) {
			pv.packet(s, d, id, c.get().getClass());
		}
		
		<P extends Packet> void packet(Direction d, int id, P c) {
			pv.packet(s, d, id, c.getClass());
		}
		
		<P extends Packet> void packet(Direction d, int id, Class<P> c) {
			pv.packet(s, d, id, c);
		}
		
		<P extends Packet> void clientbound(int id, Class<P> c) {
			packet(Direction.TO_CLIENT, id, c);
		}
		
		<P extends Packet> void clientbound(int id, P c) {
			packet(Direction.TO_CLIENT, id, c);
		}
		
		<P extends Packet> void serverbound(int id, P c) {
			packet(Direction.TO_SERVER, id, c);
		}
		
		<P extends Packet> void serverbound(int id, Class<P> c) {
			packet(Direction.TO_SERVER, id, c);
		}
		
		<P extends Packet> void both(int id, P c) {
			clientbound(id, c);
			serverbound(id, c);
		}
		
		<P extends Packet> void both(int id, Class<P> c) {
			clientbound(id, c);
			serverbound(id, c);
		}
		
		<P extends Packet> void replace(Direction d, int id, Class<? extends Packet> p) {
			pv.replace(s, d, id, p);
		}
		
		<P extends Packet> void replace(Direction d, int id, P s) {
			replace(d, id, s.getClass());
		}
		
		<P extends Packet> void replace(int id, P p) {
			replace(Direction.TO_CLIENT, id, p);
			replace(Direction.TO_SERVER, id, p);
		}
	}
	
	void postInit() {}
	
	void packet(NetworkState networkState, Direction direction, int id, Class<? extends Packet> clazz) {
		if(packets.put(networkState, direction, id, clazz) != null) throw new RuntimeException("Overwriting existing packet");
	}
	
	void reassign(NetworkState networkState, Direction direction, Class<? extends Packet> clazz, int id) {
		packets.remove(networkState, direction, clazz);
		packet(networkState, direction, id, clazz);
	}
	
	void replace(NetworkState networkState, Direction direction, int id, Class<? extends Packet> clazz) {
		packets.remove(networkState, direction, id);
		packet(networkState, direction, id, clazz);
	}
	
	void inherit(Protocol v) {
		packets.addAll(v.packets);
	}
	
	void inheritState(Protocol v, NetworkState ns) {
		packets.addAll(v.packets, ns);
	}
	
	void inheritStatesFromProtocol(Protocol v, NetworkState... css) {
		for(NetworkState cs : css)
			inheritState(v, cs);
	}
	
	void forStatus(NetworkState cs, Do p) {
		p.pv = this;
		p.s = cs;
		p.apply();
	}
	
	public TIntObjectMap<Class<? extends Packet>> getIdToClassUnmodifiableMap(NetworkState ns, Direction dir) {
		return packets.getIdToClassUnmodifiableMap(ns, dir);
	}
	
	public TObjectIntMap<Class<? extends Packet>> getClassToIdUnmodifiableMap(NetworkState ns, Direction dir) {
		return packets.getClassToIdUnmodifiableMap(ns, dir);
	}
	
	private final Packets packets = new Packets();
	public final int version;
	public final ProtocolGen generation;
	public final List<String> versions;
	
	@Override
	public String toString() {
		return "version: " + version + ", generation: " + generation.name();
	}
	
	public boolean newerThan(Protocol ver) {return ordinal() > ver.ordinal();}
	public boolean newerOrEqual(Protocol ver) {return ordinal() >= ver.ordinal();}
	public boolean olderThan(Protocol ver) {return ordinal() < ver.ordinal();}
	public boolean olderOrEqual(Protocol ver) {return ordinal() <= ver.ordinal();}
	
	public boolean isLegacy() { return generation == ProtocolGen.PRE_NETTY; }
	public boolean isModern() { return generation == ProtocolGen.POST_NETTY; }
	
	public static Protocol byNumber(int num, ProtocolGen gen) {
		for(Protocol v : VALUES)
			if(v.version == num && gen == v.generation)
				return v;
		return null;
	}
	
	public static final Protocol[] VALUES = values();
}
