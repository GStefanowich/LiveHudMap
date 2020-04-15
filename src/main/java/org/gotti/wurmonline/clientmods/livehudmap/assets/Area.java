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

import java.util.Objects;

/**
 * 
 */
public final class Area {
    private Coordinate northWest;
    private Coordinate southEast;
    
    private Area() {
        this(Coordinate.min(), Coordinate.min());
    }
    private Area(Coordinate a, Coordinate b) {
        this.set( a, b );
    }
    
    public void set(Coordinate pointA, Coordinate pointB) {
        int upperX = Math.max( pointA.getX(), pointB.getX() );
        int upperY = Math.max( pointA.getY(), pointB.getY() );
    
        int lowerX = Math.min( pointA.getX(), pointB.getX() );
        int lowerY = Math.min( pointA.getY(), pointB.getY() );
        
        // Set the positioning
        this.northWest = Coordinate.of(lowerX, lowerY);
        this.southEast = Coordinate.of(upperX, upperY);
    }
    
    public Coordinate getNorthWest() {
        return this.northWest;
    }
    public Coordinate getSouthEast() {
        return this.southEast;
    }
    
    public Coordinate snap(Coordinate old) {
        if (this.isWithin( old ))
            return old;
        else if (old.isNegative()) {
            return Coordinate.of(
                Math.max(northWest.getX(), old.getX()),
                Math.max(northWest.getY(), old.getY())
            );
        } else {
            return Coordinate.of(
                Math.min(southEast.getX(), old.getX()),
                Math.min(southEast.getX(), old.getY())
            );
        }
    }
    
    public boolean isWithin(Coordinate c) {
        int upperX = Math.max( this.northWest.getX(), this.southEast.getX() );
        int upperY = Math.max( this.northWest.getY(), this.southEast.getY() );
        
        int lowerX = Math.min( this.northWest.getX(), this.southEast.getX() );
        int lowerY = Math.min( this.northWest.getY(), this.southEast.getY() );
        
        return  ( c.getX() >= lowerX ) && ( c.getY() >= lowerY ) && ( c.getX() <= upperX ) && ( c.getY() <= upperY );
    }
    public boolean isBorder(Coordinate c) {
        return this.northWest.sameX(c)
            || this.southEast.sameX(c)
            || this.northWest.sameY(c)
            || this.southEast.sameY(c); 
    }
    
    public static Area ofEmpty() {
        return new Area();
    }
    public static Area of(Coordinate a, Coordinate b) {
        return new Area(a, b);
    }
    
    @Override
    public String toString() {
        return (this.getNorthWest().getX() + "." + this.getNorthWest().getY())
            + "-" + (this.getNorthWest().getX() + "." + this.getSouthEast().getY())
            + "-" + (this.getSouthEast().getX() + "." + this.getSouthEast().getY())
            + "-" + (this.getSouthEast().getY() + "." + this.getNorthWest().getY());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Area))
            return false;
        Area border = (Area)obj;
        return Objects.equals(this.getNorthWest(), border.getNorthWest()) && Objects.equals(this.getSouthEast(), border.getSouthEast());
    }
    @Override
    public int hashCode() {
        return Objects.hash( this.getNorthWest(), this.getSouthEast() );
    }
}
