package org.gotti.wurmonline.clientmods.livehudmap.assets;

import java.awt.Color;

public abstract class AbstractTileData<Positioning> {
    
    public abstract String getName();
    public abstract Positioning getPos();
    public abstract float getHeight();
    
    public abstract Color getColor();
    
}
