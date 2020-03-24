package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.mesh.Tiles.Tile;

public abstract class AbstractCaveRenderer extends MapRenderer<CaveDataBuffer> {
	
	protected static final float MAP_HEIGHT = 1000;
	
	public AbstractCaveRenderer(CaveDataBuffer buffer) {
		super( buffer );
	}
	
	protected boolean isTunnel( int x, int y ) {
		Tile tile = this.getTileType(x, y);
		return this.isTunnel(tile);
	}
	protected boolean isTunnel( Tile tile ) {
		return tile == Tile.TILE_CAVE || tile == Tile.TILE_CAVE_EXIT || ((tile != null) && (tile.isReinforcedFloor() || tile.isRoad()));
	}
	protected boolean isSurroundedByRock( int x, int y ) {
		return !isTunnel(x + 1, y) && !isTunnel(x - 1, y) && !isTunnel(x, y + 1) && !isTunnel(x, y - 1);
	}
	
	@Override
	public void pick(PickData pickData, float xMouse, float yMouse, int width, int height, int playerX, int playerY) {
		// Offset cursor and window to get tile pos
		final int tileX = playerX + (int)(xMouse * width) - width / 2;
		final int tileY = playerY + (int)(yMouse * height) - height / 2;
		
		// Get the tile at the pos
		final Tile tile = this.getEffectiveTileType( tileX, tileY );
		
		// If not cave-wall
		if (tile != Tile.TILE_CAVE_WALL && !isTunnel( tile )) {
			pickData.addText(tile.getName()
				.replace(" wall", "")
				.replace(" vein", ""));
			
		} else if (tile == Tile.TILE_CAVE_EXIT) {
			pickData.addText(tile.getDesc());
			
		}
		
		// Do the default action
		super.pick( pickData, xMouse, yMouse, width, height, playerX, playerY );
	}
	
	protected short getHeight(int x, int y) {
		int sum = this.getBuffer().getRawFloor( x, y ) +
			this.getBuffer().getRawFloor(x + 1, y ) +
			this.getBuffer().getRawFloor( x, y + 1 ) +
			this.getBuffer().getRawFloor(x + 1, y + 1);
		return (short) (sum / 4);
	}
}
