package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CaveColors {
    private static final Map<Tile, Color> TILE_MAP = new HashMap<>();
    
    static {
        addMapping(Tile.TILE_CAVE_WALL, Color.DARK_GRAY.darker());
        addMapping(Tile.TILE_CAVE_WALL_REINFORCED, Color.DARK_GRAY);
        
        addMapping(Tile.TILE_CAVE_WALL_MARBLE_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_POTTERY_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_ROUNDED_STONE_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_SANDSTONE_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_SLATE_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_STONE_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_WOOD_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_RENDERED_REINFORCED, Color.DARK_GRAY);
        
        addMapping(Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED, Color.DARK_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED, Color.DARK_GRAY);
        
        addMapping(Tile.TILE_CAVE, Color.PINK);
        addMapping(Tile.TILE_CAVE_FLOOR_REINFORCED, Color.PINK);
        addMapping(Tile.TILE_CAVE_PREPATED_FLOOR_REINFORCED, Color.PINK);
        
        addMapping(Tile.TILE_CAVE_EXIT, Color.BLACK);
        addMapping(Tile.TILE_CAVE_WALL_ORE_IRON, Color.RED.darker());
        addMapping(Tile.TILE_CAVE_WALL_LAVA, Color.RED);
        addMapping(Tile.TILE_CAVE_WALL_ORE_COPPER, Color.GREEN);
        addMapping(Tile.TILE_CAVE_WALL_ORE_TIN, Color.GRAY);
        addMapping(Tile.TILE_CAVE_WALL_ORE_GOLD, Color.YELLOW.darker());
        addMapping(Tile.TILE_CAVE_WALL_ORE_ADAMANTINE, Color.CYAN);
        addMapping(Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL, Color.YELLOW.brighter());
        addMapping(Tile.TILE_CAVE_WALL_ORE_SILVER, Color.LIGHT_GRAY);
        addMapping(Tile.TILE_CAVE_WALL_ORE_LEAD, Color.PINK.darker().darker());
        addMapping(Tile.TILE_CAVE_WALL_ORE_ZINC, new Color(235, 235, 235));
        addMapping(Tile.TILE_CAVE_WALL_SLATE, Color.BLACK);
        addMapping(Tile.TILE_CAVE_WALL_MARBLE, Color.WHITE);
        addMapping(Tile.TILE_CAVE_WALL_SANDSTONE, Color.YELLOW.darker().darker());
        addMapping(Tile.TILE_CAVE_WALL_ROCKSALT, Color.WHITE.darker());
    }
    
    private static void addMapping(Tile tile, Color color) {
        TILE_MAP.put(tile, color);
    }
    
    public static Color getColorFor(Tile tile) {
        Color color = TILE_MAP.getOrDefault(tile, Color.PINK);
        if (tile.isRoad() && LiveMap.SHOW_ROADS)
            return color.brighter();
        return color;
    }
    
    protected static Map<Tile, Color> getMappings() {
        return Collections.unmodifiableMap(TILE_MAP);
    }
}
