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

package org.gotti.wurmonline.clientmods.livehudmap.assets;

import org.json.JSONObject;

public final class SklotopolisDeed {
    
    private final Coordinate deedToken;
    private final Area deedBorder = Area.ofEmpty();
    private final Area perimeterBorder = Area.ofEmpty();
    
    private String deedName;
    private Kingdoms kingdom;
    
    private boolean isSpawn = false;
    
    public SklotopolisDeed(JSONObject blob) {
        this.deedToken = Coordinate.of(blob.getInt("x"), blob.getInt("y")); // Set the deed token
        this.update( blob ); // Call the update (Non final objects)
        /*{"name": "Port Malone","tag": "port-malone-2443","mayor": "Backpfeifengesicht","tilesNorth": 51,"tilesSouth": 81,"tilesEast": 146,"tilesWest": 59,"tilesPerimeter": 5,"guards": 0,"kingdom": 4,"isSpawnPoint": false,"type": "large","lastActive": "Last active: 0 days ago.","x": 2443,"y": 3496}*/
    }
    public void update(JSONObject blob) {
        // Update the deeds name
        this.deedName = blob.getString("name");
        
        // Update the deeds size
        this.deedBorder.set(
            this.deedToken.offset(
                Direction.NORTH,
                blob.getInt("tilesNorth"),
                Direction.WEST,
                blob.getInt("tilesWest")
            ),
            this.deedToken.offset(
                Direction.SOUTH,
                blob.getInt("tilesSouth"),
                Direction.EAST,
                blob.getInt("tilesEast")
            )
        );
        
        int perimeterSize = blob.getInt("tilesPerimeter");
        this.perimeterBorder.set(
            this.deedBorder.getNorthWest().offset(
                Direction.NORTH,
                perimeterSize,
                Direction.WEST,
                perimeterSize
            ),
            this.deedBorder.getSouthEast().offset(
                Direction.SOUTH,
                perimeterSize,
                Direction.EAST,
                perimeterSize
            )
        );
        
        // If spawn point
        this.isSpawn = blob.getBoolean("isSpawnPoint");
        
        // Set the kingdom
        this.kingdom = Kingdoms.getById(blob.getInt("kingdom"));
    }
    
    public String getName() {
        return this.deedName;
    }
    public Kingdoms getKingdom() {
        return this.kingdom;
    }
    public String getDimensions() {
        Coordinate size = this.getNorthWest().sub(this.getSouthEast());
        return (Math.abs(size.getX()) + 1) + "x" + (Math.abs(size.getY()) + 1);
    }
    
    // Token Positioning
    public Coordinate getCenter() {
        return this.deedToken;
    }
    
    // Deed Positioning
    public Area getDeedBorder() {
        return this.deedBorder;
    }
    public Coordinate getNorthWest() {
        return this.getDeedBorder().getNorthWest();
    }
    public Coordinate getSouthEast() {
        return this.getDeedBorder().getSouthEast();
    }
    
    // Perimeter Positioning
    public Area getPerimeterBorder() {
        return this.perimeterBorder;
    }
    public Coordinate getPerimeterNorthWest() {
        return this.getPerimeterBorder().getNorthWest();
    }
    public Coordinate getPerimeterSouthEast() {
        return this.getPerimeterBorder().getSouthEast();
    }
    
    public boolean isSpawn() {
        return this.isSpawn;
    }
    
    public TileDeedData getTileData(Coordinate pos) {
        boolean inBorder = this.perimeterBorder.isWithin( pos );
        if ( inBorder ) {
            boolean inDeed = this.deedBorder.isWithin(pos);
            if (( this.perimeterBorder.isBorder( pos ) || ( inDeed && this.deedBorder.isBorder( pos ) )))
                return new TileDeedData(this.getName(), pos, !inDeed);
        }
        return null;
    }
}
