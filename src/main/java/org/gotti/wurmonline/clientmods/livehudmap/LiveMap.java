package org.gotti.wurmonline.clientmods.livehudmap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.wurmonline.client.comm.ServerConnectionListenerClass;
import com.wurmonline.client.renderer.GroundItemData;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;
import com.wurmonline.client.renderer.cell.GroundItemCellRenderable;
import com.wurmonline.client.renderer.structures.BridgeData;
import com.wurmonline.client.renderer.structures.StructureData;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.StructureType;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.EntityType;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Sklotopolis;
import org.gotti.wurmonline.clientmods.livehudmap.assets.SklotopolisServer;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileStructureData;
import org.gotti.wurmonline.clientmods.livehudmap.reflection.GroundItems;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.RenderType;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import com.wurmonline.client.game.PlayerPosition;
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

public class LiveMap implements TerrainChangeListener, CaveBufferChangeListener {
	
	private static final Method PROCESS_IMAGE_METHOD;
	
	public static boolean ALWAYS_NORTH = true;
	public static boolean SHOW_SELF = true;
	public static boolean SHOW_DEEDS = true;
	public static boolean SHOW_PLAYERS = true;
	public static boolean SHOW_CREATURES = true;
	public static boolean SHOW_HOSTILES = true;
	public static boolean SHOW_VEHICLES = true;
	public static boolean SHOW_BUILDINGS = false;
	public static boolean SHOW_ROADS = false;
	public static boolean SHOW_ORES = true;
	
	public static int REFRESH_RATE = ( LiveHudMapMod.USE_HIGH_RES_MAP ? 3 : 7 );
	
	private int dirtyTimer = 0;
	private int size;
	
	private final World world;
	private SklotopolisServer server = null;
	
	private MapLayer playerLayer = this.updateLayer();
	
	private boolean dirty = true;
	private BufferedImage image;
	private ImageTexture texture;
	private final Function<String, Boolean> onUpdate;
	
	private int windowX;
	private int windowY;
	
	private Coordinate playerPos = Coordinate.zero();
	private float rotation = 0;
	
	public LiveMap(World world, int size) {
		this( world, size, null );
	}
	public LiveMap(World world, int size, Function<String, Boolean> callable) {
		this.size = size;
		
		this.world = world;
		this.server = null;
		
		this.world.getNearTerrainBuffer().addListener(this);
		this.world.getCaveBuffer().addCaveBufferListener(this);
		
		this.onUpdate = callable;
	}
	
