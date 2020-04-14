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

package org.gotti.wurmonline.clientmods.livehudmap.reflection;

import com.wurmonline.client.renderer.cell.Cell;
import com.wurmonline.client.renderer.structures.BridgeData;
import com.wurmonline.client.renderer.structures.BridgePartData;
import com.wurmonline.client.renderer.structures.FloorData;
import com.wurmonline.client.renderer.structures.HouseData;
import com.wurmonline.client.renderer.structures.HouseFloorData;
import com.wurmonline.client.renderer.structures.HouseRoofData;
import com.wurmonline.client.renderer.structures.RoofData;
import com.wurmonline.client.renderer.structures.StructureData;
import com.wurmonline.shared.constants.StructureConstants.FloorMaterial;

import java.lang.reflect.Field;
import java.util.List;

public class Structures {
    private Structures() {}
    
    private static final Field BRIDGE = Reflection.getField(BridgePartData.class, "bridge");
    
    private static final Field FLOOR_MATERIAL = Reflection.getField(FloorData.class, "material");
    private static final Field FLOOR_HOUSE = Reflection.getField(HouseFloorData.class, "house");
    
    private static final Field ROOF_MATERIAL = Reflection.getField(RoofData.class, "roofMaterial");
    private static final Field ROOF_HOUSE = Reflection.getField(HouseRoofData.class, "house");
    
    private static final Field CELL_STRUCTURES = Reflection.getField(Cell.class, "staticStructures");
    
    public static FloorMaterial getMaterial(FloorData floor) {
        return Reflection.getPrivateField(floor, FLOOR_MATERIAL);
    }
    public static FloorMaterial getMaterial(RoofData roof) {
        return Reflection.getPrivateField(roof, ROOF_MATERIAL);
    }
    public static HouseData getHouse(HouseFloorData floor) {
        return Reflection.getPrivateField(floor, FLOOR_HOUSE);
    }
    public static HouseData getHouse(HouseRoofData roof) {
        return Reflection.getPrivateField(roof, ROOF_HOUSE);
    }
    
    public static List<StructureData> getStructures(Cell cell) {
        return Reflection.getPrivateField(cell, CELL_STRUCTURES);
    }
    
    public static BridgeData getBridge(BridgePartData bridge) {
        return Reflection.getPrivateField(bridge, BRIDGE);
    }
}
