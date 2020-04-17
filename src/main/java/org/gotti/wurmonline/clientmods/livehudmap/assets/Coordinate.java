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

import com.wurmonline.client.game.PlayerPosition;

import java.util.Objects;

public final class Coordinate {
    
    private static final Coordinate MIN = Coordinate.of(0, 0);
    private static final Coordinate MAX = Coordinate.of(4096,4096);
    
    private final int x; // Tile X
    private final int y; // Tile Y
    private final int z; // Height Value
    
    private Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * @return Get the HORIZONTAL position
     */
    public int getX() {
        return this.x;
    }
    
    /**
     * @return Get the VERTICAL position
     */
    public int getY() {
        return this.y;
    }
    
    /**
     * @return Get the HEIGHT of the position
     */
    public int getZ() {
        return this.z;
    }
    
    /**
     * @return Convert the X tile to a game tile
     */
    public int getCellX() {
        return this.getX() * 4;
    }
    
    /**
     * @return Convert the Y tile to a game tile
     */
    public int getCellY() {
        return this.getY() * 4;
    }
    
    /**
     * @return
     */
    public int getTileX() {
        int tileX = this.getX() % LiveMapConfig.MAP_TILE_SIZE;
        return (tileX < 0 ? LiveMapConfig.MAP_TILE_SIZE + tileX : tileX);
    }
    
    /**
     * @return
     */
    public int getTileY() {
        int tileY = this.getY() % LiveMapConfig.MAP_TILE_SIZE;
        return (tileY < 0 ? LiveMapConfig.MAP_TILE_SIZE + tileY : tileY);
    }
    
    /**
     * @return The position within the Tile (64x64)
     */
    public Coordinate tilePos() {
        return Coordinate.of(
            this.getTileX(),
            this.getTileY()
        );
    }
    
    /**
     * @return Get the Coordinate of the nearest 64x64 Tile point
     */
    public Coordinate nearestTileMarker() {
        return Coordinate.of(
            this.getX() - this.getTileX(),
            this.getY() - this.getTileY()
        );
    }
    
    /**
     * @return Get the 64x64 Region that the Coordinate is in
     */
    public Region nearestRegion() {
        Coordinate base = this.nearestTileMarker();
        return new Region(base, LiveMapConfig.MAP_TILE_SIZE);
    }
    
    /*
     * Maths to add or subtract distance based on numerical values
     */
    public Coordinate add(Coordinate pos) {
        return this.add(pos.getX(), pos.getY());
    }
    public Coordinate add(int x, int y) {
        return Coordinate.of(
            this.getX() + x,
            this.getY() + y
        );
    }
    public Coordinate sub(Coordinate pos) {
        return this.sub(pos.getX(), pos.getY());
    }
    public Coordinate sub(int x, int y) {
        return Coordinate.of(
            this.getX() - x,
            this.getY() - y
        );
    }
    
    public Coordinate multiply(int by) {
        return Coordinate.of(
            this.getX() * by,
            this.getY() * by
        );
    }
    public Coordinate divide(int by) {
        return Coordinate.of(
            this.getX() / by,
            this.getY() / by
        );
    }
    
    /*
     * Boolean checks
     */
    public boolean isNegative() {
        return this.getX() < 0 || this.getY() < 0;
    }
    public boolean isPositive() {
        return this.getX() >=0 && this.getY() >= 0;
    }
    
    /*
     * Maths to add or subtract distance based on directions
     */
    public Coordinate offset(Direction direction) {
        return this.offset( direction, 1 );
    }
    public Coordinate offset(Direction direction, int count) {
        return Coordinate.of(
            this.getX() + (direction.getX() * count),
            this.getY() + (direction.getY() * count)
        );
    }
    public Coordinate offset(Direction dirA, int cA, Direction dirB, int cB) {
        return Coordinate.of(
            this.getX()
                + (dirA.getX() * cA)
                + (dirB.getX() * cB),
            this.getY()
                + (dirA.getY() * cA)
                + (dirB.getY() * cB)
        );
    }
    
    /*
     * Conditional checks to see if points share an X or Y value
     */
    public boolean sameX(Coordinate pos) {
        return this.getX() == pos.getX();
    }
    public boolean sameY(Coordinate pos) {
        return this.getY() == pos.getY();
    }
    
    /*
     * Area Constructors using this and another point as bases
     */
    public Area to(int x, int y) {
        return this.to(Coordinate.of(x, y));
    }
    public Area to(Coordinate second) {
        return Area.of(this, second);
    }
    
    /*
     * Min and Max Coordinates of the map
     */
    public static Coordinate min() {
        return Coordinate.MIN;
    }
    public static Coordinate max() { return Coordinate.MAX; }
    
    public static Coordinate of(long val) {
        return Coordinate.of(
            val >> 16,
            val & 0xffff
        );
    }
    
    public static Coordinate of(int x, int y) {
        return new Coordinate(x, y, 0);
    }
    public static Coordinate of(float x, float y) {
        return Coordinate.of((int)x, (int)y);
    }
    public static Coordinate of(int x, int y, int z) {
        return new Coordinate(x, y, z);
    }
    public static Coordinate of(PlayerPosition pos) {
        return Coordinate.of(pos.getTileX(), pos.getTileY(), pos.getLayer());
    }
    
    public static Coordinate parse(String dimensions) {
        return Coordinate.parse(dimensions,"x");
    }
    public static Coordinate parse(String dimensions, String separator) {
        return Coordinate.parse(dimensions.split(separator, 2));
    }
    public static Coordinate parse(String[] dimensions) {
        return Coordinate.of(
            Integer.parseInt(dimensions[0]),
            Integer.parseInt(dimensions[1])
        );
    }
    
    public static int horizontalDiff(Coordinate start, Coordinate end) {
        return Direction.EAST.getDistance(start) - Direction.EAST.getDistance(end);
    }
    public static int verticalDiff(Coordinate start, Coordinate end) {
        return Direction.SOUTH.getDistance(start) - Direction.SOUTH.getDistance(end);
    }
    
    @Override
    public String toString() {
        return this.getX() + "x" + this.getY();
    }
    public long toLong() {
        return (this.getX() << 16) | this.getY();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Coordinate))
            return false;
        Coordinate pos = (Coordinate)obj;
        return (pos.getX() == this.getX()) && (pos.getY() == this.getY() && (pos.getZ() == this.getZ()));
    }
    @Override
    public int hashCode() {
        return Objects.hash( this.getX(), this.getY(), this.getZ() );
    }
    
    public static boolean equals(Coordinate a, Coordinate b) {
        if (a == null && b == null)
            return true;
        return (a != null) && a.equals(b);
    }
}
