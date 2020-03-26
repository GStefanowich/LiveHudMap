package org.gotti.wurmonline.clientmods.livehudmap;

import com.wurmonline.client.renderer.gui.MapLayers;

public enum MapLayer {
	SURFACE {
		@Override
		public final MapLayerView getMap() {
			return MapLayers.SURFACE;
		}
	},
	CAVE {
		@Override
		public final MapLayerView getMap() {
			return MapLayers.CAVE;
		}
	};
	
	public abstract MapLayerView getMap();
	
	public static MapLayer getByElevation(int layer) {
		if (layer >= 0) return SURFACE;
		return CAVE;
	}
}
