package org.gotti.wurmonline.clientmods.livehudmap.assets;

import com.wurmonline.client.renderer.structures.BridgePartData;
import com.wurmonline.client.renderer.structures.StructureData;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.TileColors;

import java.awt.Color;

public class TileStructureData extends AbstractTileData<Coordinate> {
    
    private final String name;
    private final Coordinate pos;
    private final float layer;
    private final StructureType type;
    private final Color color;
    
    public TileStructureData(StructureType type, String name, StructureData data) {
        this.name = name;
        this.pos = Coordinate.of(
            data.getTileX(),
            data.getTileY()
        );
        this.layer = data.getHPos();
        this.type = type;
        
        if (data instanceof BridgePartData)
            this.color = TileColors.getColorFor(((BridgePartData)data).getMaterial());
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
        return this.type == StructureType.BRIDGE;
    }
    public boolean isBuilding() {
        return this.type == StructureType.HOUSE;
    }
    
    @Override
    public Color getColor() {
        return this.color;
    }
}
