package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import com.wurmonline.client.game.IDataBuffer;
import com.wurmonline.client.game.TerrainDataInformationProvider;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.SklotopolisDeed;
import org.gotti.wurmonline.clientmods.livehudmap.assets.SklotopolisServer;

public abstract class MapRenderer<DataType extends TerrainDataInformationProvider & IDataBuffer> {
	
	protected static final float MAP_HEIGHT = 1000;
	private final RenderType type;
	private final DataType buffer;
	
	protected MapRenderer( RenderType type, DataType buffer ) {
		this.type = type;
		this.buffer = buffer;
	}
	
	/**
	 * Render the map into an image.
	 * The map is rendered starting from (x,y) to (x+width,y+height).
	 *
	 * @param map The server to create a map dump of
	 * @param leftX Left tile
	 * @param topY Top tile
	 * @param imageX Map view width in tiles
	 * @param imageY Map view height in tiles
	 * @return returns of image of the map
	 */
	public abstract BufferedImage createMapDump(LiveMap map, int leftX, int topY, int imageX, int imageY, Coordinate playerPos);
	
	/**
	 * Get the Data Buffer from the Server
	 * @return The Servers DataBuffer
	 */
	protected final DataType getBuffer() {
		return this.buffer;
	}
	
	/**
	 * Get information that should show at the top of the tooltip
	 * @param map The livemap
	 * @param tooltip Tooltip data
	 * @param tilePos The tile the cursor is hovering
	 * @param player Player tile x
	 */
	protected abstract void abstractTooltip(LiveMap map, PickData tooltip, Coordinate tilePos, Coordinate player);
	
	/**
	 * Get tooltip information.
	 * @param tooltip Tooltip data
	 * @param pos The tile the cursor is hovering
	 * @param player Player tile
	 */
	public final void tooltip(LiveMap map, PickData tooltip, Coordinate pos, Coordinate player) {
		// If the tile is within a deed
		SklotopolisServer server = map.getServer();
		if (server != null && LiveMap.SHOW_DEEDS) {
			// Add deed information
			Optional<SklotopolisDeed> search = server.getDeed( pos );
			search.ifPresent(deed -> {
				tooltip.addText(deed.getName() + " [" + deed.getDimensions() + "]");
				if (deed.isSpawn())
					tooltip.addText("Spawn");
			});
		}
		
		// List all of the entities at a tile
		List<TileEntityData> entities = map.getEntitiesAt( pos );
		for (TileEntityData entity : entities)
			tooltip.addText(entity.getName());
		
		// Add middle-provided information about the layer
		this.abstractTooltip(map, tooltip, pos, player);
		
		// Add coordinates as the last element
		tooltip.addText( "x" + pos.getX() + ", y" + pos.getY() );
	}
	
	/**
	 * Get the tile type from the information buffer
	 * @param x Layer x position
	 * @param y Layer y position
	 * @return Tile at the location
	 */
	protected final Tile getTileType( int x, int y ) {
		return this.getBuffer().getTileType( x, y );
	}
	protected final Tile getTileType( Coordinate pos ) {
		return this.getTileType(pos.getX(), pos.getY());
	}
	protected Tile getEffectiveTileType( int x, int y ) {
		return this.getTileType( x, y );
	}
	protected Tile getEffectiveTileType( Coordinate pos ) {
		return this.getTileType( pos );
	}
	
	/**
	 * @param map The active livemap
	 * @param tile The tile type
	 * @param pos The position to get the tile of
	 * @return The color of the tile
	 */
    protected abstract Color tileColor(LiveMap map, Tile tile, Coordinate pos);
}
