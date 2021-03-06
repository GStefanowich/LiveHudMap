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

public enum Direction {
    NORTH( 0, -1 ) {
        @Override
        public Direction opposite() {
            return SOUTH;
        }
    },
    EAST( 1, 0 ) {
        @Override
        public Direction opposite() {
            return WEST;
        }
    },
    SOUTH( 0, 1 ) {
        @Override
        public Direction opposite() {
            return NORTH;
        }
    },
    WEST( -1 , 0 ) {
        @Override
        public Direction opposite() {
            return EAST;
        }
    };
    
    private final int x;
    private final int y;
    
    Direction(int xOffset, int yOffset) {
        this.x = xOffset;
        this.y = yOffset;
    }
    
    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    public int getDistance(Coordinate pos) {
        return (this.getX() * pos.getX()) + (this.getY() * pos.getY());
    }
    
    public abstract Direction opposite();
    public static Direction fromRotation(float rotation) {
        if (rotation < 45f)
            return Direction.SOUTH;
        if (rotation < 135)
            return Direction.EAST;
        if (rotation < 225)
            return Direction.NORTH;
        if (rotation < 315)
            return Direction.WEST;
        return Direction.SOUTH;
    }
}
