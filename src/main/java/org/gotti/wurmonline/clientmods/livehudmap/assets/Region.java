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
public final class Region {
    private final Coordinate base;
    private final int height;
    private final int width;
    
    Region(Coordinate start, Coordinate end) {
        this.base = start;
        this.height = Coordinate.verticalDiff( start, end );
        this.width = Coordinate.horizontalDiff( start, end );
    }
    Region(Coordinate start, int dimension) {
        this.base = start;
        this.height = dimension;
        this.width = dimension;
    }
    
    public Coordinate getBase() {
        return this.base;
    }
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
    
    @Override
    public String toString() {
        return this.base.toString() + "-" + this.width + "x" + this.height;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Region))
            return false;
        Region region = (Region) obj;
        return Objects.equals(this.getBase(), region.getBase())
            && (this.getWidth() == region.getWidth())
            && (this.getHeight() == region.getHeight());
    }
    @Override
    public int hashCode() {
        return Objects.hash( this.getBase(), this.width, this.height );
    }
}
