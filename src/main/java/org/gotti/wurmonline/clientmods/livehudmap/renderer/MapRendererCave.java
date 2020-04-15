package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.client.renderer.cell.CellRenderer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;

public final class MapRendererCave extends AbstractCaveRenderer {
	
	public MapRendererCave(CellRenderer renderer, CaveDataBuffer buffer) {
		super(renderer,buffer);
	}
	
	@Override
	public BufferedImage createMapDump(LiveMap map, int leftX, int topY, int imageX, int imageY, Coordinate playerPos) {
		Set<MapTile> grids = new HashSet<>();
		
		// Image should always be a square that covers both lengths
		final int imageDimension = Math.max( imageY, imageX );
		
		// For X direction
		for (int x = 0; x < imageX; x++) {
			// For Y direction
			for (int y = imageX - 1; y >= 0; y--) {
				final Coordinate pos = Coordinate.of( x + leftX, y + topY );
				final MapTile grid = this.getMapTile(pos);
				
				if (grid != null) {
					grids.add( grid );
					
					// Update if the player is within view
					if (this.canDrawAt(pos)) {
						// If the player is on the tile
						List<TileEntityData> entityAt = map.getEntitiesAt(pos);
						
						// Get the tile height
						final short height = this.getHeight(pos);
						
						// Get the tile type
						Tile tile = this.getEffectiveTileType(pos);
						
						// Create the color for the tile
						final Color color = (entityAt.isEmpty() ? super.tileColor(map, tile, pos) : map.entityColor(entityAt.get(0)));
						
						// Get the RGB of the color
						int r = color.getRed();
						int g = color.getGreen();
						int b = color.getBlue();
						
						// Offset the color
						if (entityAt.isEmpty() && height < 0 && tile == Tile.TILE_CAVE) {
							r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
							g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
							b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
						}
						
						// Set the color for the tile
						grid.setAt(pos, r, g, b);
					}
				}
			}
		}
		
		// Stitch the map squares together
		return MapTile.join(imageDimension, grids);
	}
}
