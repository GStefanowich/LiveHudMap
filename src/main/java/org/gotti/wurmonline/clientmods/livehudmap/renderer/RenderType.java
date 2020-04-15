package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.World;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.assets.LiveMapConfig;

public enum RenderType {
	FLAT {
		@Override
		public MapRenderer createMapRenderer( World world ) {
			return new MapRendererFlat(world.getCellRenderer(), world.getNearTerrainBuffer());
		}
	},
	ISOMETRIC {
		@Override
		public MapRenderer createMapRenderer( World world ) {
			return new MapRendererIsometric(world.getCellRenderer(), world.getNearTerrainBuffer());
		}
	},
	TOPOGRAPHIC {
		@Override
		public MapRenderer createMapRenderer( World world ) {
			return new MapRendererTopographic(world.getCellRenderer(), world.getNearTerrainBuffer());
		}
	},
	CAVE {
		@Override
		public MapRenderer createMapRenderer(World world) {
			return new MapRendererCave(world.getCellRenderer(), world.getCaveBuffer());
		}
		
		@Override
		public int getMapSize() {
			if (LiveMapConfig.HIGH_RES_MAP) return 64;
			return 32;
		}
	};
	
	public abstract MapRenderer createMapRenderer(World world);
	
	public int getMapSize() {
		if (LiveMapConfig.HIGH_RES_MAP)
			return 256;
		return 128;
	}
}
