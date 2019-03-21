package net.md_5.bungee.protocol;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.PacketMap.PacketInfo;
import net.md_5.bungee.protocol.packet.*;

public enum Protocol {
	MC_1_6_4(78, ProtocolGen.PRE_NETTY) { void postInit() {
		forStatus(NetworkState.LEGACY, new Do() { void apply() {
			packet(0, KeepAlive::new);
			packet(1, Login::new);
			serverboundPacket(2, LegacyLoginRequest::new);
			packet(3, Chat::new);
			clientboundPacket(4, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Long.BYTES*2);
			};});
			clientboundPacket(5, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES + Short.BYTES);
				skipLegacyItemStack(buf);
			};});
			clientboundPacket(6, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*3);
			};});
			serverboundPacket(7, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + 1);
			};});
			clientboundPacket(8, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Float.BYTES*2 + Short.BYTES);
			};});
			packet(9, Respawn::new);
			packet(10, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(1);
			};});
			packet(11, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Double.BYTES*4 + 1);
			};});
			packet(12, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Float.BYTES*2 + 1);
			};});
			packet(13, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Double.BYTES*4 + Float.BYTES*2 + 1);
			};});
			serverboundPacket(14, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(3 + Integer.BYTES*2);
			};});
			serverboundPacket(15, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + 2);
				skipLegacyItemStack(buf);
				buf.skipBytes(3);
			};});
			packet(16, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Short.BYTES);
			};});
			clientboundPacket(17, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*3 + 2);
			};});
			packet(18, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES + 1);
			};});
			serverboundPacket(19, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + 1);
			};});
			clientboundPacket(20, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES);
				skipLegacyString(buf, 16);
				buf.skipBytes(Integer.BYTES*3 + 2 + Short.BYTES);
				skipLegacyWatchableObjects(buf);
			};});
			clientboundPacket(22, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2);
			};});
			clientboundPacket(23, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*4 + 3);
				int i = buf.readInt();
				if(i > 0)
					buf.skipBytes(Short.BYTES*3);
			};});
			clientboundPacket(24, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*4 + 4 + Short.BYTES*3);
				skipLegacyWatchableObjects(buf);
			};});
			clientboundPacket(25, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES);
				skipLegacyString(buf, 13);
				buf.skipBytes(Integer.BYTES*4);
			};});
			clientboundPacket(26, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*4 + Short.BYTES);
			};});
			serverboundPacket(27, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Float.BYTES*2 + 2);
			};});
			clientboundPacket(28, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES + Short.BYTES*3);
			};});
			clientboundPacket(29, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(buf.readByte() * Integer.BYTES);
			};});
			clientboundPacket(30, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES);
			};});
			clientboundPacket(31, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES + 3);
			};});
			clientboundPacket(32, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES + 2);
			};});
			clientboundPacket(33, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES + 5);
			};});
			clientboundPacket(34, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*4 + 2);
			};});
			clientboundPacket(35, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES + 1);
			};});
			clientboundPacket(38, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES + 1);
			};});
			clientboundPacket(39, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + 1);
			};});
			clientboundPacket(40, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES);
				skipLegacyWatchableObjects(buf);
			};});
			clientboundPacket(41, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES + 2 + Short.BYTES);
			};});
			clientboundPacket(42, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES + 1);
			};});
			clientboundPacket(43, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Float.BYTES + Short.BYTES*2);
			};});
			clientboundPacket(44, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES);
				int count = buf.readInt();
				while(count-- != 0) {
					skipLegacyString(buf, 64);
					buf.skipBytes(Double.BYTES);
					buf.skipBytes(buf.readShort()*(Long.BYTES*2+Double.BYTES+1));
				}
			};});
			clientboundPacket(51, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + 1 + Short.BYTES*2);
				buf.skipBytes(buf.readInt());
			};});
			clientboundPacket(52, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + Short.BYTES);
				buf.skipBytes(buf.readInt());
			};});
			clientboundPacket(53, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + 2 + Short.BYTES);
			};});
			clientboundPacket(54, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + Short.BYTES*2 + 2);
			};});
			clientboundPacket(55, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*4 + 1);
			};});
			clientboundPacket(56, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				int count = buf.readShort();
				int data = buf.readInt();
				buf.skipBytes(1);
				buf.skipBytes(data);
				buf.skipBytes(count * (Integer.BYTES*2 + Short.BYTES*2));
			};});
			clientboundPacket(60, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Double.BYTES*3 + Float.BYTES);
				buf.skipBytes(3*buf.readInt());
				buf.skipBytes(Float.BYTES*3);
			};});
			clientboundPacket(61, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*4 + 2);
			};});
			clientboundPacket(62, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				skipLegacyString(buf, 256);
				buf.skipBytes(Integer.BYTES*3 + Float.BYTES + 1);
			};});
			clientboundPacket(63, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				skipLegacyString(buf, 64);
				buf.skipBytes(Float.BYTES*7 + Integer.BYTES);
			};});
			clientboundPacket(70, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(2);
			};});
			clientboundPacket(71, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*4 + 1);
			};});
			clientboundPacket(100, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(1);
				int v = buf.readByte();
				skipLegacyString(buf, 32);
				buf.skipBytes(2);
				if(v == 1) buf.skipBytes(Integer.BYTES);
			};});
			packet(101, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(1);
			};});
			serverboundPacket(102, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(3 + Short.BYTES*2);
				skipLegacyItemStack(buf);
			};});
			clientboundPacket(103, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Short.BYTES + 1);
				skipLegacyItemStack(buf);
			};});
			clientboundPacket(104, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(1);
				short count = buf.readShort();
				while(count-- != 0)
					skipLegacyItemStack(buf);
			};});
			clientboundPacket(105, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Short.BYTES*2 + 1);
			};});
			packet(106, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(2 + Short.BYTES);
			};});
			packet(107, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Short.BYTES);
				skipLegacyItemStack(buf);
			};});
			serverboundPacket(108, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(2);
			};});
			packet(130, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + Short.BYTES);
				for(int i = 0; i < 4; i++) skipLegacyString(buf, 15);
			};});
			clientboundPacket(131, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Short.BYTES*3 + 1);
			};});
			clientboundPacket(132, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2 + Short.BYTES + 1);
				skipLegacyTag(buf);
			};});
			clientboundPacket(133, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*3 + 1);
			};});
			clientboundPacket(200, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(Integer.BYTES*2);
			};});
			clientboundPacket(201, PlayerListItem::new);
			packet(202, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				buf.skipBytes(1 + Float.BYTES*2);
			};});
			serverboundPacket(203, TabCompleteRequest::new);
			clientboundPacket(203, TabCompleteResponse::new);
			serverboundPacket(204, ()-> new SkipPacket() { void skip(ByteBuf buf) {
				skipLegacyString(buf, 7);
				buf.skipBytes(4);
			};});
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
	MC_1_7_2(4, ProtocolGen.POST_NETTY){ void postInit(){
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
	MC_1_7_6(5, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_7_2);
	}},
	MC_1_8_0(47, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_7_2);
	}},
	MC_1_9_0(107, ProtocolGen.POST_NETTY) { void postInit() {
		inheritStatusesFromProtocol(MC_1_8_0, NetworkState.HANDSHAKE, NetworkState.LOGIN, NetworkState.STATUS);
		
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
	MC_1_9_1(108, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_9_0);
	}},
	MC_1_9_2(109, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_9_0);
	}},
	MC_1_9_4(110, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_9_0);
		reassign(PlayerListHeaderFooter.class, Direction.TO_CLIENT, 0x47);
	}},
	MC_1_10_0(210, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_9_4);
	}},
	MC_1_11_0(315, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_10_0);
	}},
	MC_1_11_1(316, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_11_0);
	}},
	MC_1_12_0(335, ProtocolGen.POST_NETTY) { void postInit() {
		inheritStatusesFromProtocol(MC_1_11_1, NetworkState.HANDSHAKE, NetworkState.LOGIN, NetworkState.STATUS);
		
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
	MC_1_12_1(338, ProtocolGen.POST_NETTY) { void postInit() {
		inheritStatusesFromProtocol(MC_1_11_1, NetworkState.HANDSHAKE, NetworkState.LOGIN, NetworkState.STATUS);
		
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
	MC_1_12_2(340, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_12_1);
	}},
	MC_1_13_0(393, ProtocolGen.POST_NETTY) { void postInit() {
		inheritStatusesFromProtocol(MC_1_11_1, NetworkState.HANDSHAKE, NetworkState.LOGIN, NetworkState.STATUS);
		packet(NetworkState.LOGIN, 0x04, Direction.TO_CLIENT, LoginPayloadRequest::new);
		packet(NetworkState.LOGIN, 0x02, Direction.TO_SERVER, LoginPayloadResponse::new);
		
		forStatus(NetworkState.GAME, new Do() { void apply() {
			serverboundPacket(0x02, Chat::new);
			serverboundPacket(0x04, ClientSettings::new);
			serverboundPacket(0x05, TabCompleteRequest::new);
			serverboundPacket(0x0A, PluginMessage::new);
			serverboundPacket(0x0E, KeepAlive::new);
			clientboundPacket(0x0E, Chat::new);
			clientboundPacket(0x0C, BossBar::new);
			clientboundPacket(0x10, TabCompleteResponse::new);
			if ( Boolean.getBoolean( "net.md-5.bungee.protocol.register_commands" ) )
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
	MC_1_13_1(401, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_13_0);
	}},
	MC_1_13_2(404, ProtocolGen.POST_NETTY) { void postInit() {
		inherit(MC_1_13_1);
	}};
	
	private Protocol(int ver, ProtocolGen gen) {
		this.version = ver;
		this.generation = gen;
		packets = new PacketMap();
	}
	
	interface Factory {
		public Packet create();
	}
	
	static abstract class Do {
		NetworkState s;
		Protocol pv;
		
		abstract void apply();
		
		void packet(Direction d, int id, Factory c) {
			pv.packet(s, id, d, c);
		}
		
		void clientboundPacket(int id, Factory c) {
			pv.packet(s, id, Direction.TO_CLIENT, c);
		}
		
		void serverboundPacket(int id, Factory c) {
			pv.packet(s, id, Direction.TO_SERVER, c);
		}
		
		void packet(int id, Factory c) {
			clientboundPacket(id, c);
			serverboundPacket(id, c);
		}
	}
	
	void postInit() {}
	
	static {
		for(Protocol pv : values())
			pv.postInit();
	}
	
	void inherit(Protocol v) {
		packets.addFrom(v.packets, pi -> true);
	}
	
	void inheritStatus(NetworkState ns, Protocol v) {
		packets.addFrom(v.packets, pi -> pi.getNetworkState() == ns);
	}
	
	void inheritStatusesFromProtocol(Protocol v, NetworkState... css) {
		for(NetworkState cs : css)
			inheritStatus(cs, v);
	}
	
	void reassign(Class<? extends Packet> c, Direction dir, int newId) {
		PacketMap.PacketInfo pi = packets.remove(c, dir);
		packet(pi.networkState, newId, pi.direction, pi.factory);
	}
	
	void reassign(NetworkState ns, int oldID, Direction dir, int newId) {
		packet(ns, newId, dir, remove(ns, oldID, dir).getFactory());
	}
	
	PacketMap.PacketInfo remove(NetworkState ns, int id, Direction d) {
		return packets.remove(ns, id, d);
	}
	
	void forStatus(NetworkState cs, Do p) {
		p.pv = this;
		p.s = cs;
		p.apply();
	}
	
	void packet(NetworkState ns, int id, Direction direction, Factory factory) {
		packets.add(ns, id, direction, factory);
	}
	
	public Factory packetFactory(NetworkState ns, int id, Direction d) {
		PacketInfo pi = packets.getInfo(ns, id, d);
		return pi == null ? null : pi.getFactory();
	}
	
	public Packet createPacket(NetworkState cs, int id, Direction d) {
		Factory f = packetFactory(cs, id, d);
		return f == null ? null : f.create();
	}
	
	public int idOf(Packet p, Direction dir) {
		return packets.getInfo(p.getClass(), dir).getId();
	}
	
	private PacketMap packets;
	public final int version;
	public final ProtocolGen generation;
	
	public boolean newerThan(Protocol ver) {return ordinal() > ver.ordinal();}
	public boolean newerOrEqual(Protocol ver) {return ordinal() >= ver.ordinal();}
	public boolean olderThan(Protocol ver) {return ordinal() < ver.ordinal();}
	public boolean olderOrEqual(Protocol ver) {return ordinal() <= ver.ordinal();}
	
	public boolean isLegacy() { return generation == ProtocolGen.PRE_NETTY; }
	
	public static Protocol byNumber(int num, ProtocolGen gen) {
		for(Protocol v : values())
			if(v.version == num && gen == v.generation)
				return v;
		return null;
	}
	
	public static final List<String> GAME_VERSIONS = new ArrayList<>();
	
	static {
		for(Protocol v : values())
			GAME_VERSIONS.add(v.name().substring("MC_".length()).replace('_', '.'));
	}
}
