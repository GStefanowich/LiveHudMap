package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import com.wurmonline.client.game.CaveDataBuffer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;

public final class MapRendererCave extends AbstractCaveRenderer {
	
	public MapRendererCave( RenderType type, CaveDataBuffer buffer ) {
		super(type, buffer);
	}
	
	@Override
	public BufferedImage createMapDump(LiveMap map, int leftX, int topY, int imageX, int imageY, Coordinate playerPos) {
		if (topY < 0) topY = 0;
		if (leftX < 0) leftX = 0;
		
		// Image should always be a square that covers both lengths
		int imageXY = Math.max( imageY, imageX );
		
		// Create a new image canvas
		final BufferedImage bi2 = new BufferedImage(imageXY, imageXY, BufferedImage.TYPE_INT_RGB);
		// Create an RGB map (Length x Width x 3[RGB])
		final float[] data = new float[imageX * imageX * 3];
		
		// For X direction
		for (int x = 0; x < imageX; x++) {
			// For Y direction
			for (int y = imageX - 1; y >= 0; y--) {
				Coordinate pos = Coordinate.of( x + leftX, y + topY );
				
				// If the player is on the tile
				List<TileEntityData> entityAt = map.getEntitiesAt( pos );
				
				// Get the tile height
				final short height = this.getHeight( pos );
				
				// Get the tile type
				Tile tile = this.getEffectiveTileType( pos );
				
				// Create the color for the tile
				final Color color = ( entityAt.isEmpty() ? super.tileColor(map, tile, pos ) : map.entityColor(entityAt.get( 0 )));
				
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
				
				int pixelPos = (x + y * imageX) * 3;
				data[pixelPos + 0] = r;
				data[pixelPos + 1] = g;
				data[pixelPos + 2] = b;
			}
		}
		
		bi2.getRaster().setPixels(0, 0, imageX, imageX, data);
		return bi2;
	}
	
	@Override
	protected Tile getEffectiveTileType( int x, int y ) {
		return this.getEffectiveTileType(Coordinate.of( x, y ));
	}
	@Override
	protected Tile getEffectiveTileType( Coordinate pos ) {
		Tile tile = this.getTileType( pos );
		
		// If tile is an ore
		if ((!LiveMap.SHOW_ORES) && tile.isOreCave())
			return Tile.TILE_CAVE_WALL;
		
		// If ore should be hidden if it is fully concealed
		if (!LiveHudMapMod.SHOW_HIDDEN_ORE && tile != Tile.TILE_CAVE_WALL && !this.isTunnel( tile ) && this.isSurroundedByRock( pos ))
			return Tile.TILE_CAVE_WALL;
		
		return tile;
	}
}
