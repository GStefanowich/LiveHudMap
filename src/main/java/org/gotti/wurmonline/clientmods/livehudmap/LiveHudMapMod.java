package org.gotti.wurmonline.clientmods.livehudmap;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wurmonline.client.renderer.gui.WindowSerializer;
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

	private static final Logger LOGGER = Logger.getLogger(LiveHudMapMod.class.getName());
	
	public static boolean USE_HIGH_RES_MAP = false;
	public static boolean SHOW_HIDDEN_ORE = true;
	
	public static int INT_VAL = 0;
	
	private Object liveMap = null;
	
	@Override
	public void configure(Properties properties) {
		USE_HIGH_RES_MAP = Boolean.parseBoolean(properties.getProperty("hiResMap", String.valueOf(USE_HIGH_RES_MAP)));
		SHOW_HIDDEN_ORE = Boolean.parseBoolean(properties.getProperty("showHiddenOre", String.valueOf(SHOW_HIDDEN_ORE)));
		
		LOGGER.log(Level.INFO, "hiResMap: " + USE_HIGH_RES_MAP);
		LOGGER.log(Level.INFO, "showHiddenOre: " + SHOW_HIDDEN_ORE);
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
	private void initLiveMap(final HeadsUpDisplay hud) {
		// Sandbox the code into a runnable to prevent crashing of the main thread
		((Runnable) () -> {
			try {
				World world = ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "world"));
				
				this.liveMap = new LiveMapWindow( world );
				
				MainMenu mainMenu = ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "mainMenu"));
				mainMenu.registerComponent("Live map", (WurmComponent) this.liveMap);
				
				List<WurmComponent> components = ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "components"));
				components.add((WurmComponent) this.liveMap);
				
				SavePosManager savePosManager = ReflectionUtil.getPrivateField(hud, ReflectionUtil.getField(hud.getClass(), "savePosManager"));
				savePosManager.registerAndRefresh((WindowSerializer) this.liveMap, "livemapwindow");
			} catch (IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}).run();
	}
	
	/**
	 * On console input, if the user inputs "Toggle Livemap", toggle the live map.
	 * @param string The console input
	 * @param silent
	 * @return If the command was a roaring success
	 */
	@Override
	public boolean handleInput(String string, Boolean silent) {
		if ( string != null ) {
			boolean success;
			if (success = (string.equalsIgnoreCase("toggle livemap") && this.liveMap instanceof LiveMapWindow))
				((LiveMapWindow) this.liveMap).toggle();
			
			if (!success && string.startsWith("i ")){
				INT_VAL = Integer.parseInt(string.substring( "i ".length() ));
				success = true;
			}
			
			return success;
		}
		return false;
	}
	
}
