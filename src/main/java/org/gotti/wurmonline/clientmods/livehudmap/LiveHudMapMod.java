package org.gotti.wurmonline.clientmods.livehudmap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.client.renderer.gui.HudSettings;
import com.wurmonline.client.renderer.gui.WindowSerializer;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.LiveMapConfig;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;
import org.gotti.wurmunlimited.modsupport.console.ConsoleListener;
import org.gotti.wurmunlimited.modsupport.console.ModConsole;

import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.gui.HeadsUpDisplay;
import com.wurmonline.client.renderer.gui.LiveMapWindow;
import com.wurmonline.client.renderer.gui.WurmComponent;
import com.wurmonline.client.settings.SavePosManager;

public class LiveHudMapMod implements WurmClientMod, Initable, Configurable, ConsoleListener {
	
	private static HeadsUpDisplay HUD = null;
	private static final Logger LOGGER = Logger.getLogger(LiveHudMapMod.class.getName());
	
	public static final String MOD_NAME = "livemap";
	public static final Path MOD_FOLDER = Paths.get("mods", LiveHudMapMod.MOD_NAME);
	
	private Object liveMap = null;
	
	@Override
	public void configure(Properties properties) {
		LiveMapConfig.HIGH_RES_MAP    = LiveMapConfig.parse(properties, "hiResMap", LiveMapConfig::parseBoolean);
		LiveMapConfig.SHOW_HIDDEN_ORE = LiveMapConfig.parse(properties, "showHiddenOre", LiveMapConfig::parseBoolean);
		LiveMapConfig.MAP_TILE_SIZE   = LiveMapConfig.parse(properties, "mapTileSize", LiveMapConfig::parseInt);
		LiveMapConfig.SAVE_SECONDS    = LiveMapConfig.parse(properties, "mapCacheSeconds", LiveMapConfig::parseInt);
		LiveMapConfig.THREAD_COUNT    = LiveMapConfig.parse(properties, "maxThreadCount", LiveMapConfig::parseInt);
	}
	
	@Override
	public void init() {
		HookManager hooks = HookManager.getInstance();
		
		// com.wurmonline.client.renderer.gui.HeadsUpDisplay.init(int, int)
		hooks.registerHook("com.wurmonline.client.renderer.gui.HeadsUpDisplay", "init", "(II)V",
			() -> (proxy, method, args) -> {
				method.invoke(proxy, args);
				
				this.initLiveMap((HeadsUpDisplay) proxy);
				
				return null;
			}
		);
		
		// Add the ConsoleListener to the console
		ModConsole.addConsoleListener( this );
	}
	
	/**
	 * Initialize the livemap
	 * @param hud The players HUD (Heads up Display)
	 * @author Ago1024   
	 */
	private void initLiveMap( final HeadsUpDisplay hud ) {
		LiveHudMapMod.HUD = hud;
		
		LiveMap.threadExecute(() -> {
			try {
				// Get the WORLD from the HUD
				World world = ReflectionUtil.getPrivateField(LiveHudMapMod.HUD, ReflectionUtil.getField(LiveHudMapMod.HUD.getClass(), "world"));
				
				// Wait for the connection to become active
				while (world.getServerConnection() == null)
					Thread.sleep( 1000 );
				
				// Create the livemap window
				this.liveMap = new LiveMapWindow( world );
				
				// Create the button component to open/close the map
				HudSettings mainMenu = ReflectionUtil.getPrivateField(LiveHudMapMod.HUD, ReflectionUtil.getField(LiveHudMapMod.HUD.getClass(), "hudSettings"));
				mainMenu.registerComponent("Live map", (WurmComponent) this.liveMap);
				
				// Register the component
				List<WurmComponent> components = ReflectionUtil.getPrivateField(LiveHudMapMod.HUD, ReflectionUtil.getField(LiveHudMapMod.HUD.getClass(), "components"));
				components.add((WurmComponent) this.liveMap);
				
				// Create the position manager to maintain the location of the livemap
				SavePosManager savePosManager = ReflectionUtil.getPrivateField(LiveHudMapMod.HUD, ReflectionUtil.getField(LiveHudMapMod.HUD.getClass(), "savePosManager"));
				savePosManager.registerAndRefresh((WindowSerializer) this.liveMap, "livemapwindow");
			} catch (IllegalAccessException | NoSuchFieldException | InterruptedException e) {
				throw new RuntimeException( e );
			}
		});
	}
	
	/**
	 * On console input, if the user inputs "Toggle Livemap", toggle the live map.
	 * @param string The console input
	 * @param silent If the output should be silent
	 * @return If the command was a roaring success
	 * @author Ago1024
	 */
	@Override
	public boolean handleInput( String string, Boolean silent ) {
		if ( string != null ) {
			boolean success;
			if (success = (string.equalsIgnoreCase("toggle livemap") && this.liveMap instanceof LiveMapWindow))
				((LiveMapWindow) this.liveMap).toggle();
			return success;
		}
		return false;
	}
	
	public static void log(String log) {
		LOGGER.log(Level.INFO, log);
	}
	public static void log(Coordinate pos) {
		LiveHudMapMod.log("Pos: " + pos.getX() + ", " + pos.getY());
	}
	public static void log(Throwable e) {
		LOGGER.log(Level.WARNING, e.getMessage(), e);
	}
	public static void log(Object obj) {
		LiveHudMapMod.log(String.valueOf(obj));
	}
	
	/**
	 * @return Get the Name of the Server the player is connected to
	 */
	public static String getServerName() {
		return LiveHudMapMod.HUD.getWorld().getServerName();
	}
	
	public static void openDeedFinderWindow() {
	}
	public static void openDeedRouteWindow() {
	}
}
