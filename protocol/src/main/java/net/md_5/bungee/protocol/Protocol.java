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
import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.packet.*;

public enum Protocol {
	MC_1_5_2(61, ProtocolGen.PRE_NETTY, "1.5.2") { void postInit() {
		forStatus(NetworkState.LEGACY, new Do() { void apply() {
			packet(0, KeepAlive::new);
			packet(1, Login::new);
			serverboundPacket(2, LegacyLoginRequest::new);
			packet(3, Chat::new);
			clientboundSkip(4, buf -> buf.skipBytes(Long.BYTES*2));
			clientboundSkip(5, buf -> {
				buf.skipBytes(Integer.BYTES + Short.BYTES);
				skipLegacyItemStack(buf);
			});
			clientboundSkip(6, buf -> buf.skipBytes(Integer.BYTES*3));
			serverboundSkip(7, buf -> buf.skipBytes(Integer.BYTES*2 + 1));
			clientboundSkip(8, buf -> buf.skipBytes(Float.BYTES + Short.BYTES*2));
			packet(9, Respawn::new);
			skip(10, buf-> buf.skipBytes(1));
			skip(11, buf -> buf.skipBytes(Double.BYTES*4 + 1));
			skip(12, buf -> buf.skipBytes(Float.BYTES*2 + 1));
			skip(13, buf -> buf.skipBytes(Double.BYTES*4 + Float.BYTES*2 + 1));
			serverboundSkip(14, buf -> buf.skipBytes(3 + Integer.BYTES*2));
			serverboundSkip(15, buf -> {
				buf.skipBytes(Integer.BYTES*2 + 2);
				skipLegacyItemStack(buf);
				buf.skipBytes(3);
			});
			skip(16, buf -> buf.skipBytes(Short.BYTES));
			clientboundSkip(17, buf -> buf.skipBytes(Integer.BYTES*3 + 2));
			skip(18, buf -> buf.skipBytes(Integer.BYTES + 1));
			serverboundSkip(19, buf -> buf.skipBytes(Integer.BYTES + 1));
			clientboundSkip(20, buf -> {
				buf.skipBytes(Integer.BYTES);
				skipLegacyString(buf, 16);
				buf.skipBytes(Integer.BYTES*3 + 2 + Short.BYTES);
				skipLegacyWatchableObjects(buf);
			});
			clientboundSkip(22, buf -> buf.skipBytes(Integer.BYTES*2));
			clientboundSkip(23, buf -> {
				buf.skipBytes(Integer.BYTES*4 + 3);
				int i = buf.readInt();
				if(i > 0)
					buf.skipBytes(Short.BYTES*3);
			});
			clientboundSkip(24, buf -> {
				buf.skipBytes(Integer.BYTES*4 + 4 + Short.BYTES*3);
				skipLegacyWatchableObjects(buf);
			});
			clientboundSkip(25, buf -> {
				buf.skipBytes(Integer.BYTES);
				skipLegacyString(buf, 13);
				buf.skipBytes(Integer.BYTES*4);
			});
			clientboundSkip(26, buf -> buf.skipBytes(Integer.BYTES*4 + Short.BYTES));
			clientboundSkip(28, buf -> buf.skipBytes(Integer.BYTES + Short.BYTES*3));
			clientboundSkip(29, buf -> buf.skipBytes(buf.readByte() * Integer.BYTES));
			clientboundSkip(30, buf -> buf.skipBytes(Integer.BYTES));
			clientboundSkip(31, buf -> buf.skipBytes(Integer.BYTES + 3));
			clientboundSkip(32, buf -> buf.skipBytes(Integer.BYTES + 2));
			clientboundSkip(33, buf -> buf.skipBytes(Integer.BYTES + 5));
			clientboundSkip(34, buf -> buf.skipBytes(Integer.BYTES*4 + 2));
			clientboundSkip(35, buf -> buf.skipBytes(Integer.BYTES + 1));
			clientboundSkip(38, buf -> buf.skipBytes(Integer.BYTES + 1));
			clientboundSkip(39, buf -> buf.skipBytes(Integer.BYTES*2));
			clientboundSkip(40, buf -> {
				buf.skipBytes(Integer.BYTES);
				skipLegacyWatchableObjects(buf);
			});
			clientboundSkip(41, buf -> buf.skipBytes(Integer.BYTES + 2 + Short.BYTES));
			clientboundSkip(42, buf -> buf.skipBytes(Integer.BYTES + 1));
			clientboundSkip(43, buf -> buf.skipBytes(Float.BYTES + Short.BYTES*2));
			clientboundSkip(51, buf -> {
				buf.skipBytes(Integer.BYTES*2 + 1 + Short.BYTES*2);
				buf.skipBytes(buf.readInt());
			});
			clientboundSkip(52, buf -> {
				buf.skipBytes(Integer.BYTES*2 + Short.BYTES);
				buf.skipBytes(buf.readInt());
			});
			clientboundSkip(53, buf -> buf.skipBytes(Integer.BYTES*2 + 2 + Short.BYTES));
			clientboundSkip(54, buf -> buf.skipBytes(Integer.BYTES*2 + Short.BYTES*2 + 2));
			clientboundSkip(55, buf -> buf.skipBytes(Integer.BYTES*4 + 1));
			clientboundSkip(56, buf -> {
				int count = buf.readShort();
				int data = buf.readInt();
				buf.skipBytes(1);
				buf.skipBytes(data);
				buf.skipBytes(count * (Integer.BYTES*2 + Short.BYTES*2));
			});
			clientboundSkip(60, buf -> {
				buf.skipBytes(Double.BYTES*3 + Float.BYTES);
				buf.skipBytes(3*buf.readInt());
				buf.skipBytes(Float.BYTES*3);
			});
			clientboundSkip(61, buf -> buf.skipBytes(Integer.BYTES*4 + 2));
			clientboundSkip(62, buf -> {
				skipLegacyString(buf, 256);
				buf.skipBytes(Integer.BYTES*3 + Float.BYTES + 1);
			});
			clientboundSkip(63, buf -> {
				skipLegacyString(buf, 64);
				buf.skipBytes(Float.BYTES*7 + Integer.BYTES);
			});
			clientboundSkip(70, buf -> buf.skipBytes(2));
			clientboundSkip(71, buf -> buf.skipBytes(Integer.BYTES*4 + 1));
			clientboundSkip(100, buf -> {
				buf.skipBytes(2);
				skipLegacyString(buf, 32);
				buf.skipBytes(2);
			});
			skip(101, buf -> buf.skipBytes(1));
			serverboundSkip(102, buf -> {
				buf.skipBytes(3 + Short.BYTES*2);
				skipLegacyItemStack(buf);
			});
			clientboundSkip(103, buf -> {
				buf.skipBytes(Short.BYTES + 1);
				skipLegacyItemStack(buf);
			});
			clientboundSkip(104, buf -> {
				buf.skipBytes(1);
				short count = buf.readShort();
				while(count-- != 0)
					skipLegacyItemStack(buf);
			});
			clientboundSkip(105, buf -> buf.skipBytes(Short.BYTES*2 + 1));
			skip(106, buf -> buf.skipBytes(2 + Short.BYTES));
			skip(107, buf -> {
				buf.skipBytes(Short.BYTES);
				skipLegacyItemStack(buf);
			});
			serverboundSkip(108, buf -> buf.skipBytes(2));
			skip(130, buf -> {
				buf.skipBytes(Integer.BYTES*2 + Short.BYTES);
				for(int i = 0; i < 4; i++) skipLegacyString(buf, 15);
			});
			skip(131, buf -> {
				buf.skipBytes(Short.BYTES*2);
				buf.skipBytes(buf.readUnsignedShort());
			});
			clientboundSkip(132, buf -> {
				buf.skipBytes(Integer.BYTES*2 + Short.BYTES + 1);
				skipLegacyTag(buf);
			});
			clientboundSkip(200, buf -> buf.skipBytes(Integer.BYTES + 1));
			clientboundPacket(201, PlayerListItem::new);
			skip(202, buf -> buf.skipBytes(3));
			serverboundPacket(203, TabCompleteRequest::new);
			clientboundPacket(203, TabCompleteResponse::new);
			serverboundPacket(204, ClientSettings::new);
			serverboundPacket(205, LegacyClientCommand::new);
			clientboundPacket(206, ScoreboardObjective::new);
			clientboundPacket(207, ScoreboardScore::new);
			clientboundPacket(208, ScoreboardDisplay::new);
			clientboundPacket(209, Team::new);
			packet(250, PluginMessage::new);
			packet(252, EncryptionResponse::new);
			clientboundPacket(253, EncryptionRequest::new);
			serverboundPacket(254, LegacyStatusRequest::new);
			packet(255, Kick::new);
		}});
	}},
	MC_1_6_4(78, ProtocolGen.PRE_NETTY, "1.6.4") { void postInit() {
		inherit(MC_1_5_2);
		
		forStatus(NetworkState.LEGACY, new Do() {void apply() {
			replace(Direction.TO_CLIENT, 8, () -> new SkipPacket() {
				public void skip(ByteBuf buf) { buf.skipBytes(Float.BYTES*2 + Short.BYTES); }
			});
			replace(Direction.TO_SERVER, 19, () -> new SkipPacket() {
				public void skip(ByteBuf buf) { buf.skipBytes(Integer.BYTES*2 + 1); }
			});
			serverboundSkip(27, buf -> buf.skipBytes(Float.BYTES*2 + 2));
			replace(Direction.TO_CLIENT, 39, () -> new SkipPacket() { public void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + 1);
			}});
			clientboundSkip(44, buf -> {
				buf.skipBytes(Integer.BYTES);
				int count = buf.readInt();
				while(count-- != 0) {
					skipLegacyString(buf, 64);
					buf.skipBytes(Double.BYTES);
					buf.skipBytes(buf.readShort()*(Long.BYTES*2+Double.BYTES+1));
				}
			});
			replace(Direction.TO_CLIENT, 100, ()-> new SkipPacket() { public void skip(ByteBuf buf) {
				buf.skipBytes(1);
				int v = buf.readByte();
				skipLegacyString(buf, 32);
				buf.skipBytes(2);
				if(v == 11) buf.skipBytes(Integer.BYTES);
			}});
			clientboundSkip(133, buf -> buf.skipBytes(Integer.BYTES*3 + 1));
			replace(Direction.TO_CLIENT, 200, ()-> new SkipPacket() { public void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2);
			}});
			replace(202, ()-> new SkipPacket() { public void skip(ByteBuf buf) {
				buf.skipBytes(1 + Float.BYTES*2);
			};});
		}});
	}},
	MC_1_7_2(4, ProtocolGen.POST_NETTY, "1.7.2", "1.7.4", "1.7.5"){ void postInit(){
		forStatus(NetworkState.HANDSHAKE, new Do() { void apply() {
			serverboundPacket(0x00, Handshake::new);
		};});
		forStatus(NetworkState.STATUS, new Do() { void apply() {
			serverboundPacket(0x00, StatusRequest::new);
			clientboundPacket(0x00, StatusResponse::new);
			packet(0x01, PingPacket::new);
		};});
		forStatus(NetworkState.LOGIN, new Do() { void apply() {
			clientboundPacket(0x00, Kick::new);
			serverboundPacket(0x00, LoginRequest::new);
			
			clientboundPacket(0x01, EncryptionRequest::new);
			serverboundPacket(0x01, EncryptionResponse::new);
			
			clientboundPacket(0x02, LoginSuccess::new);
			clientboundPacket(0x03, SetCompression::new);
		};});
		forStatus(NetworkState.GAME, new Do() { void apply() {
			packet(0x00, KeepAlive::new);
			clientboundPacket(0x01, Login::new);
			serverboundPacket(0x01, Chat::new);
			clientboundPacket(0x02, Chat::new);
			clientboundPacket(0x07, Respawn::new);
			serverboundPacket(0x14, TabCompleteRequest::new);
			serverboundPacket(0x15, ClientSettings::new);
			serverboundPacket(0x17, PluginMessage::new);
			clientboundPacket(0x1A, EntityStatus::new);
			clientboundPacket(0x38, PlayerListItem::new);
			clientboundPacket(0x3A, TabCompleteResponse::new);
			clientboundPacket(0x3B, ScoreboardObjective::new);
			clientboundPacket(0x3C, ScoreboardScore::new);
			clientboundPacket(0x3D, ScoreboardDisplay::new);
			clientboundPacket(0x3E, Team::new);
			clientboundPacket(0x3F, PluginMessage::new);
			clientboundPacket(0x40, Kick::new);
			clientboundPacket(0x45, Title::new);
			clientboundPacket(0x46, SetCompression::new);
			clientboundPacket(0x47, PlayerListHeaderFooter::new);
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
			serverboundPacket(0x01, TabCompleteRequest::new);
			serverboundPacket(0x02, Chat::new);
			serverboundPacket(0x04, ClientSettings::new);
			serverboundPacket(0x09, PluginMessage::new);
			serverboundPacket(0x0B, KeepAlive::new);
			clientboundPacket(0x0C, BossBar::new);
			clientboundPacket(0x0E, TabCompleteResponse::new);
			clientboundPacket(0x0F, Chat::new);
			clientboundPacket(0x18, PluginMessage::new);
			clientboundPacket(0x1A, Kick::new);
			clientboundPacket(0x1B, EntityStatus::new);
			clientboundPacket(0x1F, KeepAlive::new);
			clientboundPacket(0x23, Login::new);
			clientboundPacket(0x2D, PlayerListItem::new);
			clientboundPacket(0x33, Respawn::new);
			clientboundPacket(0x38, ScoreboardDisplay::new);
			clientboundPacket(0x3F, ScoreboardObjective::new);
			clientboundPacket(0x41, Team::new);
			clientboundPacket(0x42, ScoreboardScore::new);
			clientboundPacket(0x45, Title::new);
			clientboundPacket(0x46, SetCompression::new);
			clientboundPacket(0x48, PlayerListHeaderFooter::new);
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
			serverboundPacket(0x02, TabCompleteRequest::new);
			serverboundPacket(0x03, Chat::new);
			serverboundPacket(0x05, ClientSettings::new);
			serverboundPacket(0x0A, PluginMessage::new);
			serverboundPacket(0x0C, KeepAlive::new);
			clientboundPacket(0x0C, BossBar::new);
			clientboundPacket(0x0E, TabCompleteResponse::new);
			clientboundPacket(0x0F, Chat::new);
			clientboundPacket(0x18, PluginMessage::new);
			clientboundPacket(0x1A, Kick::new);
			clientboundPacket(0x1B, EntityStatus::new);
			clientboundPacket(0x1F, KeepAlive::new);
			clientboundPacket(0x23, Login::new);
			clientboundPacket(0x2D, PlayerListItem::new);
			clientboundPacket(0x34, Respawn::new);
			clientboundPacket(0x3A, ScoreboardDisplay::new);
			clientboundPacket(0x41, ScoreboardObjective::new);
			clientboundPacket(0x43, Team::new);
			clientboundPacket(0x44, ScoreboardScore::new);
			clientboundPacket(0x46, SetCompression::new);
			clientboundPacket(0x47, Title::new);
			clientboundPacket(0x49, PlayerListHeaderFooter::new);
		};});
	}},
	MC_1_12_1(338, ProtocolGen.POST_NETTY, "1.12.1") { void postInit() {
		inheritStatesFromProtocol(MC_1_11_1, NetworkState.HANDSHAKE, NetworkState.LOGIN, NetworkState.STATUS);
		
		forStatus(NetworkState.GAME, new Do() { void apply() {
			serverboundPacket(0x01, TabCompleteRequest::new);
			serverboundPacket(0x02, Chat::new);
			serverboundPacket(0x04, ClientSettings::new);
			serverboundPacket(0x09, PluginMessage::new);
			serverboundPacket(0x0B, KeepAlive::new);
			clientboundPacket(0x0C, BossBar::new);
			clientboundPacket(0x0E, TabCompleteResponse::new);
			clientboundPacket(0x0F, Chat::new);
			clientboundPacket(0x18, PluginMessage::new);
			clientboundPacket(0x1A, Kick::new);
			clientboundPacket(0x1B, EntityStatus::new);
			clientboundPacket(0x1F, KeepAlive::new);
			clientboundPacket(0x23, Login::new);
			clientboundPacket(0x2E, PlayerListItem::new);
			clientboundPacket(0x35, Respawn::new);
			clientboundPacket(0x3B, ScoreboardDisplay::new);
			clientboundPacket(0x42, ScoreboardObjective::new);
			clientboundPacket(0x44, Team::new);
			clientboundPacket(0x45, ScoreboardScore::new);
			clientboundPacket(0x46, SetCompression::new);
			clientboundPacket(0x48, Title::new);
			clientboundPacket(0x4A, PlayerListHeaderFooter::new);
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
			serverboundPacket(0x02, Chat::new);
			serverboundPacket(0x04, ClientSettings::new);
			serverboundPacket(0x05, TabCompleteRequest::new);
			serverboundPacket(0x0A, PluginMessage::new);
			serverboundPacket(0x0E, KeepAlive::new);
			clientboundPacket(0x0E, Chat::new);
			clientboundPacket(0x0C, BossBar::new);
			clientboundPacket(0x10, TabCompleteResponse::new);
			clientboundPacket(0x11, Commands::new);
			clientboundPacket(0x19, PluginMessage::new);
			clientboundPacket(0x1B, Kick::new);
			clientboundPacket(0x1C, EntityStatus::new);
			clientboundPacket(0x21, KeepAlive::new);
			clientboundPacket(0x25, Login::new);
			clientboundPacket(0x30, PlayerListItem::new);
			clientboundPacket(0x38, Respawn::new);
			clientboundPacket(0x3E, ScoreboardDisplay::new);
			clientboundPacket(0x45, ScoreboardObjective::new);
			clientboundPacket(0x46, SetCompression::new);
			clientboundPacket(0x47, Team::new);
			clientboundPacket(0x48, ScoreboardScore::new);
			clientboundPacket(0x4B, Title::new);
			clientboundPacket(0x4E, PlayerListHeaderFooter::new);
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
		
		void packet(Direction d, int id, Supplier<Packet> c) {
			pv.packet(s, d, id, c.get().getClass());
		}
		
		void clientboundPacket(int id, Supplier<Packet> c) {
			packet(Direction.TO_CLIENT, id, c);
		}
		
		void serverboundPacket(int id, Supplier<Packet> c) {
			packet(Direction.TO_SERVER, id, c);
		}
		
		void packet(int id, Supplier<Packet> c) {
			clientboundPacket(id, c);
			serverboundPacket(id, c);
		}
		
		void clientboundSkip(int id, SkipPacket sp) {
			clientboundPacket(id, () -> sp);
		}
		
		void serverboundSkip(int id, SkipPacket sp) {
			serverboundPacket(id, () -> sp);
		}
		
		void skip(int id, SkipPacket sp) {
			clientboundSkip(id, sp);
			serverboundSkip(id, sp);
		}
		
		void replace(Direction d, int id, Class<? extends Packet> p) {
			pv.replace(s, d, id, p);
		}
		
		void replace(Direction d, int id, Supplier<Packet> s) {
			replace(d, id, s.get().getClass());
		}
		
		void replace(int id, Supplier<Packet> p) {
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
