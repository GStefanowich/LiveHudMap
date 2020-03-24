package org.gotti.wurmonline.clientmods.livehudmap;

import java.awt.image.BufferedImage;

import org.gotti.wurmonline.clientmods.livehudmap.renderer.MapRenderer;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.RenderType;

import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.math.FastMath;

public final class MapLayerView {
	
	private World world;
	private RenderType type;
	private MapRenderer renderer;
	
	private int zoom;
	
	public MapLayerView(World world, RenderType renderType) {
		this.world = world;
		this.zoom = 1;
		this.type = renderType;
		this.renderer = this.type.createMapRenderer(world);
	}
	
	public void zoomIn() {
		if (this.type.getMapSize() / this.zoom > 4) this.zoom *= 2;
	}
	public void zoomOut() {
		if (this.zoom > 1) this.zoom /= 2;
	}
	public int getZoom() {
		return this.zoom;
	}
	
	public BufferedImage render(int playerX, int playerY) {
		int area = this.type.getMapSize() / this.zoom;
		return this.renderer.createMapDump(playerX - area / 2, playerY - area / 2, area, area, playerX, playerY);
	}
	
	public void setRenderer(RenderType renderType) {
		if (renderType != this.type) {
			this.type = renderType;
			this.renderer = this.type.createMapRenderer( this.world );
			if (this.type.getMapSize() / this.zoom < 4) {
				this.zoom = FastMath.nearestPowerOfTwo(this.type.getMapSize() / 4);
			}
			if (this.zoom == 0) {
				this.zoom = 1;
			}
		}
	}
	public RenderType getRenderer() {
		return this.type;
	}
	
	public void pick(PickData pickData, float xMouse, float yMouse) {
		final int sz = this.type.getMapSize() / this.zoom;
		final PlayerPosition pos = this.world.getPlayer().getPos();
		this.renderer.pick(pickData, xMouse, yMouse, sz, sz, pos.getTileX(), pos.getTileY());
	}
	
}
