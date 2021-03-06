package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.wurmonline.client.game.IDataBuffer;
import com.wurmonline.client.game.TerrainDataInformationProvider;
import com.wurmonline.client.renderer.GroundItemData;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.renderer.cell.Cell;
import com.wurmonline.client.renderer.cell.CellRenderable;
import com.wurmonline.client.renderer.cell.CellRenderer;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;
import com.wurmonline.client.renderer.cell.GroundItemCellRenderable;
import com.wurmonline.client.renderer.structures.BridgePartData;
import com.wurmonline.client.renderer.structures.FenceData;
import com.wurmonline.client.renderer.structures.HouseRoofData;
import com.wurmonline.client.renderer.structures.StructureData;
import com.wurmonline.client.renderer.structures.WallData;
import com.wurmonline.mesh.Tiles.Tile;
import org.apache.commons.codec.Charsets;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.MapLayer;
import org.gotti.wurmonline.clientmods.livehudmap.assets.AbstractTileType;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.EntityType;
import org.gotti.wurmonline.clientmods.livehudmap.assets.LiveMapConfig;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Region;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Server;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Servers;
import org.gotti.wurmonline.clientmods.livehudmap.assets.SklotopolisDeed;
import org.gotti.wurmonline.clientmods.livehudmap.assets.SklotopolisServer;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Sync;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileStructureData;
import org.gotti.wurmonline.clientmods.livehudmap.reflection.Entities;
import org.gotti.wurmonline.clientmods.livehudmap.reflection.GroundItems;
import org.gotti.wurmonline.clientmods.livehudmap.reflection.Structures;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
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
			.maximumSize((long)(Math.pow(Math.floorDiv(this.getRenderType().getMapSize(), LiveMapConfig.MAP_TILE_SIZE), 2) * 3))
			.expireAfterAccess(LiveMapConfig.SAVE_SECONDS, TimeUnit.SECONDS)
			.refreshAfterWrite(LiveMapConfig.SAVE_SECONDS, TimeUnit.SECONDS)
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
					future.setFuture(LiveMap.threadExecute(() -> {
						// Read the image from the file
						return Sync.runOr( region.toString(), () -> {
							// Load the map from the disk using "key"
							Path subFolder = Paths.get(LiveHudMapMod.MOD_FOLDER.toString(), LiveHudMapMod.getServerName(), MapRenderer.this.getRenderType().name().toLowerCase());
							
							// Create the folder for the layer if it does not exist
							if (!Files.exists(subFolder))
								Files.createDirectories(subFolder);
							
							// Check if the map file exists
							File imageFile = Paths.get(subFolder.toString(), region.toString() + ".png").toFile();
							if (!imageFile.exists())
								return MapTile.empty( region );
							
							LiveHudMapMod.log("Last modified: " + imageFile.lastModified());
							
							// Check if the data file exists
							Path dataPath = Paths.get(subFolder.toString(), region.toString() + ".json");
							File dataFile = dataPath.toFile();
							final JSONObject data;
							if (dataFile.exists()) {
								String allLines = String.join("",Files.readAllLines(dataPath, Charsets.UTF_8));
								if (allLines.startsWith("{") && allLines.endsWith("}"))
									data = new JSONObject(allLines);
								else data = new JSONObject();
							} else data = new JSONObject();
							
							return MapTile.from( region, ImageIO.read(imageFile), data);
						}, MapTile.empty( region ));
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
	 * Check if new data should be written over the pos.
	 * @param pos The position to check drawing ability
	 * @return If the tile is within the players site, or outside the map border
	 */
	protected final boolean canDrawAt(Coordinate pos) {
		return LiveMap.isWithinPlayerView(pos) || (!LiveMap.isWithinMap(pos));
	}
	
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
	 * @param list The list of tooltip values
	 * @param map The livemap
	 * @param tilePos The tile the cursor is hovering
	 */
	protected abstract List<String> positionData(List<String> list, LiveMap map, Coordinate tilePos);
	
	/**
	 * Get tooltip information.
	 * @param pickData Tooltip data
	 * @param pos The tile the cursor is hovering
	 * @param player Player tile
	 */
	public final void tooltip(LiveMap map, PickData pickData, Coordinate pos, Coordinate player) {
		// If the tile is within a deed
		Server server = Servers.getServer();
		if (server instanceof SklotopolisServer && LiveMap.SHOW_DEEDS) {
			// Add deed information
			Optional<SklotopolisDeed> search = ((SklotopolisServer) server).getDeed( pos );
			search.ifPresent(deed -> {
				pickData.addText(deed.getName() + " [" + deed.getDimensions() + "]");
				if (deed.isSpawn())
					pickData.addText("Spawn");
			});
		}
		
		// List all of the entities at a tile
		List<TileEntityData> entities = this.getEntitiesAt( pos, player.getZ() );
		for (TileEntityData entity : entities)
			pickData.addText(entity.getName());
		
		// Add middle-provided information about the layer
		MapTile mapTile = this.getMapTile(pos);
		if (mapTile != null) {
			List<String> tooltips = mapTile.getData(pos);
			for (String t : tooltips)
				pickData.addText(t);
		}
		
		// Add coordinates as the last element
		pickData.addText( "x" + pos.getX() + ", y" + pos.getY() );
	}
	
	/**
	 * Get the tile type from the information buffer
	 * @param pos X, Y coordinates of tile
	 * @return Tile at the location
	 */
	protected final Tile getTileType( Coordinate pos ) {
		if (!(LiveMap.isWithinPlayerView(pos) && LiveMap.isWithinMap(pos)))
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
	
	private static final Coordinate TEST_COORDINATE = Coordinate.of(2469,3460);
	
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
		Cell cell = this.getRenderer().findCell(pos.getCellX(), pos.getCellY(), playerLayer);
		return Optional.ofNullable(cell.getBridgeAtSurfaceAt(pos.getX(), pos.getY()));
	}
	protected final Optional<StructureData> getStructureAt(Coordinate pos, int playerLayer) {
		Cell cell = this.getRenderer().findCell((float)pos.getCellX(), (float)pos.getCellY(), playerLayer);
		Iterator<StructureData> structures = Structures.getStructures( cell ).iterator();
		
		StructureData data = null;
		while (structures.hasNext()) {
			StructureData next = structures.next();
			if (next == null || next instanceof WallData || next.getTileX() != pos.getX() || next.getTileY() != pos.getY())
				continue;
			
			if (data == null || next.getHeightOffset() > data.getHeightOffset())
				data = next;
		}
		
		return Optional.ofNullable( data );
	}
	protected final Optional<Color> getDeedColorAt(Coordinate pos) {
		Server server = Servers.getServer();
		
		// Color deed borders
		if (server instanceof SklotopolisServer && LiveMap.SHOW_DEEDS)
			return ((SklotopolisServer) server).getDeedBorder(pos).map(deed -> this.getRenderType() == RenderType.CAVE ? Color.GRAY : deed.getColor());
		return Optional.empty();
	}
	
	protected final List<TileEntityData> getEntitiesAt(Coordinate pos, int playerLayer) {
		List<TileEntityData> data = new ArrayList<>();
		
		data.addAll(this.getCreaturesAt(pos, playerLayer));
		data.addAll(this.getGroundItems(pos, playerLayer));
		
		return data;
	}
	protected final List<TileEntityData> getCreaturesAt(Coordinate pos, int playerLayer) {
		List<TileEntityData> data = new ArrayList<>();
		
		Cell cell = this.getRenderer().findCell((float)pos.getCellX(), (float)pos.getCellY(), playerLayer);
		Iterator<CellRenderable> creatures = Entities.getEntities(cell).iterator();
		while (creatures.hasNext()) {
			CellRenderable renderable = creatures.next();
			
			if ((!(renderable instanceof CreatureCellRenderable)) || (((int)Math.floor(renderable.getXPos() / 4)) != pos.getX()) || ((int)Math.floor(renderable.getYPos() / 4)) != pos.getY())
				continue;
			
			CreatureCellRenderable creature = (CreatureCellRenderable) renderable;
			EntityType type;
			
			if ((type = EntityType.getByModelName(creature.getModelName())) != null)
				data.add(new TileEntityData(type, creature));
		}
		
		return data;
	}
	protected final List<TileEntityData> getGroundItems(Coordinate pos, int playerLayer) {
		List<TileEntityData> data = new ArrayList<>();
		
		Cell cell = this.getRenderer().findCell((float)pos.getCellX(), (float)pos.getCellY(), playerLayer);
		Iterator<CellRenderable> entities = Entities.getGroundItems(cell).iterator();
		while (entities.hasNext()) {
			CellRenderable renderable = entities.next();
			if ((!(renderable instanceof GroundItemCellRenderable)) || (((int)Math.floor(renderable.getXPos() / 4)) != pos.getX()) || ((int)Math.floor(renderable.getYPos() / 4)) != pos.getY())
				continue;
			
			GroundItemCellRenderable item = (GroundItemCellRenderable) renderable;
			GroundItemData itemDat = GroundItems.getData(item);
			EntityType type;
			
			if ((type = EntityType.getByModelName(itemDat.getModelName())) != null)
				data.add(new TileEntityData(type, itemDat));
		}
		
		return data;
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
    protected abstract Color terrainColor(LiveMap map, Tile tile, Coordinate pos);
}
