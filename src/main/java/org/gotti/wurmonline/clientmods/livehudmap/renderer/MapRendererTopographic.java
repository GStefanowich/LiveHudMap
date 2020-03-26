package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Direction;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileStructureData;

public class MapRendererTopographic extends AbstractSurfaceRenderer {
	private short interval;
	
	public MapRendererTopographic( RenderType type, NearTerrainDataBuffer buffer ) {
		super( type, buffer );
		this.interval = 250;
	}
	
	@Override
	public BufferedImage createMapDump(LiveMap map, int leftX, int topY, int imageX, int imageY, Coordinate playerPos) {
		if (topY < 0) topY = 0;
		if (leftX < 0) leftX = 0;
		
		// Image should always be a square that covers both lengths
		int imageDimension = Math.max(imageY, imageX);
		
		// Create a new image canvas
		final BufferedImage bi2 = new BufferedImage(imageDimension, imageDimension, BufferedImage.TYPE_INT_RGB);
		// Create an RGB map (Length x Width x 3[RGB])
		final float[] data = new float[imageX * imageX * 3];
		
		// For X direction
		for (int x = 0; x < imageX; x++) {
			// For Y direction
			for (int y = imageX - 1; y >= 0; y--) {
				Coordinate pos = Coordinate.of( x + leftX, y + topY );
				
				// If the player is on the tile
				List<TileEntityData> entityAt = map.getEntitiesAt( pos );
				List<TileStructureData> structureAt = map.getStructuresAt( pos );
				
				// Get the tile height
				final short height = getSurfaceHeight(pos );
				
				final short nearHeightNX = x == 0 ? height : getSurfaceHeight(pos.getX() - 1, pos.getY());
				final short nearHeightNY = y == 0 ? height : getSurfaceHeight(pos.getX(), pos.getY() - 1);
				final short nearHeightX = x == imageX - 1 ? height : getSurfaceHeight(pos.getX() + 1, pos.getY());
				final short nearHeightY = y == imageX - 1 ? height : getSurfaceHeight(pos.getX(), pos.getY() + 1);
				
				boolean isContour = this.checkContourLine(height, nearHeightNX, this.interval)
					|| this.checkContourLine(height, nearHeightNY, this.interval)
					|| this.checkContourLine(height, nearHeightX, this.interval)
					|| this.checkContourLine(height, nearHeightY, this.interval)
					|| (entityAt.isEmpty() && this.isLargeAdjacent( map, pos ));
				
				// Get the tile type
				final Tile tile = this.getTileType( pos );
				
				// Create the color for the tile
				final Color color = (this.colorEntityPriority( entityAt, structureAt ) ? map.entityColor(entityAt.get( 0 )) : super.tileColor(map, tile, pos ));
				
				// Get the RGB of the color
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				
				// Change the terrain if the player isn't standing in the way
				if (entityAt.isEmpty() && structureAt.isEmpty()) {
					if ( isContour ) {
						r = 0;
						g = 0;
						b = 0;
					} else if (height < 0) {
						r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
						g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
						b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
					}
				}
				
				int pixelPos = (x + y * imageX) * 3;
				data[pixelPos + 0] = r;
				data[pixelPos + 1] = g;
				data[pixelPos + 2] = b;
			}
		}
		
		bi2.getRaster().setPixels(0, 0, imageX, imageX, data);
		return bi2;
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
		if (h0 == h1) {
			return false;
		}
		for (int i = h0; i <= h1; i++) {
			if (i % interval == 0) {
				return true;
			}
		}
		return false;
	}
	
}
