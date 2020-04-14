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

import java.awt.Color;

public class TileDeedData extends AbstractTileData<Coordinate> {
    private final Coordinate pos;
    private final boolean isBorder;
    private final String deedName;
    
    public TileDeedData(String deedName, Coordinate pos, boolean border) {
        this.pos = pos;
        this.isBorder = border;
        this.deedName = deedName + ( isBorder ? " Border" : "" );
    }
    
    @Override
    public String getName() {
        return this.deedName;
    }
    
    @Override
    public Coordinate getPos() {
        return this.pos;
    }
    
    @Override
    public float getHeight() {
        return -1;
    }
    
    @Override
    public Color getColor() {
        return this.isBorder ? new Color(139,0,0) : new Color(124,252,0);
    }
    
    @Override
    public AbstractTileType getType() {
        return AbstractTileType.DEED;
    }
}
