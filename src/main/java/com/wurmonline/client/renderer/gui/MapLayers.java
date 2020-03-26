package com.wurmonline.client.renderer.gui;

import org.gotti.wurmonline.clientmods.livehudmap.MapLayerView;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.RenderType;

public final class MapLayers {
    private MapLayers() {}
    
    public final static MapLayerView SURFACE = new MapLayerView(WurmComponent.hud.getWorld(), RenderType.FLAT);
    public final static MapLayerView CAVE = new MapLayerView(WurmComponent.hud.getWorld(), RenderType.CAVE);
}
