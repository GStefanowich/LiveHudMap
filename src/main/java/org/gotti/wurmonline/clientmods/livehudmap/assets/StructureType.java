package org.gotti.wurmonline.clientmods.livehudmap.assets;

import com.wurmonline.client.renderer.structures.BridgeData;
import com.wurmonline.client.renderer.structures.HouseData;
import com.wurmonline.client.renderer.structures.StructureData;

public enum StructureType {
    BRIDGE,
    HOUSE,
    GROUND;
    
    public static StructureType getByClassName(StructureData structure) {
        if (structure instanceof BridgeData)
            return BRIDGE;
        if (structure instanceof HouseData)
            return HOUSE;
        return null;
    }
}
