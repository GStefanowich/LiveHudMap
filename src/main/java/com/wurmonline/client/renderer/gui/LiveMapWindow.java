package com.wurmonline.client.renderer.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.imageio.ImageIO;

import com.wurmonline.client.util.Computer;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.MapLayer;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.SklotopolisServer;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.RenderType;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import com.wurmonline.client.game.World;
import com.wurmonline.client.options.Options;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.client.resources.textures.ImageTexture;
import com.wurmonline.client.resources.textures.ImageTextureLoader;
import com.wurmonline.client.resources.textures.ResourceTexture;
import com.wurmonline.client.resources.textures.ResourceTextureLoader;

public class LiveMapWindow extends WWindow {
	private final WurmBorderPanel mainPanel;
	private final LiveMap liveMap;
	private final BufferedImage iconImage;
	private final LiveMapView liveMapView;
	private final WurmTextPanel playerPosition;
	
	public LiveMapWindow(World world) {
		super("Live map", true);
		
		this.setTitle( "Live map" );
		
		this.mainPanel = new WurmBorderPanel("Live map", 0, 0 , 256, 256);
		this.playerPosition = new WurmTextPanel("Player Coordinates", false);
		
		this.liveMap = new LiveMap(world, 256, ( val ) -> {
			if ( val == null || val.isEmpty() )
				return false;
			
			this.playerPosition.removeAllLines();
			this.playerPosition.addLine( val );
			
			return true;
		});
		this.resizable = false;
		
		this.iconImage = loadIconImage();
		
		// Create the button panel
		WurmArrayPanel<WButton> buttons = new WurmArrayPanel<>("Live map buttons", WurmArrayPanel.DIR_VERTICAL);
		buttons.setInitialSize(32, 256, false); // Button panel size
		
		// Add the sidebar buttons for zooming
		buttons.addComponent(createButton("+", "Zoom in" , 0, (SimpleButtonListener) p0 -> this.liveMap.zoomIn()));
		buttons.addComponent(createButton("-", "Zoom out" , 1, (SimpleButtonListener) p0 -> this.liveMap.zoomOut()));
		
		// Add the sidebar buttons for changing the map view
		/*buttons.addComponent(createButton("Flat", "Flat view" , 2, (SimpleButtonListener) p0 -> this.liveMap.setRenderer(MapLayer.SURFACE, RenderType.FLAT)));
		buttons.addComponent(createButton("3D", "Pseudo 3D view" , 3, (SimpleButtonListener) p0 -> this.liveMap.setRenderer(MapLayer.SURFACE, RenderType.ISOMETRIC)));
		buttons.addComponent(createButton("Topo", "Topographic view" , 4, (SimpleButtonListener) p0 -> this.liveMap.setRenderer(MapLayer.SURFACE, RenderType.TOPOGRAPHIC)));*/
		
		// Set the map image as the main component
		this.mainPanel.setComponent((this.liveMapView = new LiveMapView("Live map", this.liveMap, 256, 256)), WurmBorderPanel.CENTER);
		
		// Attach the button to the right side of the panel
		this.setComponent( buttons, WurmBorderPanel.EAST );
		
		// Set the LiveMapView as the main component of this element
		this.setComponent( this.mainPanel, WurmBorderPanel.WEST );
		
		// Attach the player coordinates to the bottom of the window
		this.setComponent( this.playerPosition, WurmBorderPanel.NORTH );
		
		this.setInitialSize(this.liveMapView.width + 6 + 32, this.liveMapView.height + 25, false);
		this.layout();
		
		this.sizeFlags = FlexComponent.FIXED_WIDTH | FlexComponent.FIXED_HEIGHT;
	}
	
