package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.client.renderer.cell.CellRenderer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.AbstractTileData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileRenderLayer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MapRendererIsometric extends AbstractSurfaceRenderer {
	
	public MapRendererIsometric(CellRenderer renderer, NearTerrainDataBuffer buffer ) {
		super( renderer, buffer );
	}
	
	@Override
    public BufferedImage createMapDump(LiveMap map, int leftX, int topY, int imageX, int imageY, Coordinate playerPos) {
        Set<MapTile> grids = new HashSet<>();
        
        // Image should always be a square that covers both lengths
        final int imageDimension = Math.max(imageY, imageX);
        final int y0 = imageY + imageY / 2;
        
        // For X direction
        for (int x = 0; x < imageX; x++) {
            int height = y0 - 1;
            
            // For Y direction
            for (int y = y0 - 1; y >= -imageY / 2 && height >= 0; y--) {
                final Coordinate pos = Coordinate.of( x + leftX, y + topY );
                final MapTile grid = this.getMapTile(pos);
                
                if (grid != null) {
                    grids.add(grid);
                    
                    // Update if the player is within view
                    if (this.canDrawAt(pos)) {
                        // Get the entities on the tile
                        Optional<? extends AbstractTileData> tileData = this.getHigherTile(
                            this.getEntitiesAt(pos,height),
                            this.getStructuresAt(pos, height)
                        );
                        
                        // Get the tile height
                        float tileHeight = this.getSurfaceHeight(pos) / (Short.MAX_VALUE / 3.3f);
                        float node2 = y == y0 - 1 ? tileHeight : (this.getSurfaceHeight(pos) / (Short.MAX_VALUE / 3.3f));
                        
                        // Get the tile type
                        final Tile tile = this.getEffectiveTileType(pos);
                        
                        float h = ((node2 - tileHeight) * 1500) / 256.0f * 0x1000 / 128 + tileHeight / 2 + 1.0f;
                        h *= 0.4f;
                        
                        // Set the color based on the height
                        float r = h;
                        float g = h;
                        float b = h;
                        
                        // Get the color the tile should be
                        final Color color = (tileData.isPresent() ? tileData.get().getColor() : super.terrainColor(map, tile, pos));
                        
                        // Adjust the color to the "normal" color
                        r *= (color.getRed() / 255.0f) * 2;
                        g *= (color.getGreen() / 255.0f) * 2;
                        b *= (color.getBlue() / 255.0f) * 2;
                        
                        // Adjust values if the player is not standing there
                        if (!tileData.isPresent()) {
                            r = this.adjustScale(r);
                            g = this.adjustScale(g);
                            b = this.adjustScale(b);
                            
                            // Apply a water filter to the color
                            if (tileHeight < 0) {
                                r = r * 0.2f + 0.4f * 0.4f;
                                g = g * 0.2f + 0.5f * 0.4f;
                                b = b * 0.2f + 1.0f * 0.4f;
                            }
                        }
                        
                        final int altTarget = y - (int) (getSurfaceHeight(pos) * MapRenderer.MAP_HEIGHT / 4 / (Short.MAX_VALUE / 2.5f));
                        while (height > altTarget && height >= 0) {
                            if (height < imageY) // Set the color for the tile
                                grid.setAt(TileRenderLayer.TERRAIN,pos, r * 255, g * 255, b * 255);
                            height--;
                        }
                    }
                }
            }
        }
        
        // Stitch the map squares together
        return MapTile.join(imageDimension, grids);
    }
	
    private float adjustScale(float val) {
	    if (val > 1)
	        return 1;
	    if (val < 0)
	        return 0;
	    return val;
    }
    
    @Override
    public final RenderType getRenderType() {
        return RenderType.ISOMETRIC;
    }
}
