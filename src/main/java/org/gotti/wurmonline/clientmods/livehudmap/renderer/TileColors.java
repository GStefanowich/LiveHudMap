package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.BridgeConstants.BridgeMaterial;

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
        return TILE_MAP.getOrDefault(tile, tile.getColor());
    }
    public static Color getColorFor(BridgeMaterial material) {
        switch (material) {
            case ROPE:
                return new Color(139,69,19);
            case MARBLE:
                return Tile.TILE_MARBLE_SLABS.getColor();
            case WOOD:
                return Tile.TILE_CAVE_WALL_WOOD_REINFORCED.getColor();
            case ROUNDED_STONE:
                return Tile.TILE_CAVE_WALL_ROUNDED_STONE_REINFORCED.getColor();
            case BRICK:
            case POTTERY:
                return Tile.TILE_POTTERY_BRICKS.getColor();
            case SANDSTONE:
                return Tile.TILE_SANDSTONE_SLABS.getColor();
            case RENDERED:
                return Tile.TILE_CAVE_WALL_RENDERED_REINFORCED.getColor();
            case SLATE:
            default:
                return Tile.TILE_SLATE_SLABS.getColor();
        }
    }
    
    protected static Map<Tile, Color> getMappings() {
        return Collections.unmodifiableMap(TILE_MAP);
    }
}
