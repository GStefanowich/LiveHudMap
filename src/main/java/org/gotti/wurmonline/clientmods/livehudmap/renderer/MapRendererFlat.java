package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.renderer.cell.CellRenderer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.AbstractTileData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileRenderLayer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MapRendererFlat extends AbstractSurfaceRenderer {
	public MapRendererFlat(CellRenderer renderer, NearTerrainDataBuffer buffer) {
		super( renderer, buffer );
	}
	
	@Override
	public BufferedImage createMapDump(LiveMap map, int leftX, int topY, int imageX, int imageY, Coordinate playerPos) {
		Set<MapTile> grids = new HashSet<>();
		
		// Image should always be a square that covers both lengths
		final int imageDimension = Math.max( imageX, imageY );
		
		// For X direction
		for (int xOffset = 0; xOffset < imageDimension; xOffset++) {
			// For Y direction
			for (int yOffset = imageDimension - 1; yOffset >= 0; yOffset--) {
				final Coordinate pos = Coordinate.of(xOffset + leftX, yOffset + topY);
				final MapTile grid = this.getMapTile(pos);
				
				if (grid != null) {
					grids.add( grid );
					
					// Update if the player is within view
					if (this.canDrawAt(pos)) {
						// Get the entities on the tile
						Optional<AbstractTileData> tileData = this.getHigherTile(
							this.getEntitiesAt(pos, playerPos.getZ()),
							this.getStructuresAt(pos, playerPos.getZ())
						);
						
						// Get the tile height
						final short height = this.getSurfaceHeight(pos);
						
						// Get the tile type
						final Tile tile = this.getEffectiveTileType(pos);
						
						// Create the color for the tile
						final Color color = (tileData.isPresent() ? tileData.get().getColor() : super.terrainColor(map, tile, pos));
						
						// Get the RGB of the color
						int r = color.getRed(),
							g = color.getGreen(),
							b = color.getBlue();
						
						// Apply a water filter to the color
						if (!tileData.isPresent() && height < 0) {
							r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
							g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
							b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
						}
						
						// Set the color for the tile
						grid.setAt(TileRenderLayer.TERRAIN,pos, r, g, b);
						grid.setData(pos, this.positionData(new ArrayList<>(), map, pos));
					}
					
					Optional<Color> deedColor = this.getDeedColorAt(pos);
					if (deedColor.isPresent()) grid.setAt(TileRenderLayer.ENTITY,pos, deedColor.get());
				}
			}
		}
		
		// Stitch the map squares together
		return MapTile.join(imageDimension, grids);
	}
	
	@Override
	public final RenderType getRenderType() {
		return RenderType.FLAT;
	}
}
