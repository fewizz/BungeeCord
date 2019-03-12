package net.md_5.bungee.entitymap;

import net.md_5.bungee.protocol.Direction;

public class EntityMap_1_6_4 extends EntityMap {
	static final EntityMap_1_6_4 INSTANCE = new EntityMap_1_6_4();
	
	private EntityMap_1_6_4() {
		addRewrite(0x1, Direction.TO_CLIENT);
	}
}
