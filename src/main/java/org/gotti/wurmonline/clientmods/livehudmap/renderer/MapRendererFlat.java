package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;

public class MapRendererFlat extends AbstractSurfaceRenderer {
	public MapRendererFlat(NearTerrainDataBuffer buffer) {
		super(buffer);
	}
	
	@Override
	public BufferedImage createMapDump(int leftX, int topY, int winWidth, int winHeight, int playerX, int playerY) {
		if (topY < 0) topY = 0;
		if (leftX < 0) leftX = 0;
		
		// Create a new image canvas
		final BufferedImage bi2 = new BufferedImage(winWidth, winWidth, BufferedImage.TYPE_INT_RGB);
		// Create an RGB map (Length x Width x 3[RGB])
		final float[] data = new float[winWidth * winWidth * 3];
		
		// For X direction
		for (int x = 0; x < winWidth; x++) {
			// For Y direction
			for (int y = winWidth - 1; y >= 0; y--) {
				// If the player is on the tile
				final boolean playerAt = LiveMap.SHOW_SELF && (playerX == (x + leftX)) && (playerY == (y + topY));
				
				// Get the tile height
				final short height = getSurfaceHeight(x + leftX, y + topY);
				
				// Get the tile type
				final Tile tile = getTileType(x + leftX, y + topY);
				
				// Create the color for the tile
				final Color color = ( playerAt? Color.RED : TileColors.getColorFor( tile == null ? Tile.TILE_DIRT : tile ));
				
				// Get the RGB of the color
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				
				if ( !playerAt && height < 0) {
					r = (int) (r * 0.2f + 0.4f * 0.4f * 256f);
					g = (int) (g * 0.2f + 0.5f * 0.4f * 256f);
					b = (int) (b * 0.2f + 1.0f * 0.4f * 256f);
				}
				
				int pixelPos = (x + y * winWidth) * 3;
				data[pixelPos + 0] = r;
				data[pixelPos + 1] = g;
				data[pixelPos + 2] = b;
			}
		}
		
		bi2.getRaster().setPixels(0, 0, winWidth, winWidth, data);
		return bi2;
	}
}
