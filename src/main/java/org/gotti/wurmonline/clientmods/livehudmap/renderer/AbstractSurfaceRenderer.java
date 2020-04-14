package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.renderer.cell.CellRenderer;
import com.wurmonline.client.renderer.structures.BridgePartData;
import com.wurmonline.client.renderer.structures.FenceData;
import com.wurmonline.client.renderer.structures.HouseFloorData;
import com.wurmonline.client.renderer.structures.HouseRoofData;
import com.wurmonline.client.renderer.structures.StructureData;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.MapLayer;
import org.gotti.wurmonline.clientmods.livehudmap.assets.AbstractTileData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TilePlayerData;
import org.gotti.wurmonline.clientmods.livehudmap.reflection.Structures;

import java.awt.Color;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractSurfaceRenderer extends MapRenderer<NearTerrainDataBuffer> {
	public AbstractSurfaceRenderer(CellRenderer renderer, NearTerrainDataBuffer buffer) {
		super(MapLayer.SURFACE, renderer, buffer );
	}
	
	protected short getSurfaceHeight(Coordinate position) {
		if (!LiveMap.isWithinPlayerView(position))
			return -1;
		return (short)(this.getBuffer().getHeight(position.getX(), position.getY()) * 10);
	}
	
	@Override
	protected final void abstractTooltip(LiveMap map, PickData tooltip, Coordinate tilePos, Coordinate player) {
		// Get the tile at the pos
		final Tile tile = this.getEffectiveTileType(tilePos);
		//final TileData tileData = MapLayers.SURFACE.getStructureLayer(tilePos);
		
		// If the tile is a mine entrance
		if (tile == Tile.TILE_HOLE || tile.isCaveDoor()) {
			tooltip.addText("Cave" + ( tile.isCaveDoor() ? " (Door)" : "" ));
			
		} else {
			if (tile == Tile.TILE_CLAY
				|| tile == Tile.TILE_TAR
				|| tile == Tile.TILE_SAND
				|| tile == Tile.TILE_PEAT
				|| tile == Tile.TILE_FIELD
				|| tile == Tile.TILE_FIELD2
				|| tile.isBush()
				|| tile.isTree()
				|| tile.isEnchanted() ) {
				tooltip.addText(tile.getDesc());
				
			}
			
			// If the surface is submerged under the water layer (At height 0), check for Bridges
			if (this.getSurfaceHeight( tilePos ) < 0) {
				Optional<BridgePartData> bridges = this.getBridgeAt( tilePos, this.getLayer());
				if (!bridges.isPresent())
					tooltip.addText("Water");
				else tooltip.addText(Structures.getBridge(bridges.get()).getHoverName());
			} // If the surface is not submerged, check for structures (Structures cannot be built at the water layer)
			else {
				Optional<StructureData> structures = this.getStructureAt(tilePos, this.getLayer());
				if (structures.isPresent()) {
					StructureData structure = structures.get();
					// Ignore structure if a Fence
					if (!(structure instanceof FenceData)) {
						if (structure instanceof HouseRoofData) // If structure at tile is a Roof
							tooltip.addText(Structures.getHouse((HouseRoofData) structure).getHoverName());
						else if (structure instanceof HouseFloorData) // If structure at tile is a Floor
							tooltip.addText(Structures.getHouse((HouseFloorData) structure).getHoverName());
						else if (structure instanceof BridgePartData) // If structure at tile is a Bridge
							tooltip.addText(Structures.getBridge((BridgePartData) structure).getHoverName());
						else tooltip.addText(structure.getHoverName());
					}
				}
			}
		}
	}
	
	@Override
	protected Color tileColor(LiveMap map, Tile tile, Coordinate pos) {
		return map.tileColor(tile, pos, TileColors::getColorFor );
	}
	
	@Override
	protected Tile getDefaultTile() {
		return Tile.TILE_GRASS;
	}
	
	protected final <ListA extends AbstractTileData, ListB extends AbstractTileData> Optional<AbstractTileData> getHigherTile(List<ListA> listA, List<ListB> listB) {
		// If there are no entities, use the structure color
		Optional<? extends AbstractTileData> result = Stream.of( listA, listB )
			.flatMap(Collection::stream)
			.max((Comparator<AbstractTileData>) (base, other) -> {
				if (base instanceof TilePlayerData)
					return 1;
				return Double.compare(base.getHeight(), other.getHeight());
			});
		return Optional.ofNullable(result.orElse(null));
	}
}
