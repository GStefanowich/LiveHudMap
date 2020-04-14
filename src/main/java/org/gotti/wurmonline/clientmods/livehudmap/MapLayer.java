package org.gotti.wurmonline.clientmods.livehudmap;

import com.wurmonline.client.renderer.gui.MapLayers;

public enum MapLayer {
	SURFACE {
		@Override
		public final MapLayerView getMap() {
			return MapLayers.SURFACE;
		}
		@Override
		public final int getLayer() {
			return 1;
		}
	},
	CAVE {
		@Override
		public final MapLayerView getMap() {
			return MapLayers.CAVE;
		}
		@Override
		public final int getLayer() {
			return -1;
		}
	};
	
	public abstract MapLayerView getMap();
	public abstract int getLayer();
	
	public static MapLayer getByElevation(int layer) {
		if (layer >= 0) return SURFACE;
		return CAVE;
	}
}
