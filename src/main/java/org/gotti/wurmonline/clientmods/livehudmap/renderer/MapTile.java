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
import org.apache.commons.codec.Charsets;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Region;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Sync;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileRenderLayer;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class MapTile {
    private BufferedImage displayImage = null; // The image shown on the screen
    private final BufferedImage terrainImage; // The image of the terrain
    //private final BufferedImage entityImage;
    private final Region area;
    
    private final float[] terrainLayer;
    private final float[] entityLayer;
    
    private final Map<Long, List<String>> toolTipData;
    
    private boolean genericDirty = false;
    private boolean terrainDirty = false;
    
    private MapTile(Region area, BufferedImage image, JSONObject data) {
        this.area = area;
        // The image displayed on the screen should be a CLONE so that the terrain layer is not modified
        this.displayImage = MapTile.clone(image);
        this.terrainImage = image;
        //this.entityImage  = this.empty(TileRenderLayer.ENTITY);
        
        int dim = this.getWidth() * this.getHeight();
        this.terrainLayer = image.getData().getPixels(
            // Read from the image start to finish
            0, 0,
            this.area.getWidth(),
            this.area.getHeight(),
            // Create the standard float array
            new float[dim * TileRenderLayer.TERRAIN.numValues()]
        );
        this.entityLayer  = new float[dim * TileRenderLayer.ENTITY.numValues()];
        
        this.toolTipData = MapTile.datifyJSON(data);
    }
    private MapTile(Region area) {
        this.area = area;
        this.displayImage = this.empty(TileRenderLayer.TERRAIN);
        this.terrainImage = this.empty(TileRenderLayer.TERRAIN);
        //this.entityImage  = this.empty(TileRenderLayer.ENTITY);
        
        int dim = this.getWidth() * this.getHeight();
        this.terrainLayer = new float[dim * TileRenderLayer.TERRAIN.numValues()];
        this.entityLayer  = new float[dim * TileRenderLayer.ENTITY.numValues()];
        
        this.toolTipData = new HashMap<>();
    }
    
    public void setAt(TileRenderLayer renderLayer, Coordinate pos, final float r, final float g, final float b) {
        this.setAt(renderLayer,pos, r,g,b,255);
    }
    public void setAt(TileRenderLayer renderLayer, Coordinate pos, final float r, final float g, final float b, final float a) {
        this.setAt(renderLayer,pos, new Color(r / 255,g / 255,b / 255,a / 255));
    }
    public void setAt(TileRenderLayer renderLayer, Coordinate pos, Color color) {
        this.setInner(renderLayer,pos.getTileX(), pos.getTileY(), color);
    }
    private void setInner(TileRenderLayer renderLayer, int x, int y, final Color color) {
        int pixelPos = ((x + y * this.area.getWidth()) * renderLayer.numValues()) - 1;
        
        // Get the pixel array 
        final float[] layer = renderLayer == TileRenderLayer.TERRAIN ?
            this.terrainLayer : this.entityLayer;
        boolean updated = false;
        
        // Update ALPHA value
        if (renderLayer.getImageType() == BufferedImage.TYPE_INT_ARGB) {
            if (layer[++pixelPos] != color.getAlpha()) {
                layer[pixelPos] = color.getAlpha();
                updated = true;
            }
        }
        
        // Update RED value
        if (layer[++pixelPos] != color.getRed()) {
            layer[pixelPos] = color.getRed();
            updated = true;
        }
        
        // Update GREEN value
        if (layer[++pixelPos] != color.getGreen()) {
            layer[pixelPos] = color.getGreen();
            updated = true;
        }
        
        // Update BLUE value
        if (layer[++pixelPos] != color.getBlue()) {
            layer[pixelPos] = color.getBlue();
            updated = true;
        }
        
        // Mark the map for re-render/re-save
        if (updated) {
            if (renderLayer == TileRenderLayer.TERRAIN)
                this.markTerrainDirty();
            else this.markDirty();
        }
    }
    
    public void setData(Coordinate pos, List<String> data) {
        if (!data.isEmpty()) // Save the data if the data is not empty
            this.toolTipData.put(pos.tilePos().toLong(), data);
    }
    public List<String> getData(Coordinate pos) {
        return this.toolTipData.getOrDefault(pos.tilePos().toLong(), Collections.emptyList());
    }
    
    private Coordinate getBase() {
        return this.area.getBase();
    }
    private Region getArea() {
        return this.area;
    }
    
    private void markDirty() {
        this.genericDirty = true;
    }
    private void markTerrainDirty() {
        this.markDirty();
        this.terrainDirty = true;
    }
    
    public BufferedImage empty(TileRenderLayer layer) {
        return new BufferedImage(this.getWidth(), this.getHeight(), layer.getImageType());
    }
    
    public BufferedImage write() {
        // Update the terrain layer
        if (this.terrainDirty) {
            WritableRaster terrainWriter = this.terrainImage.getRaster();
            
            // Write the Terrain data
            terrainWriter.setPixels(0, 0, this.getWidth(), this.getHeight(), this.terrainLayer);
        }
        // Update the Entity Layer
        if (this.genericDirty || this.displayImage == null) {
            this.displayImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
            
            // Get the graphics handler of the Display Image
            Graphics graphics = this.displayImage.getGraphics();
            
            // Draw the layers to the Display Image
            graphics.drawImage(this.terrainImage,0,0,null);
            
            // TODO: Draw the Entity layer over the terrain layer
            
            /*// Get the Raster Writer
            WritableRaster displayWriter = this.displayImage.getRaster();
            
            // Write the Terrain data
            displayWriter.setPixels(0, 0, this.getWidth(), this.getHeight(), this.entityLayer);*/
            
            this.genericDirty = false;
        }
        return this.displayImage;
    }
    
    public boolean save(final Path folder, final String name) throws IOException {
        boolean success = false;
        if (this.terrainDirty) {
            // Only save if the tile is within the map range (Don't save the map border)
            if (LiveMap.isWithinMap(this.getBase())) {
                // Create the folder if it does not exist
                if (!Files.exists(folder))
                    Files.createDirectories(folder);
    
                // Log that we're writing
                LiveHudMapMod.log("Writing map tile " + this.toString());
    
                // Save the image
                success = ImageIO.write(this.terrainImage, "png", Paths.get(folder.toString(), name + ".png").toFile());
    
                // Save the JSON data
                JSONObject json = MapTile.JSONifyData(this.toolTipData);
                if (!json.isEmpty())
                    Files.write(Paths.get(folder.toString(), name + ".json"), Collections.singleton(json.toString()), Charsets.UTF_8);
            }
            // The MapTile is no longer "dirty" (Doesn't need to be saved again)
            this.terrainDirty = false;
        }
        return success;
    }
    
    public int getWidth() {
        return this.getArea().getWidth();
    }
    public int getHeight() {
        return this.getArea().getHeight();
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
    
    public static MapTile empty(Region region) {
        return new MapTile(region);
    }
    public static MapTile from(Region region, BufferedImage image, JSONObject data) {
        return new MapTile(region, image, data);
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
    
    public static BufferedImage clone(BufferedImage image) {
        ColorModel model = image.getColorModel();
        boolean preMulti = model.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(model, raster, preMulti, null);
    }
    
    /**
     * Save a MapTile to disk
     * @param region The region that is getting saved
     * @param renderType The layer to save the region to
     * @param future The future container of the MapTile to save
     */
    public static void saveToDisk(final Region region, final RenderType renderType, final SettableFuture<MapTile> future) {
        // Execute the save on a separate thread
        LiveMap.threadExecute(() -> Sync.run(region.toString(), () -> {
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
        }));
    }
    
    private static JSONObject JSONifyData(Map<Long, List<String>> map) {
        JSONObject json = new JSONObject();
        
        // Iterate the map
        for (Map.Entry<Long, List<String>> entry : map.entrySet()) {
            json.put(
                // Convert the Coordinate Key to a String
                entry.getKey().toString(),
                // Convert the list of Tooltips to a String
                new JSONArray(entry.getValue())
            );
        }
        
        return json;
    }
    private static Map<Long, List<String>> datifyJSON(JSONObject json) {
        Map<Long, List<String>> map = new HashMap<>();
        
        // Iterate the JSON Object
        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            
            map.put(
                // Convert the String Key to a Coordinate
                Long.parseLong(key),
                // Convert the List of Objects to a List of Strings
                json.getJSONArray(key).toList()
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList())
            );
        }
        
        return map;
    }
}