	// Tick update
	public void update(int windowX, int windowY) {
		this.windowX = windowX;
		this.windowY = windowY;
		
		// Check the server the player is on
		if (this.server == null) {
			// Get the server
			this.server = Sklotopolis.getServerByName(this.world.getServerName());
			
			// Initialize the servers deeds
			if (this.server != null) this.server.initialize( this.world );
		}
		
		PlayerPosition pos = this.world.getPlayer().getPos();
		Coordinate currentPosition = Coordinate.of(pos);
		
		float newRot = pos.getXRot(0);
		
		// Check if the map should be considered dirty
		if ((!this.dirty && (!this.playerPos.equals( currentPosition )) || ( !ALWAYS_NORTH && this.rotation != newRot )) || ( this.dirtyTimer <= 0 ))
			this.markDirty();
		
		// Update the layer
		if (this.getLayer().tick())
			this.updateEntities();
		
		if ( !this.dirty )
			this.dirtyTimer--;
		else {
			// Check the players layer
			this.playerLayer = this.updateLayer();
			
			// Get the players location
			this.playerPos = currentPosition;
			this.rotation = newRot;
			
			this.image = this.applyRotation(this.getLayer()
				.render( this, this.playerPos ), -this.rotation);
			
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
		if (this.onUpdate != null)
			this.onUpdate.apply((this.server != null ? this.server.getName() + ": " : "") + this.playerPos.getX() + ", " + this.playerPos.getY() + " (" + this.getLayer().getZoom() + "x)");
		this.dirty = false;
	}
	private MapLayer updateLayer() {
		return this.world == null ? MapLayer.SURFACE : MapLayer.getByElevation(this.world.getPlayerLayer());
	}
	
	// Get the player information
	public SklotopolisServer getServer() {
		return this.server;
	}
	public World getWorld() {
		return this.world;
	}
	public Coordinate getPlayerPos() {
		return this.playerPos;
	}
	
	public void initializeServer() {
		if (this.server != null)
			this.server.initialize(this.world);
	}
	
	// Get the current layer that the player is on
	private MapLayerView getLayer() {
		return this.playerLayer.getMap();
	}
	
	// Apply rotation based on the way the player is facing
	private BufferedImage applyRotation( BufferedImage image, float angle ) {
		// If rotation is disabled
		if ( ALWAYS_NORTH ) return image;
		
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
	public void toggleTrueNorth() {
		ALWAYS_NORTH = !ALWAYS_NORTH;
		this.markDirty();
	}
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
	}
	public void toggleShowHostiles() {
		SHOW_HOSTILES = !SHOW_HOSTILES;
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
		// Get the creatures from the world
		Map<Long, CreatureCellRenderable> creatures = this.getCreatures();
		Map<Long, GroundItemCellRenderable> groundItems = this.getGroundItems();
		Map<Long, StructureData> structures = this.getStructures();
		MapLayerView layer = this.getLayer();
		
		// Reset the tile data
		layer.clearTiles();
		
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
		}
		
		// Iterate structures
		for (Map.Entry<Long, StructureData> list : structures.entrySet()) {
			StructureType type;
			StructureData structure = list.getValue();
			
			// Skip if the structure does not exist, or is ignored
			if (structure == null || ((type = StructureType.getByClassName( structure )) == null))
				continue;
			final Collection<StructureData> structureParts;
			
			// Convert the data to a list
			if (structure instanceof BridgeData)
				structureParts = new ArrayList<>(((BridgeData)structure).getBridgeParts().values());
			else structureParts = Collections.singleton( structure );
			
			// For each part of the structure
			for (StructureData part : structureParts) {
				TileStructureData structureData = new TileStructureData(type, structure.getHoverName(), part);
				
				// Save the structure to the tile
				layer.addToTile(structureData.getPos(), structureData);
			}
		}
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
	public final Color tileColor(Tile tile, Coordinate pos, Function<Tile, Color> function) {
		// Color deed borders
		if (this.server != null && LiveMap.SHOW_DEEDS && this.server.isDeedBorder( pos ))
			return ( this.getRenderer() == RenderType.CAVE ? Color.GRAY : Color.ORANGE );
		
		Color color = function.apply( tile == null ? Tile.TILE_DIRT : tile );
		
		if (this.server != null) {
			// Color highways
			if (LiveMap.SHOW_ROADS && this.server.isHighway( pos ))
				return color.brighter();
			
			// Get information on the tile
			TileData tileData = this.getLayer().getTile(pos);
			if (tileData != null) {
				StructureType top = tileData.topStructure();
				if (top == StructureType.BRIDGE) {
					// Color bridges
					List<TileStructureData> bridges = tileData.getBridges();
					if (!bridges.isEmpty())
						return bridges.get(0).getColor();
				} else if (top == StructureType.HOUSE) {
					// Color houses
					
				}
			}
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
		return this.getLayer().mousePosToCoordinate(xMouse, yMouse);
	}
	
	/*
	 * Get World Objects
	 */
	public final List<TileEntityData> getEntitiesAt(Coordinate worldPos) {
		List<TileEntityData> list = new ArrayList<>();
		TileData tile = this.getLayer().getTile( worldPos );
		
		if (LiveMap.SHOW_SELF && this.playerPos.equals( worldPos ))
			list.add(new TileEntityData( this.world.getPlayer() ));
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
	}
	public final List<TileStructureData> getStructuresAt(Coordinate worldPos) {
		List<TileStructureData> list = new ArrayList<>();
		TileData tile = this.getLayer().getTile( worldPos );
		
		if (tile != null) {
			// Sift out the buildings from the tile
			if (LiveMap.SHOW_BUILDINGS)
				list.addAll(tile.getBuildings());
			// Sift out the bridges from the tile
			list.addAll(tile.getBridges());
		}
		
		return list;
	}
	private Map<Long, CreatureCellRenderable> getCreatures() {
		if (this.world == null)
			return Collections.emptyMap();
		return this.world.getServerConnection()
			.getServerConnectionListener().getCreatures();
	}
	private Map<Long, GroundItemCellRenderable> getGroundItems() {
		ServerConnectionListenerClass listener = this.world.getServerConnection()
			.getServerConnectionListener();
		
		try {
			
			Field field = ReflectionUtil.getField(ServerConnectionListenerClass.class, "groundItems");
			return ReflectionUtil.getPrivateField(listener, field);
			
		} catch (IllegalAccessException | NoSuchFieldException e) {
			LiveHudMapMod.log( e );
			return Collections.emptyMap();
		}
	}
	private Map<Long, StructureData> getStructures() {
		if (this.world == null)
			return Collections.emptyMap();
		return this.world.getServerConnection()
			.getServerConnectionListener().getStructures();
	}
	static {
		try {
			PROCESS_IMAGE_METHOD = ReflectionUtil.getMethod(TextureLoader.class, "preprocessImage", new Class[] { BufferedImage.class, boolean.class });
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
