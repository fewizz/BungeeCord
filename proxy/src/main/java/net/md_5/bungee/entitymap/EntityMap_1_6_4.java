package net.md_5.bungee.entitymap;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.Direction;
import net.md_5.bungee.protocol.ProtocolVersion;

public class EntityMap_1_6_4 extends EntityMap {
	static final EntityMap_1_6_4 INSTANCE = new EntityMap_1_6_4();
	
	private EntityMap_1_6_4() {
		addRewrite(1, Direction.TO_CLIENT); // Login
		addRewrite(5, Direction.TO_CLIENT); // Inventory
		addRewrite(7, Direction.TO_CLIENT); // Use
		addRewrite(17, Direction.TO_CLIENT); // Sleep
		addRewrite(18, Direction.TO_CLIENT); // Animation
		addRewrite(18, Direction.TO_SERVER);
		addRewrite(19, Direction.TO_SERVER); // Action
		addRewrite(20, Direction.TO_CLIENT); // Named spawn
		//22 Collect
		//23 Vehicle spawn
		addRewrite(24, Direction.TO_CLIENT); // MobSpawn
		addRewrite(25, Direction.TO_CLIENT); // Painting
		addRewrite(26, Direction.TO_CLIENT); // Exp
		addRewrite(28, Direction.TO_CLIENT); // Entity velocity
		//29 Destroy
		addRewrite(30, Direction.TO_CLIENT); // Entity
		addRewrite(31, Direction.TO_CLIENT); // EntityRelMove
		addRewrite(32, Direction.TO_CLIENT); // EntityLook
		addRewrite(33, Direction.TO_CLIENT); // RelEntityMoveLook
		addRewrite(34, Direction.TO_CLIENT); // Entity teleport
		addRewrite(34, Direction.TO_CLIENT); // Entity head rotation
		addRewrite(38, Direction.TO_CLIENT); // Entity status
		// 39 Attach entity
		addRewrite(40, Direction.TO_CLIENT); // Entity metadata
		addRewrite(41, Direction.TO_CLIENT); // Entity effect
		addRewrite(42, Direction.TO_CLIENT); // Remove entity effect
		addRewrite(44, Direction.TO_CLIENT); // Update attribs
	}
	
	@Override
	public void rewriteClientbound(ByteBuf packet, int oldId, int newId, ProtocolVersion pv) {
		super.rewriteServerbound(packet, oldId, newId, pv);
		int begin = packet.readerIndex();
		int packetID = packet.readUnsignedByte();
		
		if(packetID == 22) {
			rewriteInt(packet, oldId, newId, begin + 1 + Integer.BYTES);
		}
		if(packetID == 23) {
			rewriteInt(packet, oldId, newId, begin + 1 + Integer.BYTES*4 + 3);
		}
		if(packetID == 29) {
			int size = packet.readUnsignedByte();
			for(int i = 0; i < size; i++) {
				rewriteInt(packet, oldId, newId, begin + 2 + Integer.BYTES*i);
			}
		}
		if(packetID == 39) {
			rewriteInt(packet, oldId, newId, begin + 1);
			rewriteInt(packet, oldId, newId, begin + 1 + Integer.BYTES);
		}
		packet.readerIndex(begin);
	}
}
