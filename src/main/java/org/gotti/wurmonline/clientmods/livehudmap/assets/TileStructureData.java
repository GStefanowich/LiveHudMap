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

import com.wurmonline.client.renderer.structures.BridgePartData;
import com.wurmonline.client.renderer.structures.FloorData;
import com.wurmonline.client.renderer.structures.HouseData;
import com.wurmonline.client.renderer.structures.RoofData;
import com.wurmonline.client.renderer.structures.StructureData;
import com.wurmonline.client.renderer.structures.WallData;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.TileColors;

import java.awt.Color;

public class TileStructureData extends AbstractTileData<Coordinate> {
    
    private final String name;
    private final Coordinate pos;
    private final float layer;
    private final AbstractTileType type;
    private final Color color;
    
    public TileStructureData(AbstractTileType type, String name, StructureData data) {
        this.name = name;
        this.pos = Coordinate.of(
            data.getTileX(),
            data.getTileY()
        );
        this.layer = data.getHPos();
        this.type = type;
        
        if (data instanceof BridgePartData)
            this.color = TileColors.getColorFor(((BridgePartData)data).getMaterial());
        else if (data instanceof RoofData)
            this.color = TileColors.getColorFor((RoofData)data);
        else if (data instanceof FloorData)
            this.color = TileColors.getColorFor((FloorData)data);
        else this.color = Color.WHITE;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public Coordinate getPos() {
        return this.pos;
    }
    @Override
    public float getHeight() {
        return this.layer;
    }
    
    public boolean isBridge() {
        return this.type == AbstractTileType.BRIDGE;
    }
    public boolean isBuilding() {
        return this.type == AbstractTileType.HOUSE;
    }
    
    @Override
    public Color getColor() {
        return this.color;
    }
    
    @Override
    public AbstractTileType getType() {
        return this.type;
    }
}
