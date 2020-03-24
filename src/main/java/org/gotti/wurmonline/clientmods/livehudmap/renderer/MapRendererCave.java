package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;

public final class MapRendererCave extends AbstractCaveRenderer {
	
	public MapRendererCave(CaveDataBuffer buffer) {
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
				final short height = this.getHeight(x + leftX, y + topY);
				
				// Get the tile type
				Tile tile = this.getEffectiveTileType(x + leftX, y + topY);
				
				// Create the color for the tile
				final Color color = ( playerAt ? Color.RED : CaveColors.getColorFor( tile == null ? Tile.TILE_CAVE : tile ));
				
				// Get the RGB of the color
				int r = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();
				
				// Offset the color
				if (!playerAt && height < 0 && tile == Tile.TILE_CAVE) {
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
	
	@Override
	protected Tile getEffectiveTileType( int x, int y ) {
		Tile tile = this.getTileType( x, y );
		
		// If tile is an ore
		if ((!LiveMap.SHOW_ORES) && tile.isOreCave())
			return Tile.TILE_CAVE_WALL;
		
		// If ore should be hidden if it is fully concealed
		if (!LiveHudMapMod.SHOW_HIDDEN_ORE && tile != Tile.TILE_CAVE_WALL && !isTunnel( tile ) && isSurroundedByRock( x, y ))
			return Tile.TILE_CAVE_WALL;
		
		return tile;
	}
}
