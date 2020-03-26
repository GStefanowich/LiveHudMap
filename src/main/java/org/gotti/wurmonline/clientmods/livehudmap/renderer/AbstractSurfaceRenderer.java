package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.renderer.gui.MapLayers;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileStructureData;

import java.awt.Color;
import java.util.List;

public abstract class AbstractSurfaceRenderer extends MapRenderer<NearTerrainDataBuffer> {
	public AbstractSurfaceRenderer( RenderType type, NearTerrainDataBuffer buffer ) {
		super( type, buffer );
	}
	
	protected short getSurfaceHeight(Coordinate position) {
		return this.getSurfaceHeight(position.getX(), position.getY());
	}
	protected short getSurfaceHeight(int x, int y) {
		return (short) (this.getBuffer().getHeight(x, y) * 10);
	}
	
	@Override
	protected final void abstractTooltip(LiveMap map, PickData tooltip, Coordinate tilePos, Coordinate player) {
		// Get the tile at the pos
		final Tile tile = this.getEffectiveTileType(tilePos);
		final TileData tileData = MapLayers.SURFACE.getTile(tilePos);
		
		// If not cave-wall
		if (tile == Tile.TILE_HOLE || tile.isCaveDoor()) {
			tooltip.addText("Cave" + ( tile.isCaveDoor() ? " (Door)" : "" ));
			
		} else if (tile == Tile.TILE_CLAY
			|| tile == Tile.TILE_TAR
			|| tile == Tile.TILE_SAND
			|| tile == Tile.TILE_PEAT
			|| tile == Tile.TILE_FIELD
			|| tile == Tile.TILE_FIELD2
			|| tile.isBush()
			|| tile.isTree()
			|| tile.isEnchanted() ) {
			tooltip.addText(tile.getDesc());
			
		} else if (this.getSurfaceHeight( tilePos ) < 0) {
			List<TileStructureData> bridges = tileData.getBridges();
			if (bridges.isEmpty())
				tooltip.addText("Water");
			else {
				for (TileStructureData bridge : bridges)
					tooltip.addText(bridge.getName());
			}
		} else {
			List<TileStructureData> buildings = tileData.getBuildings();
			for (TileStructureData building : buildings)
				tooltip.addText(building.getName());
		}
	}
	
	@Override
	protected Color tileColor(LiveMap map, Tile tile, Coordinate pos) {
		return map.tileColor(tile, pos, TileColors::getColorFor );
	}
}