	private BufferedImage loadIconImage() {
		try {
			URL url = this.getClass().getClassLoader().getResource("livemapicons.png");
			if (url == null && this.getClass().getClassLoader() == HookManager.getInstance().getLoader()) {
				url = HookManager.getInstance().getClassPool().find(LiveMapWindow.class.getName());
				if (url != null) {
					String path = url.toString();
					int pos = path.lastIndexOf('!');
					if (pos != -1) {
						path = path.substring(0, pos) + "!/livemapicons.png";
					}
					url = new URL(path);
				}
			}
			if (url != null) {
				return ImageIO.read(url);
			} else {
				return null;
			}
		} catch (IOException e) {
			LiveHudMapMod.log( e );
			return null;
		}
	}
	
	private WButton createButton(String label, String tooltip, int textureIndex, ButtonListener listener) {
		if (iconImage != null) {
			BufferedImage image = iconImage.getSubimage(textureIndex * 32, 0, 32, 32);
			ImageTexture texture = ImageTextureLoader.loadNowrapNearestTexture(image, false);
			return new LiveMapButton("", tooltip, WurmComponent.LARGE_ICON_SIZE, WurmComponent.LARGE_ICON_SIZE, texture, listener);
		} else {
			final String themeName = Options.guiSkins.options[Options.guiSkins.value()].toLowerCase(Locale.ENGLISH).replace(" ", "");
			final ResourceTexture backgroundTexture = ResourceTextureLoader.getTexture("img.gui.button.mainmenu." + themeName);
			return new WTextureButton(label, tooltip, backgroundTexture, listener);
		}
	}
	
	/**
	 * When the left mouse button is dragged over the map
	 * @param xMouse X mouse position
	 * @param yMouse Y mouse position
	 */
	@Override
	protected void mouseDragged(int xMouse, int yMouse) {
		this.dragger.mouseDragged( xMouse, yMouse );
	}
	
	/**
	 * When the left mouse button is pressed down over the map
	 * @param xMouse X mouse position
	 * @param yMouse Y mouse position
	 * @param clickCount Number of clicks on the map
	 */
	@Override
	protected void leftPressed(int xMouse, int yMouse, int clickCount) {
		this.dragger.leftPressed( xMouse, yMouse, clickCount );
	}
	
	/**
	 * When the left mouse button is released over the map
	 * @param xMouse X mouse position
	 * @param yMouse Y mouse position
	 */
	@Override
	protected void leftReleased(int xMouse, int yMouse) {
		this.dragger.leftReleased( xMouse, yMouse );
	}
	
