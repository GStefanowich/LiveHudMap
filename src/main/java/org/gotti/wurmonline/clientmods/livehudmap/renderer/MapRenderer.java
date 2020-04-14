package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.wurmonline.client.game.IDataBuffer;
import com.wurmonline.client.game.TerrainDataInformationProvider;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.renderer.cell.Cell;
import com.wurmonline.client.renderer.cell.CellRenderer;
import com.wurmonline.client.renderer.structures.BridgePartData;
import com.wurmonline.client.renderer.structures.FenceData;
import com.wurmonline.client.renderer.structures.HouseRoofData;
import com.wurmonline.client.renderer.structures.StructureData;
import com.wurmonline.client.renderer.structures.WallData;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.MapLayer;
import org.gotti.wurmonline.clientmods.livehudmap.assets.AbstractTileType;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Region;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.SklotopolisDeed;
import org.gotti.wurmonline.clientmods.livehudmap.assets.SklotopolisServer;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileStructureData;
import org.gotti.wurmonline.clientmods.livehudmap.reflection.Structures;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

public abstract class MapRenderer<DataType extends TerrainDataInformationProvider & IDataBuffer> {
	
	protected static final float MAP_HEIGHT = 1000;
	private final LoadingCache<Region, SettableFuture<MapTile>> mapTiles;
	private final CellRenderer renderer;
	private final MapLayer type;
	private final DataType buffer;
	
	protected MapRenderer(MapLayer layer, CellRenderer renderer, DataType buffer) {
		this.type = layer;
		this.buffer = buffer;
		this.renderer = renderer;
		this.mapTiles = CacheBuilder.newBuilder()
			.maximumSize(256)
			.expireAfterAccess(30, TimeUnit.SECONDS)
			.refreshAfterWrite(30, TimeUnit.SECONDS)
			.removalListener((RemovalListener<Region, SettableFuture<MapTile>>) notification -> {
				// Save the Tile to disk when it gets unloaded
				MapTile.saveToDisk(notification.getKey(), MapRenderer.this.getRenderType(), notification.getValue());
			})
			.build(new CacheLoader<Region, SettableFuture<MapTile>>() {
				@Override
				public SettableFuture<MapTile> load(@Nonnull Region region) throws Exception {
					// Create the settable
					SettableFuture<MapTile> future = SettableFuture.create();
					
					// Set the action that creatures the map from file
					future.setFuture(LiveHudMapMod.getMapLoader().submit(() -> {
						// Load the map from the disk using "key"
						Path subFolder = Paths.get(LiveHudMapMod.MOD_FOLDER.toString(), LiveHudMapMod.getServerName(), MapRenderer.this.getRenderType().name().toLowerCase());
						
						// Create the folder for the layer if it does not exist
						if (!Files.exists(subFolder))
							Files.createDirectories(subFolder);
						
						// Check if the map file exists
						File imageFile = Paths.get(subFolder.toString(), region.toString() + ".png").toFile();
						if (!imageFile.exists())
							return MapTile.empty( region );
						
						// Read the image from the file
						return MapTile.from( region, ImageIO.read( imageFile ));
					}));
					
					return future;
				}
				@Override
				public ListenableFuture<SettableFuture<MapTile>> reload(Region region, SettableFuture<MapTile> old) {
					// Save the Tile to disk when it gets unloaded
					MapTile.saveToDisk(region, MapRenderer.this.getRenderType(), old);
					
					// Return the old settable again (Doesn't need to be changed)
					return Futures.immediateFuture(old);
				}
			});
	}
	
	/**
	 * Load the MapTile from/to the Cache using the MapTile Region Coordinates
	 * @param coordinate The coordinates of a MapTile (Must be X/64, Y/64)
	 * @return A MapTile to draw tiles on
	 */
	protected MapTile getMapTile(Coordinate coordinate) {
		return this.getMapTile(coordinate.nearestRegion());
	}
	
	/**
	 * Load the MapTile from/to the Cache using the MapTile Region
	 * @param region The region for a MapTile
	 * @return A MapTile to draw tiles on
	 */
	protected MapTile getMapTile(Region region) {
		try {
			// Get the Future<MapTile>
			SettableFuture<MapTile> future = this.mapTiles.get(region);
			
			// If the future has been run, return the result
			if (future.isDone())
				return future.get();
		} catch (ExecutionException | InterruptedException e) {
			LiveHudMapMod.log(e);
		}
		return null;
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
	 * @return The games cell renderer
	 */
	protected final CellRenderer getRenderer() {
		return this.renderer;
	}
	
	/**
	 * @return The RenderType (Flat, Isometic, Topographic, Cave)
	 */
	public abstract RenderType getRenderType();
	
	/**
	 * @return Get the default elevation of the map
	 */
	public final int getLayer() {
		return this.type.getLayer();
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
	 * @param pos X, Y coordinates of tile
	 * @return Tile at the location
	 */
	protected final Tile getTileType( Coordinate pos ) {
		if (!LiveMap.isWithinPlayerView( pos ))
			return this.getDefaultTile();
		return this.getBuffer().getTileType(pos.getX(), pos.getY());
	}
	
	/**
	 * 
	 * @param pos X, Y coordinates of tile
	 * @return Tile to display
	 */
	protected Tile getEffectiveTileType( Coordinate pos ) {
		return this.getTileType( pos );
	}
	
	protected final List<TileStructureData> getStructuresAt(Coordinate pos, int playerLayer) {
		List<TileStructureData> data = new ArrayList<>();
		
		Optional<BridgePartData> bridges = this.getBridgeAt(pos, playerLayer);
		Optional<StructureData> structures = this.getStructureAt(pos, playerLayer);
		
		if (bridges.isPresent()) {
			BridgePartData bridge = bridges.get();
			data.add(new TileStructureData(AbstractTileType.BRIDGE, bridge.getHoverName(), bridge));
		}
		if (structures.isPresent()) {
			StructureData structure = structures.get();
			if (!(structure instanceof FenceData))
				data.add(new TileStructureData(structure instanceof HouseRoofData ? AbstractTileType.ROOF : AbstractTileType.HOUSE, structure.getHoverName(), structure));
		}
		
		return data;
	}
	protected final Optional<BridgePartData> getBridgeAt(Coordinate pos, int playerLayer) {
		Cell cell = this.getRenderer().findCell(pos.getTileX(), pos.getTileY(), playerLayer);
		return Optional.ofNullable(cell.getBridgeAtSurfaceAt(pos.getX(), pos.getY()));
	}
	protected final Optional<StructureData> getStructureAt(Coordinate pos, int playerLayer) {
		Cell cell = this.getRenderer().findCell((float)pos.getTileX(), (float)pos.getTileY(), playerLayer);
		Iterator<StructureData> structures = Structures.getStructures( cell ).iterator();
		
		StructureData data = null;
		while (structures.hasNext()){
			StructureData next = structures.next();
			if (next == null || next instanceof WallData || next.getTileX() != pos.getX() || next.getTileY() != pos.getY())
				continue;
			if (data == null || next.getLayer() > data.getLayer())
				data = next;
		}
		
		return Optional.ofNullable( data );
	}
	
	/**
	 * @return The default Tile for out-of-bounds locations
	 */
	protected abstract Tile getDefaultTile();
	
	/**
	 * @param map The active livemap
	 * @param tile The tile type
	 * @param pos The position to get the tile of
	 * @return The color of the tile
	 */
    protected abstract Color tileColor(LiveMap map, Tile tile, Coordinate pos);
}
