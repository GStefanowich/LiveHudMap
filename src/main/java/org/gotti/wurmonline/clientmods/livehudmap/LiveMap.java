package org.gotti.wurmonline.clientmods.livehudmap;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.wurmonline.client.game.TerrainChangeListener;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.renderer.backend.Queue;
import com.wurmonline.client.renderer.cave.CaveBufferChangeListener;
import com.wurmonline.client.renderer.gui.Renderer;
import com.wurmonline.client.resources.textures.ImageTexture;
import com.wurmonline.client.resources.textures.ImageTextureLoader;
import com.wurmonline.client.resources.textures.PreProcessedTextureData;
import com.wurmonline.client.resources.textures.TextureLoader;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Area;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Direction;
import org.gotti.wurmonline.clientmods.livehudmap.assets.LiveMapConfig;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Server;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Servers;
import org.gotti.wurmonline.clientmods.livehudmap.assets.SklotopolisServer;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.RenderType;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.TitleDisplay;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class LiveMap implements TerrainChangeListener, CaveBufferChangeListener {
	
	private static final ListeningExecutorService MAP_TILE_LOADER;
	private static final Area MAP_BORDER = Area.of(Coordinate.min(), Coordinate.max());
	private static final Area PLAYER_BORDER = Area.ofEmpty();
	private static final Method PROCESS_IMAGE_METHOD;
	private static Coordinate FIRST_MAP_SQUARE = Coordinate.min();
	
	public static boolean SHOW_SELF = true;
	public static boolean SHOW_DEEDS = true;
	public static boolean SHOW_PLAYERS = true;
	public static boolean SHOW_CREATURES = true;
	public static boolean SHOW_HOSTILES = true;
	public static boolean SHOW_VEHICLES = true;
	public static boolean SHOW_BUILDINGS = false;
	public static boolean SHOW_ROADS = false;
	public static boolean SHOW_ORES = true;
	
	public static int REFRESH_RATE = 8;
	
	private int dirtyTimer = 0;
	private int size;
	
	/*private final Map<Long, GroundItemCellRenderable> ref_GroundItems;
	private final Map<Long, CreatureCellRenderable> ref_Creatures;
	private final Map<Long, StructureData> ref_Structures;*/
	
	private final World world;
	
	private MapLayer playerLayer = this.updateLayer();
	
	private boolean dirty = true;
	private BufferedImage image;
	private ImageTexture texture;
	private final Function<String, Boolean> onUpdate;
	
	private int windowX;
	private int windowY;
	
	private Coordinate mapCenter = null;
	private Coordinate currentMapCenter = Coordinate.min();
	
	public LiveMap(World world, int size) {
		this( world, size, null );
	}
	public LiveMap(World world, int size, Function<String, Boolean> callable) {
		this.size = size;
		
		this.world = world;
		
		this.world.getNearTerrainBuffer().addListener(this);
		this.world.getCaveBuffer().addCaveBufferListener(this);
		
		this.onUpdate = callable;
		
		/*ServerConnectionListenerClass listener = this.world.getClient()
			.getConnectionListener();*/
		
		/*this.ref_Creatures = listener.getCreatures();
		this.ref_Structures = listener.getStructures();
		try {
			
			Field field = ReflectionUtil.getField(ServerConnectionListenerClass.class, "groundItems");
			this.ref_GroundItems = ReflectionUtil.getPrivateField(listener, field);
			
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException( e );
		}*/
	}
	
	// Tick update
	public void update(int windowX, int windowY) {
		this.windowX = windowX;
		this.windowY = windowY;
		
		// Check the server the player is on
		if (Servers.getServer() == null || (!Servers.getServer().getName().equalsIgnoreCase(this.world.getServerName()))) {
			// Get the server
			Servers.getServerByName(this.world.getServerName());
			
			// Initialize the servers deeds
			this.initializeServer();
		}
		
		Coordinate newCurrentCenter = this.tickMapCenter();
		
		// Check if the map should be considered dirty
		if (!this.dirty && (!this.getCurrentMapCenter().equals( newCurrentCenter )) || ( this.dirtyTimer <= 0 ))
			this.markDirty();
		
		// Update the layer
		/*if (this.getLayer().tick())
			this.updateEntities();*/
		
		if ( !this.dirty )
			this.dirtyTimer--;
		else {
			// Check the players layer
			this.playerLayer = this.updateLayer();
			
			// Get the players location
			this.currentMapCenter = newCurrentCenter;
			
			this.image = this.getLayer()
				.render( this, this.currentMapCenter);
			
			if (this.texture == null)
				this.texture = ImageTextureLoader.loadNowrapNearestTexture(this.image, false);
			else {
				try {
					PreProcessedTextureData data = ReflectionUtil.callPrivateMethod(
						TextureLoader.class,
						LiveMap.PROCESS_IMAGE_METHOD,
						this.image,
						true
					);
					
					this.texture.deferInit(data, TextureLoader.Filter.NEAREST, false, false, false);
				} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
			}
			
			this.update();
		}
	}
	private void update() {
		// Update the information over the top of the map
		if (this.onUpdate != null) {
			this.onUpdate.apply(TitleDisplay.format(
				Servers.getServer(),
				this.world.getSeasonManager().getSeason(),
				this.currentMapCenter,
				this.getLayer().getZoom(),
				this.world.getWurmTime() - this.world.getWurmTimeOffset()
			));
		}
		this.dirty = false;
	}
	private MapLayer updateLayer() {
		return this.world == null ? MapLayer.SURFACE : MapLayer.getByElevation(this.world.getPlayerLayer());
	}
	
	// Set the center of the map
	public Coordinate tickMapCenter() {
		// Get where the players location is
		Coordinate player = this.getPlayerPosition();
		int distance = this.getRenderer().getMapSize() / (this.getLayer().getZoom() * 2);
		
		// Update the players positioning borders
		LiveMap.PLAYER_BORDER.set(
			player.offset(
				Direction.NORTH,
				distance,
				Direction.WEST,
				distance
			),
			player.offset(
				Direction.SOUTH,
				distance,
				Direction.EAST,
				distance
			)
		);
		
		LiveMap.FIRST_MAP_SQUARE = (this.mapCenter == null ? player : this.mapCenter).offset(
			Direction.NORTH,
			distance,
			Direction.WEST,
			distance
		);
		
		// Return where the map is currently centered
		if (this.mapCenter != null)
			return this.mapCenter;
		return player;
	}
	public Coordinate getPlayerPosition() {
		return Coordinate.of(this.world.getPlayer()
			.getPos());
	}
	public Coordinate getCurrentMapCenter() {
		return this.currentMapCenter;
	}
	
	public void setCenter(Coordinate pos) {
		if (!Coordinate.equals(this.mapCenter, pos)) {
			// Snap the position to the nearest position within the map
			if (pos != null)
				pos = LiveMap.snapNearestBorder(pos);
			
			this.mapCenter = pos;
			this.markDirty();
		}
	}
	
	// Get the player information
	public World getWorld() {
		return this.world;
	}
	
	public void initializeServer() {
		Servers.initialize(this.world);
	}
	
	// Get the current layer that the player is on
	private MapLayerView getLayer() {
		return this.playerLayer.getMap();
	}
	
	// Apply rotation based on the way the player is facing
	private BufferedImage applyRotation( BufferedImage image, float angle ) {
		// Scale the image (To help pixelation)
		BufferedImage scaled = this.applyScale( image );
		
		int width = scaled.getWidth(),
			height = scaled.getHeight();
		
		// Create a new buffered image that's rotated
		BufferedImage rotated = new BufferedImage(width, height, scaled.getType());
		
		Graphics2D graphic = rotated.createGraphics();
		graphic.rotate(Math.toRadians( angle ), width * 0.5, height * 0.5 );
		graphic.drawImage(scaled, null, 0, 0);
		graphic.dispose();
		
		return rotated;
	}
	private BufferedImage applyScale( BufferedImage image ) {
		int scale = this.getLayer()
			.getZoom();
		int width = image.getWidth() * scale,
			height = image.getHeight() * scale;
		
		BufferedImage scaled = new BufferedImage(width, height, image.getType());
		Graphics2D graphics = scaled.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		graphics.drawImage(image, 0, 0, width, height, null);
		graphics.dispose();
		
		return scaled;
	}
	
	// Set as dirty
	public void markDirty() {
		this.dirtyTimer = LiveMap.REFRESH_RATE;
		this.dirty = true;
	}
	
	// Change the renderer type
	public void setRenderer(MapLayer layer, RenderType renderType) {
		layer.getMap()
			.setRenderer(renderType);
		this.markDirty();
	}
	public RenderType getRenderer() {
		return this.getLayer()
			.getRenderer();
	}
	
	// Change the zoom of the map
	public void zoomIn() {
		this.getLayer()
			.zoomIn();
		this.markDirty();
	}
	public void zoomOut() {
		this.getLayer()
			.zoomOut();
		this.markDirty();
	}
	
	// Change map options
	public void toggleShowSelf() {
		SHOW_SELF = !SHOW_SELF;
		this.markDirty();
	}
	public void toggleShowDeeds() {
		SHOW_DEEDS = !SHOW_DEEDS;
		this.markDirty();
	}
	public void toggleShowPlayers() {
		SHOW_PLAYERS = !SHOW_PLAYERS;
		this.markDirty();
	}
	public void toggleShowCreatures() {
		SHOW_CREATURES = !SHOW_CREATURES;
		this.markDirty();
	}
	public void toggleShowHostiles() {
		SHOW_HOSTILES = !SHOW_HOSTILES;
		this.markDirty();
	}
	public void toggleShowVehicles() {
		SHOW_VEHICLES = !SHOW_VEHICLES;
		this.markDirty();
	}
	public void toggleShowBuildings() {
		SHOW_BUILDINGS = !SHOW_BUILDINGS;
		this.markDirty();
	}
	public void toggleShowRoads() {
		SHOW_ROADS = !SHOW_ROADS;
		this.markDirty();
	}
	public void toggleShowOres() {
		SHOW_ORES = !SHOW_ORES;
		this.markDirty();
	}
	
	/*
	 * Rendering
	 */
	public void render(Queue queue, float textureScale) {
		if (this.texture != null) {
			Color transparency = Color.WHITE;
			
			Renderer.texturedQuadAlphaBlend(
				queue, // Queue
				// Image
				this.texture, // Texture
				// Background layer
				transparency.getRed(), // R
				transparency.getGreen(), // G
				transparency.getBlue(), // B
				transparency.getAlpha(), // A
				// Window position
				(float)(this.windowX), // X
				(float)(this.windowY), // Y
				// Window size (Final result)
				(float)(this.size), // Width
				(float)(this.size), // Height
				// Image stretch
				0.0f, // U
				0.0f, // V
				textureScale, // uWidth
				textureScale // vWidth
			);
		}
	}
	
	/*
	 * Map Data Caching
	 */
	public void updateEntities() {
		/*// Get the creatures from the world
		Map<Long, CreatureCellRenderable> creatures = this.getCreatures();
		Map<Long, GroundItemCellRenderable> groundItems = this.getGroundItems();
		Map<Long, StructureData> structures = this.getStructures();
		MapLayerView layer = this.getLayer();
		
		// Reset the tile data
		layer.clearEntities();
		
		// Sift through the worlds creatures
		for (Map.Entry<Long, CreatureCellRenderable> list : creatures.entrySet()) {
			EntityType type;
			CreatureCellRenderable creature = list.getValue();
			
			// Skip if the creature does not exist, is ignored, or is at a different plane
			if (creature == null || ((type = EntityType.getByModelName(creature.getModelName())) == null) || (this.playerLayer != MapLayer.getByElevation(creature.getLayer())))
				continue;
			
			TileEntityData entityData = new TileEntityData(type,creature);
			
			// Save the entity to the tile
			layer.addToTile( entityData.getPos(), entityData );
		}
		
		// Iterate through the worlds ground items
		for (Map.Entry<Long, GroundItemCellRenderable> list : groundItems.entrySet()) {
			EntityType type;
			GroundItemCellRenderable groundItem = list.getValue();
			GroundItemData groundItemData = GroundItems.getData( groundItem );
			
			if (groundItemData == null || ((type = EntityType.getByModelName(groundItemData.getModelName())) == null) || (this.playerLayer != MapLayer.getByElevation(groundItem.getLayer())))
				continue;
			
			TileEntityData entityData = new TileEntityData(type, groundItemData);
			
			// Save the entity to the tile
			layer.addToTile(entityData.getPos(), entityData);
		}*/
		
		// Iterate structures
		/*for (Map.Entry<Long, StructureData> list : structures.entrySet()) {
			AbstractTileType type;
			StructureData structure = list.getValue();
			
			// Skip if the structure does not exist, or is ignored
			if (structure == null || ((type = AbstractTileType.getByClassName(structure)) == null))
				continue;
			final Collection<StructureData> structureParts;
			
			// Convert the data to a list
			if (structure instanceof BridgeData)
				structureParts = new ArrayList<>(((BridgeData) structure).getBridgeParts().values());
			else structureParts = Collections.singleton(structure);
			
			// For each part of the structure
			for (StructureData part : structureParts) {
				TileStructureData structureData = new TileStructureData(type, structure.getHoverName(), part);
				
				// Save the structure to the tile
				layer.addToTile(structureData.getPos(), structureData);
			}
		}*/
	}
	
	/*
	 * Automatic Game Listeners
	 */
	@Override
	public void caveChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean) {
		this.markDirty();
	}
	
	@Override
	public void terrainUpdated(int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean1, boolean paramBoolean2) {
		this.markDirty();
	}
	
	/*
	 * Map Colors
	 */
	public final Color terrainColor(Tile tile, Coordinate pos, Function<Tile, Color> function) {
		Color color = function.apply( tile == null ? Tile.TILE_DIRT : tile );
		
		Server server = Servers.getServer();
		if (server instanceof SklotopolisServer) {
			// Color highways
			if (LiveMap.SHOW_ROADS && ((SklotopolisServer) server).isHighway( pos ))
				return color.brighter();
		}
		
		return color;
	}
	public final Color entityColor(TileEntityData entity) {
		return entity.getColor();
	}
	
	/*
	 * Mouse Hinting
	 */
	public void tooltip(final PickData pickData, final float xMouse, final float yMouse) {
		this.getLayer()
			.tooltip(this, pickData, xMouse, yMouse);
	}
	public Coordinate mousePosToCoordinate(final float xMouse, final float yMouse) {
		return this.getLayer()
			.mousePosToCoordinate(this,xMouse, yMouse);
	}
	public Coordinate mousePosToCoordinate(Coordinate mapCenter, final float xMouse, final float yMouse) {
		return this.getLayer()
			.mousePosToCoordinate(mapCenter, xMouse, yMouse);
	}
	
	/*
	 * Get World Objects
	 */
	/*public final Optional<AbstractTileData> getHighestAt( Coordinate pos ) {
		return Optional.ofNullable(this.getLayer().getStructureLayer( pos ).getTop());
	}*/
	/*public final List<TileEntityData> getEntitiesAt( Coordinate pos ) {
		List<TileEntityData> list = new ArrayList<>();
		TileData tile = this.getLayer().getStructureLayer( pos );
		
		if (LiveMap.SHOW_SELF && this.getPlayerPosition().equals( pos ))
			list.add(new TilePlayerData( this.world.getPlayer() ));
		if (tile != null) {
			// Sift out the players from the tile
			if (LiveMap.SHOW_PLAYERS)
				list.addAll(tile.getPlayers());
			// Sift out the vehicles from the tile
			if (LiveMap.SHOW_VEHICLES)
				list.addAll(tile.getVehicles());
			// Sift out the hostiles from the tile
			if (LiveMap.SHOW_HOSTILES)
				list.addAll(tile.getMonsters());
			// Sift out the creatures from the tile
			if (LiveMap.SHOW_CREATURES)
				list.addAll(tile.getCreatures());
			list.addAll(tile.getObjects());
		}
		
		return list;
	}*/
	/*public final List<TileStructureData> getStructuresAt(Coordinate worldPos) {
		List<TileStructureData> list = new ArrayList<>();
		TileData tile = this.getLayer().getStructureLayer( worldPos );
		
		if (tile != null) {
			// Sift out the buildings from the tile
			if (LiveMap.SHOW_BUILDINGS)
				list.addAll(tile.getBuildings());
			// Sift out the bridges from the tile
			list.addAll(tile.getBridges());
		}
		
		return list;
	}*/
	/*private Map<Long, CreatureCellRenderable> getCreatures() {
		return this.ref_Creatures;
	}
	private Map<Long, GroundItemCellRenderable> getGroundItems() {
		return this.ref_GroundItems;
	}
	private Map<Long, StructureData> getStructures() {
		return this.ref_Structures;
	}*/
	
	public static Coordinate getMapOffset(Coordinate pos) {
		return pos.sub(LiveMap.FIRST_MAP_SQUARE);
	}
	public static Coordinate snapNearestBorder(Coordinate pos) {
		return LiveMap.MAP_BORDER.snap(pos);
	}
	public static boolean isWithinMap(Coordinate pos) {
		return LiveMap.MAP_BORDER.isWithin(pos);
	}
	public static boolean isWithinPlayerView(Coordinate pos) {
		return LiveMap.PLAYER_BORDER.isWithin(pos);
	}
	
	/**
	 * @return Executor Thread for reading MapTiles
	 */
	private static ListeningExecutorService getMapLoader() {
		return LiveMap.MAP_TILE_LOADER;
	}
	public static <T> ListenableFuture<T> threadExecute(Callable<T> callable) {
		return LiveMap.getMapLoader().submit(callable);
	}
	public static ListenableFuture<?> threadExecute(Runnable runnable) {
		return LiveMap.getMapLoader().submit(runnable);
	}
	
	static {
		MAP_TILE_LOADER = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(LiveMapConfig.THREAD_COUNT));
		
		try {
			PROCESS_IMAGE_METHOD = ReflectionUtil.getMethod(TextureLoader.class, "preprocessImage", new Class[] { BufferedImage.class, boolean.class });
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
