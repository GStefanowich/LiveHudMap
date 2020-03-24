package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.mesh.Tiles.Tile;

public abstract class AbstractSurfaceRenderer extends MapRenderer<NearTerrainDataBuffer> {
	public AbstractSurfaceRenderer(NearTerrainDataBuffer buffer) {
		super( buffer );
	}
	
	protected short getSurfaceHeight(int x, int y) {
		return (short) (this.getBuffer().getHeight(x, y) * 10);
	}
	
	@Override
	public void pick(PickData pickData, float xMouse, float yMouse, int width, int height, int playerX, int playerY) {
		// Offset cursor and window to get tile pos
		final int tileX = playerX + (int)(xMouse * width) - width / 2;
		final int tileY = playerY + (int)(yMouse * height) - height / 2;
		
		// Get the tile at the pos
		final Tile tile = this.getEffectiveTileType( tileX, tileY );
		
		// If not cave-wall
		if (tile == Tile.TILE_HOLE) {
			pickData.addText("Cave");
			
		} else if (tile == Tile.TILE_CLAY
			|| tile == Tile.TILE_TAR
			|| tile == Tile.TILE_SAND
			|| tile == Tile.TILE_PEAT
			|| tile == Tile.TILE_FIELD
			|| tile == Tile.TILE_FIELD2
			|| tile.isBush()
			|| tile.isTree()
			|| tile.isEnchanted() ) {
			pickData.addText(tile.getDesc());
			
		} else if (this.getSurfaceHeight( tileX, tileY ) < 0) {
			pickData.addText("Water");
			
		}
		
		// Do the default action
		super.pick( pickData, xMouse, yMouse, width, height, playerX, playerY );
	}
}
