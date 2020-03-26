package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileStructureData;

public class MapRendererFlat extends AbstractSurfaceRenderer {
	public MapRendererFlat(RenderType type, NearTerrainDataBuffer buffer) {
		super( type, buffer );
	}
	
	@Override
	public BufferedImage createMapDump(LiveMap map, int leftX, int topY, int imageX, int imageY, Coordinate playerPos) {
		if (topY < 0) topY = 0;
		if (leftX < 0) leftX = 0;
		
		// Image should always be a square that covers both lengths
		int imageDimension = Math.max(imageY, imageX );
		
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
				final short height = this.getSurfaceHeight( pos );
				
				// Get the tile type
				final Tile tile = this.getTileType( pos );
				
				// Create the color for the tile
				final Color color = ( entityAt.isEmpty() ? super.tileColor(map, tile, pos ) : map.entityColor(entityAt.get( 0 )));
				
				// Get the RGB of the color
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				
				// Apply a water filter to the color
				if (entityAt.isEmpty() && structureAt.isEmpty() && height < 0) {
					r = (int)(r * 0.2f + 0.4f * 0.4f * 256f);
					g = (int)(g * 0.2f + 0.5f * 0.4f * 256f);
					b = (int)(b * 0.2f + 1.0f * 0.4f * 256f);
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
}
