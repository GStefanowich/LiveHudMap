package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import com.wurmonline.client.game.NearTerrainDataBuffer;
import com.wurmonline.mesh.Tiles.Tile;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileStructureData;

public class MapRendererIsometric extends AbstractSurfaceRenderer {
	
	public MapRendererIsometric( RenderType type, NearTerrainDataBuffer buffer ) {
		super( type, buffer );
	}
	
    public BufferedImage createMapDump(LiveMap map, int leftX, int topY, int imageX, int imageY, Coordinate playerPos) {
        if (topY < 0) topY = 0;
        if (leftX < 0) leftX = 0;
        
        // Image should always be a square that covers both lengths
        int imageDimension = Math.max(imageY, imageX);
        
        // Create a new image canvas
        final BufferedImage bi2 = new BufferedImage(imageDimension, imageDimension, BufferedImage.TYPE_INT_RGB);
        // Create an RGB map (Length x Width x 3[RGB])
        final float[] data = new float[imageX * imageY * 3];
        
        int y0 = imageY + imageY / 2;
        
        // For X direction
        for (int x = 0; x < imageX; x++) {
            int height = y0 - 1;
            
            // For Y direction
            for (int y = y0 - 1; y >= -imageY / 2 && height >= 0; y--) {
                Coordinate pos = Coordinate.of( x + leftX, y + topY );
                
                // If the player is on the tile
                List<TileEntityData> entityAt = map.getEntitiesAt( pos );
                List<TileStructureData> structureAt = map.getStructuresAt( pos );
                
                // Get the tile height
                float tileHeight = this.getSurfaceHeight( pos ) / (Short.MAX_VALUE / 3.3f);
                float node2 = y == y0 - 1 ? tileHeight : (this.getSurfaceHeight( pos ) / (Short.MAX_VALUE / 3.3f));
                
                // Get the tile type
                final Tile tile = this.getTileType(pos );
                
                float h = ((node2 - tileHeight) * 1500) / 256.0f * 0x1000 / 128 + tileHeight / 2 + 1.0f;
                h *= 0.4f;
                
                // Set the color based on the height
                float r = h;
                float g = h;
                float b = h;
                
                // Get the color the tile should be
                final Color color = ( entityAt.isEmpty() ? super.tileColor(map, tile, pos ) : map.entityColor(entityAt.get( 0 )));
                
                // Adjust the color to the "normal" color
                r *= (color.getRed() / 255.0f) * 2;
                g *= (color.getGreen() / 255.0f) * 2;
                b *= (color.getBlue() / 255.0f) * 2;
                
                // Adjust values if the player is not standing there
                if ( entityAt.isEmpty() && structureAt.isEmpty() ) {
                    r = this.adjustScale( r );
                    g = this.adjustScale( g );
                    b = this.adjustScale( b );
                    
                    // Apply a water filter to the color
                    if (tileHeight < 0) {
                        r = r * 0.2f + 0.4f * 0.4f;
                        g = g * 0.2f + 0.5f * 0.4f;
                        b = b * 0.2f + 1.0f * 0.4f;
                    }
                }
                
                final int altTarget = y - (int) (getSurfaceHeight(pos ) * MapRenderer.MAP_HEIGHT / 4  / (Short.MAX_VALUE / 2.5f));
                while (height > altTarget && height >= 0) {
                	if (height < imageY) {
                        int pixelPos = (x + height * imageX) * 3;
	                    data[pixelPos + 0] = r * 255;
	                    data[pixelPos + 1] = g * 255;
	                    data[pixelPos + 2] = b * 255;
                	}
                    height--;
                }
            }
        }
        
        bi2.getRaster().setPixels(0, 0, imageX, imageY, data);
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
