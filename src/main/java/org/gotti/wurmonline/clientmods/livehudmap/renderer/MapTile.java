/*
 * This software is licensed under the MIT License
 * https://github.com/GStefanowich/LiveHudMap
 *
 * Copyright (c) 2019 Gregory Stefanowich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.google.common.util.concurrent.SettableFuture;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.LiveMapConfig;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Region;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public final class MapTile {
    private final BufferedImage image;
    private final Region area;
    private final float[] pixels;
    
    private boolean dirty = false;
    
    private MapTile(Region area, BufferedImage image) {
        this.area = area;
        this.image = image;
        this.pixels = image.getData().getPixels(
            // Read from the image start to finish
            0, 0,
            this.area.getWidth(),
            this.area.getHeight(),
            // Create the standard float array
            new float[this.area.getWidth() * this.area.getHeight() * 3]
        );
    }
    private MapTile(Region area) {
        this.area = area;
        this.image = new BufferedImage(this.area.getWidth(), this.area.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.pixels = new float[this.area.getWidth() * this.area.getHeight() * 3];
    }
    
    public void setAt(Coordinate pos, final float r, final float g, final float b) {
        this.setInner(pos.getTileX(), pos.getTileY(), r, g, b);
    }
    private void setInner(int x, int y, final float r, final float g, final float b) {
        int pixelPos = (x + y * this.area.getWidth()) * 3;
        
        // Update RED value
        if (this.pixels[pixelPos] != r) {
            this.pixels[pixelPos] = r;
            this.markDirty();
        }
        
        // Update GREEN value
        if (this.pixels[++pixelPos] != g) {
            this.pixels[pixelPos] = g;
            this.markDirty();
        }
        
        // Update BLUE value
        if (this.pixels[++pixelPos] != b) {
            this.pixels[pixelPos] = b;
            this.markDirty();
        }
    }
    
    private Coordinate getBase() {
        return this.area.getBase();
    }
    private Region getArea() {
        return this.area;
    }
    
    private void markDirty() {
        this.dirty = true;
    }
    
    public BufferedImage write() {
        this.image.getRaster().setPixels(0, 0, this.area.getWidth(), this.area.getHeight(), this.pixels);
        return this.image;
    }
    public boolean save(Path folder, String name) throws IOException {
        boolean success = false;
        if (this.dirty) {
            // Create the folder if it does not exist
            if (!Files.exists(folder))
                Files.createDirectories(folder);
            
            // Create the File path
            File location = Paths.get(folder.toString(), name + ".png").toFile();
            
            // Log that we're writing
            LiveHudMapMod.log("Writing to " + location.toString());
            
            // Save the image
            success = ImageIO.write(this.image, "png", location);
            
            // The MapTile is no longer "dirty" (Doesn't need to be saved again)
            this.dirty = false;
        }
        return success;
    }
    
    public static MapTile empty(Region region) {
        return new MapTile(region);
    }
    public static MapTile from(Region region, BufferedImage image) {
        return new MapTile(region, image);
    }
    public static BufferedImage join(int imageDimension, Collection<MapTile> tiles) {
        BufferedImage image = new BufferedImage(imageDimension, imageDimension, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        
        // Handle all of the tiles
        for (MapTile tile : tiles) {
            // If the tile is null, skip it
            if (tile == null)
                continue;
            
            Region area = tile.getArea();
            
            // Shift the offset of the map to fit within the window properly
            Coordinate diff = LiveMap.getMapOffset(tile.getBase());
            
            // Draw the tile onto the bigger image
            graphics.drawImage(
                tile.write(),
                diff.getX(),
                diff.getY(),
                diff.getX() + area.getWidth(),
                diff.getY() + area.getHeight(),
                0,
                0,
                area.getWidth(),
                area.getHeight(),
                null
            );
        }
        
        return image;
    }
    
    @Override
    public String toString() {
        return this.getArea().toString();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof MapTile))
            return false;
        MapTile tile = (MapTile)obj;
        return Objects.equals(tile.getBase(), this.getBase()) && Objects.equals(tile.getArea(), this.getArea());
    }
    @Override
    public int hashCode() {
        return Objects.hash( this.getBase(), this.getArea() );
    }
    
    /**
     * Save a MapTile to disk
     * @param region The region that is getting saved
     * @param renderType The layer to save the region to
     * @param future The future container of the MapTile to save
     */
    public static void saveToDisk(final Region region, final RenderType renderType, final SettableFuture<MapTile> future) {
        // Execute the save on a separate thread
        LiveMap.threadExecute(() -> {
            // Create the path to save to
            Path folder = Paths.get(
                LiveHudMapMod.MOD_FOLDER.toString(),
                LiveHudMapMod.getServerName(),
                renderType.name().toLowerCase()
            );
            
            try {
                
                // If finished and not cancelled, Cancel the future
                if (!(future.isDone() || future.isCancelled()))
                    future.cancel(false);
                
                // Save the MapTile
                if (future.isDone())
                    future.get().save(folder, region.toString());
                
            } catch (InterruptedException | ExecutionException | IOException e) {
                LiveHudMapMod.log(e);
            }
        });
    }
}
