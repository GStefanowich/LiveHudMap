package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TileColors {
    private static final Map<Tile, Color> TILE_MAP = new HashMap<>();
    
    static {
        addMapping(Tile.TILE_FIELD, new Color(34,139,34));
        addMapping(Tile.TILE_FIELD2, new Color(34,139,34));
    }
    
    private static void addMapping(Tile tile, Color color) {
        TILE_MAP.put(tile, color);
    }
    
    public static Color getColorFor(Tile tile) {
        Color color = TILE_MAP.getOrDefault(tile, tile.getColor());
        if (tile.isRoad() && LiveMap.SHOW_ROADS)
            return color.brighter();
        return color;
    }
    
    protected static Map<Tile, Color> getMappings() {
        return Collections.unmodifiableMap(TILE_MAP);
    }
}
