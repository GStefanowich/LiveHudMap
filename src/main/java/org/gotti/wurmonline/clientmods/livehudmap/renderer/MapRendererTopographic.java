package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.renderer.cell.CellRenderer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.AbstractTileData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Direction;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;

public class MapRendererTopographic extends AbstractSurfaceRenderer {
	private short interval;
	
	public MapRendererTopographic(CellRenderer renderer, NearTerrainDataBuffer buffer) {
		super(renderer, buffer );
		this.interval = 250;
	}
	
	@Override
	public BufferedImage createMapDump(LiveMap map, int leftX, int topY, int imageX, int imageY, Coordinate playerPos) {
		Set<MapTile> grids = new HashSet<>();
		
		// Image should always be a square that covers both lengths
		final int imageDimension = Math.max(imageY, imageX);
		
		// For X direction
		for (int x = 0; x < imageX; x++) {
			// For Y direction
			for (int y = imageX - 1; y >= 0; y--) {
				final Coordinate pos = Coordinate.of( x + leftX, y + topY );
				final MapTile grid = this.getMapTile(pos);
				
				if (grid != null) {
					grids.add(grid);
					
					// Update if the player is within view
					if (LiveMap.isWithinPlayerView(pos)) {
						// Get the entities on the tile
						Optional<? extends AbstractTileData> tileData = this.getHigherTile(
							map.getEntitiesAt(pos),
							this.getStructuresAt(pos, playerPos.getZ())
						);
						
						// Get the tile height
						final short height = getSurfaceHeight(pos);
						
						// Get the tile type
						final Tile tile = this.getEffectiveTileType(pos);
						
						// Create the color for the tile
						final Color color = (tileData.isPresent() ? tileData.get().getColor() : super.tileColor(map, tile, pos));
						
						// Get the RGB of the color
						int r = color.getRed();
						int g = color.getGreen();
						int b = color.getBlue();
						
						// Change the terrain if the player isn't standing in the way
						if (!tileData.isPresent()) {
							final short nearHeightNX = x == 0 ? height : this.getSurfaceHeight(pos.offset(Direction.WEST));
							final short nearHeightNY = y == 0 ? height : this.getSurfaceHeight(pos.offset(Direction.NORTH));
							final short nearHeightX = x == imageX - 1 ? height : this.getSurfaceHeight(pos.offset(Direction.EAST));
							final short nearHeightY = y == imageX - 1 ? height : this.getSurfaceHeight(pos.offset(Direction.SOUTH));
							
							final boolean isContour = this.checkContourLine(height, nearHeightNX, this.interval)
								|| this.checkContourLine(height, nearHeightNY, this.interval)
								|| this.checkContourLine(height, nearHeightX, this.interval)
								|| this.checkContourLine(height, nearHeightY, this.interval)
								|| this.isLargeAdjacent(map, pos);
							
							if (isContour) {
								r = 0;
								g = 0;
								b = 0;
							} else if (height < 0) {
								r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
								g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
								b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
							}
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
	
	private boolean isLargeAdjacent( LiveMap map, Coordinate center ) {
		for (Direction direction : Direction.values()) {
			List<TileEntityData> tile = map.getEntitiesAt( center.offset( direction ));
			for (TileEntityData entity : tile) {
				if (entity.getPos().size() > 1)
					return true;
			}
		}
		return false;
	}
	private boolean checkContourLine(short h0, short h1, short interval) {
		if (h0 == h1)
			return false;
		for (int i = h0; i <= h1; i++) {
			if (i % interval == 0)
				return true;
		}
		return false;
	}
	
	@Override
	public final RenderType getRenderType() {
		return RenderType.TOPOGRAPHIC;
	}
}
