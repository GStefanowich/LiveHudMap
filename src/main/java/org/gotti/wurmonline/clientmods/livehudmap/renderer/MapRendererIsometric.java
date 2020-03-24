package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;

public class MapRendererIsometric extends AbstractSurfaceRenderer {
	
	public MapRendererIsometric(NearTerrainDataBuffer buffer) {
		super(buffer);
	}
	
    public BufferedImage createMapDump(int leftX, int topY, int winWidth, int winHeight, int playerX, int playerY) {
        if (topY < 0) topY = 0;
        if (leftX < 0) leftX = 0;
        
        // Create a new image canvas
        final BufferedImage bi2 = new BufferedImage(winWidth, winHeight, BufferedImage.TYPE_INT_RGB);
        // Create an RGB map (Length x Width x 3[RGB])
        final float[] data = new float[winWidth * winHeight * 3];
        
        int y0 = winHeight + winHeight / 2;
        
        // For X direction
        for (int x = 0; x < winWidth; x++) {
            int height = y0 - 1;
            
            // For Y direction
            for (int y = y0 - 1; y >= -winHeight / 2 && height >= 0; y--) {
                // If the player is on the tile
                final boolean playerAt = LiveMap.SHOW_SELF && (playerX == (x + leftX)) && (playerY == (y + topY));
    
                // Get the tile height
                float node = this.getSurfaceHeight(x + leftX, y + topY) / (Short.MAX_VALUE / 3.3f);
                float node2 = y == y0 - 1 ? node : (this.getSurfaceHeight(x + 1 + leftX, y + 1 + topY) / (Short.MAX_VALUE / 3.3f));
                
                // Get the tile type
                final Tile tile = this.getTileType(x + leftX, y + topY);
                
                float h = ((node2 - node) * 1500) / 256.0f * 0x1000 / 128 + node / 2 + 1.0f;
                h *= 0.4f;
                
                // Set the color based on the height
                float r = h;
                float g = h;
                float b = h;
                
                // Get the color the tile should be
                final Color color = ( playerAt ? Color.RED : TileColors.getColorFor( tile == null ? Tile.TILE_DIRT : tile ));
                
                // Adjust the color to the "normal" color
                r *= (color.getRed() / 255.0f) * 2;
                g *= (color.getGreen() / 255.0f) * 2;
                b *= (color.getBlue() / 255.0f) * 2;
                
                // Adjust values if the player is not standing there
                if ( !playerAt ) {
                    r = this.adjustScale( r );
                    g = this.adjustScale( g );
                    b = this.adjustScale( b );
                    
                    if (node < 0) {
                        r = r * 0.2f + 0.4f * 0.4f;
                        g = g * 0.2f + 0.5f * 0.4f;
                        b = b * 0.2f + 1.0f * 0.4f;
                    }
                }
                
                final int altTarget = y - (int) (getSurfaceHeight(x + leftX, y + topY) * MapRenderer.MAP_HEIGHT / 4  / (Short.MAX_VALUE / 2.5f));
                while (height > altTarget && height >= 0) {
                	if (height < winHeight) {
                        int pixelPos = (x + height * winWidth) * 3;
	                    data[pixelPos + 0] = r * 255;
	                    data[pixelPos + 1] = g * 255;
	                    data[pixelPos + 2] = b * 255;
                	}
                    height--;
                }
            }
        }
        
        bi2.getRaster().setPixels(0, 0, winWidth, winHeight, data);
        return bi2;
    }
	
    private float adjustScale(float val) {
	    if (val > 1)
	        return 1;
	    if (val < 0)
	        return 0;
	    return val;
    }
    
}
