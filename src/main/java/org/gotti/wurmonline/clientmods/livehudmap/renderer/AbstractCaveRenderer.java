package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.renderer.cell.CellRenderer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.MapLayer;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Direction;

import java.awt.Color;

public abstract class AbstractCaveRenderer extends MapRenderer<CaveDataBuffer> {
	
	protected static final float MAP_HEIGHT = 1000;
	
	public AbstractCaveRenderer(CellRenderer renderer, CaveDataBuffer buffer) {
		super(MapLayer.CAVE, renderer, buffer );
	}
	
	protected boolean isTunnel( Coordinate pos ) {
		Tile tile = this.getTileType( pos );
		return this.isTunnel(tile);
	}
	protected boolean isTunnel( Tile tile ) {
		return tile == Tile.TILE_CAVE || tile == Tile.TILE_CAVE_EXIT || ((tile != null) && (tile.isReinforcedFloor() || tile.isRoad()));
	}
	protected boolean isSurroundedByRock( Coordinate pos ) {
		return !this.isTunnel(pos.offset( Direction.NORTH ))
			&& !this.isTunnel(pos.offset( Direction.EAST ))
			&& !this.isTunnel(pos.offset( Direction.SOUTH ))
			&& !this.isTunnel(pos.offset( Direction.WEST ));
	}
	
	@Override
	protected final void abstractTooltip(LiveMap map, PickData tooltip, Coordinate tilePos, Coordinate player) {
		// Get the tile at the pos
		final Tile tile = this.getEffectiveTileType( tilePos );
		
		// If tile has no loaded in yet
		if (tile == null) {
			tooltip.addText("Void");
			
		} else if (tile != Tile.TILE_CAVE_WALL && !isTunnel( tile )) {
			// If not cave-wall, describe (ores)
			tooltip.addText(tile.getName()
				.replace(" wall", "")
				.replace(" vein", ""));
			
			// Add tooltip for cave exits
		} else if (tile == Tile.TILE_CAVE_EXIT) {
			tooltip.addText(tile.getDesc());
			
		}
	}
	
	protected short getHeight(Coordinate pos) {
		int sum = this.getBuffer().getRawFloor( pos.getX(), pos.getY() ) +
			this.getBuffer().getRawFloor(pos.getX() + 1, pos.getY() ) +
			this.getBuffer().getRawFloor( pos.getX(), pos.getY() + 1 ) +
			this.getBuffer().getRawFloor(pos.getX() + 1, pos.getY() + 1);
		return (short) (sum / 4);
	}
	
	@Override
	protected Color tileColor(LiveMap map, Tile tile, Coordinate pos) {
		return map.tileColor(tile, pos, CaveColors::getColorFor );
	}
	
	@Override
	public final RenderType getRenderType() {
		return RenderType.CAVE;
	}
	
	@Override
	protected Tile getEffectiveTileType( Coordinate pos ) {
		Tile tile = super.getEffectiveTileType( pos );
		
		// If tile is an ore
		if ((!LiveMap.SHOW_ORES) && tile.isOreCave())
			return this.getDefaultTile();
		
		// If ore should be hidden if it is fully concealed
		if (!LiveHudMapMod.SHOW_HIDDEN_ORE && tile != Tile.TILE_CAVE_WALL && !this.isTunnel( tile ) && this.isSurroundedByRock( pos ))
			return this.getDefaultTile();
		
		return tile;
	}
	
	@Override
	protected Tile getDefaultTile() {
		return Tile.TILE_CAVE_WALL;
	}
}
