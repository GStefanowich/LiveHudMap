package org.gotti.wurmonline.clientmods.livehudmap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import com.wurmonline.client.game.PlayerObj;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Sklotopolis;
import org.gotti.wurmonline.clientmods.livehudmap.assets.SklotopolisServer;
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
	public static boolean SHOW_VEHICLES = true;
	public static boolean SHOW_BUILDINGS = true;
	public static boolean SHOW_ROADS = true;
	public static boolean SHOW_ORES = true;
	
	private int size;
	
	private final World world;
	private SklotopolisServer server;
	
	private MapLayer playerLayer = this.updateLayer();
	private MapLayerView surface;
	private MapLayerView cave;
	
	private boolean dirty = true;
	private BufferedImage image;
	private ImageTexture texture;
	private final Function<String, Boolean> onUpdate;
	
	private int windowX;
	private int windowY;
	
	private int playerX = 0, playerY = 0;
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
		
		this.surface = new MapLayerView(world, RenderType.FLAT);
		this.cave = new MapLayerView(world, RenderType.CAVE);
		
		this.onUpdate = callable;
	}
	
	// Tick update
	public void update(int windowX, int windowY) {
		this.windowX = windowX;
		this.windowY = windowY;
		
		// Check the server the player is on
		if (this.server == null)
			this.server = Sklotopolis.getServerByName(this.world.getServerName());
		
		PlayerObj player = this.world.getPlayer();
		PlayerPosition pos = player.getPos();
		float newRot = pos.getXRot(0);
		
		if ( !this.dirty && this.playerX != pos.getTileX() || this.playerY != pos.getTileY() || this.rotation != newRot )
			this.dirty = true;
		
		if ( this.dirty ) {
			// Check the players layer
			this.playerLayer = this.updateLayer();
			
			// Get the players location
			this.playerX = pos.getTileX();
			this.playerY = pos.getTileY();
			this.rotation = newRot;
			
			this.image = this.applyRotation(this.getLayer()
				.render( this.playerX, this.playerY ), -this.rotation);
			
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
			this.onUpdate.apply((this.server != null ? this.server.getName() + ": " : "") + this.windowX + ", " + this.windowY + " (" + this.getLayer().getZoom() + "x)");
		this.dirty = false;
	}
	private MapLayer updateLayer() {
		return this.isSurface() ? MapLayer.SURFACE : MapLayer.CAVE;
	}
	
	// Get the current layer that the player is on
	private MapLayerView getLayer() {
		return this.getLayer( this.playerLayer );
	}
	private MapLayerView getLayer(MapLayer layer) {
		switch (layer) {
			case SURFACE:
				return surface;
			case CAVE:
				return cave;
		}
		throw new IllegalArgumentException("Unknown layer: " + layer.name());
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
		this.dirty = true;
	}
	
	// Change the renderer type
	public void setRenderer(MapLayer layer, RenderType renderType) {
		this.getLayer( layer )
			.setRenderer(renderType);
		this.markDirty();
	}
	public RenderType getRenderer() {
		return this.getLayer( this.playerLayer )
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
	
	private boolean isSurface() {
		if (this.world == null)
			return true;
		return this.world.getPlayerLayer() >= 0;
	}
	
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
	
	@Override
	public void caveChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean) {
		this.markDirty();
	}
	
	@Override
	public void terrainUpdated(int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean1, boolean paramBoolean2) {
		this.markDirty();
	}
	
	public void pick(final PickData pickData, final float xMouse, final float yMouse) {
		this.getLayer().pick(pickData, xMouse, yMouse);
	}
	
	static {
		try {
			PROCESS_IMAGE_METHOD = ReflectionUtil.getMethod(TextureLoader.class, "preprocessImage", new Class[] { BufferedImage.class, boolean.class });
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
