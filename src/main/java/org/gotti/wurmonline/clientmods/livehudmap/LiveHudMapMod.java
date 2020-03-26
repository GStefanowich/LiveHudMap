package org.gotti.wurmonline.clientmods.livehudmap;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.client.renderer.gui.HudSettings;
import com.wurmonline.client.renderer.gui.WindowSerializer;
import com.wurmonline.client.util.Computer;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;
import org.gotti.wurmunlimited.modsupport.console.ConsoleListener;
import org.gotti.wurmunlimited.modsupport.console.ModConsole;

import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.gui.HeadsUpDisplay;
import com.wurmonline.client.renderer.gui.LiveMapWindow;
import com.wurmonline.client.renderer.gui.MainMenu;
import com.wurmonline.client.renderer.gui.WurmComponent;
import com.wurmonline.client.settings.SavePosManager;

public class LiveHudMapMod implements WurmClientMod, Initable, Configurable, ConsoleListener {
	
	private static HeadsUpDisplay HUD = null;
	private static final Logger LOGGER = Logger.getLogger(LiveHudMapMod.class.getName());
	
	public static boolean USE_HIGH_RES_MAP = false;
	public static boolean SHOW_HIDDEN_ORE = true;
	
	private Object liveMap = null;
	
	@Override
	public void configure(Properties properties) {
		USE_HIGH_RES_MAP = Boolean.parseBoolean(properties.getProperty("hiResMap", String.valueOf(USE_HIGH_RES_MAP)));
		SHOW_HIDDEN_ORE = Boolean.parseBoolean(properties.getProperty("showHiddenOre", String.valueOf(SHOW_HIDDEN_ORE)));
		
		LOGGER.log(Level.ALL, "hiResMap: " + USE_HIGH_RES_MAP);
		LOGGER.log(Level.ALL, "showHiddenOre: " + SHOW_HIDDEN_ORE);
	}
	
	@Override
	public void init() {
		// com.wurmonline.client.renderer.gui.HeadsUpDisplay.init(int, int)
		HookManager.getInstance().registerHook("com.wurmonline.client.renderer.gui.HeadsUpDisplay", "init", "(II)V",
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
	 */
	private void initLiveMap( final HeadsUpDisplay hud ) {
		LiveHudMapMod.HUD = hud;
		
		// Sandbox the code into a runnable to prevent crashing of the main thread
		((Runnable) () -> {
			try {
				// Get the WORLD from the HUD
				World world = ReflectionUtil.getPrivateField(LiveHudMapMod.HUD, ReflectionUtil.getField(LiveHudMapMod.HUD.getClass(), "world"));
				
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
			} catch (IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}).run();
	}
	
	/**
	 * On console input, if the user inputs "Toggle Livemap", toggle the live map.
	 * @param string The console input
	 * @param silent If the output should be silent
	 * @return If the command was a roaring success
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
	
	public static void log( String log ) {
		if (HUD != null) HUD.consoleOutput( log );
	}
	public static void log( Throwable e ) {
		LOGGER.log( Level.WARNING, e.getMessage(), e );
	}
	
}
