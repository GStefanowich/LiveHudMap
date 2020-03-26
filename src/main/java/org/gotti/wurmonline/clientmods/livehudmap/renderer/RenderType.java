package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.World;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;

public enum RenderType {
	FLAT {
		@Override
		public MapRenderer createMapRenderer( World world ) {
			return new MapRendererFlat(this, world.getNearTerrainBuffer());
		}
	},
	ISOMETRIC {
		@Override
		public MapRenderer createMapRenderer( World world ) {
			return new MapRendererIsometric(this, world.getNearTerrainBuffer());
		}
	},
	TOPOGRAPHIC {
		@Override
		public MapRenderer createMapRenderer( World world ) {
			return new MapRendererTopographic(this, world.getNearTerrainBuffer());
		}
	},
	CAVE {
		@Override
		public MapRenderer createMapRenderer(World world) {
			return new MapRendererCave(this, world.getCaveBuffer());
		}
		
		@Override
		public int getMapSize() {
			if (LiveHudMapMod.USE_HIGH_RES_MAP) return 96;
			return 32;
		}
	};
	
	public abstract MapRenderer createMapRenderer(World world);
	
	public int getMapSize() {
		if (LiveHudMapMod.USE_HIGH_RES_MAP)
			return 256;
		return 128;
	}

}
