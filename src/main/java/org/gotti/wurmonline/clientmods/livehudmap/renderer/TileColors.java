package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.renderer.Material;
import com.wurmonline.client.renderer.structures.FloorData;
import com.wurmonline.client.renderer.structures.RoofData;
import com.wurmonline.client.renderer.structures.WallData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.shared.constants.BridgeConstants.BridgeMaterial;
import com.wurmonline.shared.constants.StructureConstants;
import com.wurmonline.shared.constants.StructureConstants.FloorMaterial;
import org.gotti.wurmonline.clientmods.livehudmap.reflection.Structures;

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
    
    public static Color getColorFor(FloorMaterial material) {
        switch (material) {
            case WOOD:
                return new Color(133, 94, 66);
            case STONE_BRICK:
            case STONE_SLAB:
                return Tile.TILE_STONE_SLABS.getColor();
            case SANDSTONE_SLAB:
                return Tile.TILE_SANDSTONE_BRICKS.getColor();
            case SLATE_SLAB:
                return Tile.TILE_SLATE_BRICKS.getColor();
            case THATCH:
                return Color.ORANGE;
            case METAL_IRON:
                return Tile.TILE_CAVE_WALL_ORE_IRON.getColor();
            case METAL_STEEL:
                return Color.ORANGE;
            case METAL_COPPER:
                return Tile.TILE_CAVE_WALL_ORE_COPPER.getColor();
            case CLAY_BRICK:
                return new Color(203, 65, 84);
            case METAL_GOLD:
                return Tile.TILE_CAVE_WALL_ORE_GOLD.getColor();
            case METAL_SILVER:
                return Tile.TILE_CAVE_WALL_ORE_SILVER.getColor();
            case MARBLE_SLAB:
                return Tile.TILE_MARBLE_SLABS.getColor();
            case STANDALONE:
                return Color.ORANGE;
        }
        return Color.WHITE;
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
    public static Color getColorFor(RoofData roof) {
        return TileColors.getColorFor(Structures.getMaterial( roof ));
    }
    public static Color getColorFor(FloorData floor) {
        return TileColors.getColorFor(Structures.getMaterial( floor ));
    }
    
    protected static Map<Tile, Color> getMappings() {
        return Collections.unmodifiableMap(TILE_MAP);
    }
}