	/**
	 * @param xMouse X mouse position
	 * @param yMouse Y mouse position
	 * @param clickCount Number of clicks on the map
	 */
	@Override
	protected void rightPressed(final int xMouse, final int yMouse, int clickCount) {
		final SklotopolisServer server = this.liveMap.getServer();
		
		// Offset cursor and window to get tile pos
		final Coordinate tile = this.mousePosToCoordinate( xMouse, yMouse );
		
		// Create a new popup at the mouse location
		WurmPopup popup = new WurmPopup("liveMapHudMenu", "Options", xMouse, yMouse);
		popup.addSeparator();
		
		// Add parent context options
		this.dragger.addContextMenuEntry( popup );
		popup.addButton(SimpleButtonListener.livePopup(popup, "Close window", this::closePressed));
		popup.addSeparator();
		
		// Add URL opener
		if (server != null) {
			String mapURL = server.getMapURL( tile );
			if (mapURL != null) {
				popup.addButton(SimpleButtonListener.livePopup(popup, "Open in Browser", () -> Computer.openURL( mapURL )));
				popup.addButton(SimpleButtonListener.livePopup(popup, "Copy location URL", () -> Computer.setClipboardContents( mapURL )));
				popup.addSeparator();
			}
		}
		
		// Change options of the map
		popup.addButton(SimpleButtonListener.livePopup( popup,SimpleButtonListener.toggleHidden(LiveMap.SHOW_SELF) + " Self", this.liveMap::toggleShowSelf));
		popup.addButton(SimpleButtonListener.livePopup( popup,SimpleButtonListener.toggleHidden(LiveMap.SHOW_DEEDS) + " Deeds", this.liveMap::toggleShowDeeds));
		popup.addButton(SimpleButtonListener.livePopup( popup,SimpleButtonListener.toggleHidden(LiveMap.SHOW_PLAYERS) + " Players", this.liveMap::toggleShowPlayers));
		popup.addButton(SimpleButtonListener.livePopup( popup,SimpleButtonListener.toggleHidden(LiveMap.SHOW_CREATURES) + " Creatures", this.liveMap::toggleShowCreatures));
		popup.addButton(SimpleButtonListener.livePopup( popup,SimpleButtonListener.toggleHidden(LiveMap.SHOW_HOSTILES) + " Hostiles", this.liveMap::toggleShowHostiles));
		popup.addButton(SimpleButtonListener.livePopup( popup,SimpleButtonListener.toggleHidden(LiveMap.SHOW_VEHICLES) + " Vehicles", this.liveMap::toggleShowVehicles));
		popup.addButton(SimpleButtonListener.livePopup( popup,SimpleButtonListener.toggleHidden(LiveMap.SHOW_ROADS) + " Roads", this.liveMap::toggleShowRoads));
		popup.addButton(SimpleButtonListener.livePopup( popup,SimpleButtonListener.toggleHidden(LiveMap.SHOW_BUILDINGS) + " Buildings", this.liveMap::toggleShowBuildings));
		popup.addButton(SimpleButtonListener.livePopup( popup,SimpleButtonListener.toggleHidden(LiveMap.SHOW_ORES) + " Ores", this.liveMap::toggleShowOres));
		
		popup.addSeparator();
		popup.addButton(SimpleButtonListener.livePopup( popup, SimpleButtonListener.toggleEnable(LiveMap.ALWAYS_NORTH) + " True North", this.liveMap::toggleTrueNorth));
		
		RenderType renderType = this.liveMap.getRenderer();
		
		// Change the views of the map
		if (renderType != RenderType.CAVE) {
			popup.addSeparator();
			// Add FLAT button
			if (renderType != RenderType.FLAT)
				popup.addButton(SimpleButtonListener.livePopup(popup, "Flat View", () -> this.liveMap.setRenderer(MapLayer.SURFACE, RenderType.FLAT)));
			// Add ISOMETRIC button
			if (renderType != RenderType.ISOMETRIC)
				popup.addButton(SimpleButtonListener.livePopup(popup, "Isometic View", () -> this.liveMap.setRenderer(MapLayer.SURFACE, RenderType.ISOMETRIC)));
			// Add TOPOGRAPHIC button
			if (renderType != RenderType.TOPOGRAPHIC)
				popup.addButton(SimpleButtonListener.livePopup(popup, "Topographic View", () -> this.liveMap.setRenderer(MapLayer.SURFACE, RenderType.TOPOGRAPHIC)));
		}
		
		WurmComponent.hud.showPopupComponent( popup );
	}
	
	@Override
	public void closePressed() {
		WurmComponent.hud.toggleComponent( this );
	}
	public void toggle() {
		WurmComponent.hud.toggleComponent( this );
	}
	
	@Override
	public void pick(final PickData pickData, final int xMouse, final int yMouse) {
		if (this.liveMapView.contains(xMouse, yMouse)) {
			this.liveMap.tooltip(pickData, 1.0f * (xMouse - this.liveMapView.x) / this.liveMapView.width, 1.0f * (yMouse - this.liveMapView.y) / this.liveMapView.width);
		}
	}
	public Coordinate mousePosToCoordinate(final int xMouse, final int yMouse) {
		return this.liveMap.mousePosToCoordinate(1.0f * (xMouse - this.liveMapView.x) / this.liveMapView.width, 1.0f * (yMouse - this.liveMapView.y) / this.liveMapView.width);
	}
}
