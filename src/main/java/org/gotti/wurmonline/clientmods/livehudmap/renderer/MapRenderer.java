package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.image.BufferedImage;

import com.wurmonline.client.game.IDataBuffer;
import com.wurmonline.client.game.TerrainDataInformationProvider;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.mesh.Tiles.Tile;

public abstract class MapRenderer<DataType extends TerrainDataInformationProvider & IDataBuffer> {
	
	protected static final float MAP_HEIGHT = 1000;
	private final DataType buffer;
	
	protected MapRenderer( DataType buffer ) {
		this.buffer = buffer;
	}
	
	/**
	 * Render the map into an image.
	 * The map is rendered starting from (x,y) to (x+width,y+height).
	 * @param leftX Left tile
	 * @param topY Top tile
	 * @param winWidth Map view width in tiles
	 * @param winHeight Map view height in tiles
	 * @param leftX Player tile x
	 * @param topY Player tile y
	 * @return
	 */
	public abstract BufferedImage createMapDump( int leftX, int topY, int winWidth, int winHeight, int playerX, int playerY );
	
	/**
	 * Get the Data Buffer from the Server
	 * @return The Servers DataBuffer
	 */
	protected final DataType getBuffer() {
		return this.buffer;
	}
	
	/**
	 * Get tooltip information.
	 * @param pickData Tooltip data
	 * @param xMouse mouse position
	 * @param yMouse mouse position
	 * @param width Map view width in tiles
	 * @param height Map view height in tiles
	 * @param playerX Player tile x
	 * @param playerY Player tile y
	 */
	public void pick(PickData pickData, float xMouse, float yMouse, int width, int height, int playerX, int playerY) {
		// Offset cursor and window to get tile pos
		final int tileX = playerX + (int)(xMouse * width) - width / 2;
		final int tileY = playerY + (int)(yMouse * height) - height / 2;
		
		pickData.addText( "x" + tileX + ", y" + tileY );
	}
	
	/**
	 * Get the tile type from the information buffer
	 * @param x Layer x position
	 * @param y Layer y position
	 * @return Tile at the location
	 */
	protected final Tile getTileType(int x, int y) {
		return this.getBuffer().getTileType( x, y );
	}
	protected Tile getEffectiveTileType( int x, int y ) {
		return this.getTileType( x, y );
	}
}
