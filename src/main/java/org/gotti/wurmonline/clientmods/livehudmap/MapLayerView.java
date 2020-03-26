package org.gotti.wurmonline.clientmods.livehudmap;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;

import org.gotti.wurmonline.clientmods.livehudmap.assets.AbstractTileData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileData;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.MapRenderer;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.RenderType;

import com.wurmonline.client.game.PlayerPosition;
import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.math.FastMath;

public final class MapLayerView {
	private HashMap<Coordinate, TileData> tileData = new HashMap<>();
	private World world;
	private RenderType type;
	private MapRenderer renderer;
	
	private int updateTimer = 0;
	private int zoom;
	
	public MapLayerView(World world, RenderType renderType) {
		this.zoom = 4;
		this.world = world;
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
	
	public BufferedImage render(LiveMap map, Coordinate playerPos) {
		int area = this.type.getMapSize() / this.zoom;
		return this.renderer.createMapDump( map, playerPos.getX() - area / 2, playerPos.getY() - area / 2, area, area, playerPos );
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
	
	public void tooltip(LiveMap map, PickData pickData, float xMouse, float yMouse) {
		final int size = this.type.getMapSize() / this.zoom;
		final Coordinate player = Coordinate.of(this.world.getPlayer().getPos());
		this.renderer.tooltip(
			map,
			pickData,
			Coordinate.of(
				player.getX() + (int)(xMouse * size) - size / 2,
				player.getY() + (int)(yMouse * size) - size / 2
			),
			player
		);
	}
	public Coordinate mousePosToCoordinate(final float xMouse, final float yMouse) {
		final int sz = this.type.getMapSize() / this.zoom;
		final PlayerPosition pos = this.world.getPlayer().getPos();
		// Offset cursor and window to get tile pos
		return Coordinate.of(
			pos.getTileX() + (int)(xMouse * sz) - sz / 2,
			pos.getTileY() + (int)(yMouse * sz) - sz / 2
		);
	}
	
	public void clearTiles() {
		this.tileData.clear();
	}
	public TileData getTile(Coordinate pos) {
		boolean contains;
		
		TileData tile = this.tileData.get( pos );
		
		// Create a new Tile Data if it is not in the map
		if (!(contains = tile != null))
			tile = new TileData();
		
		// Save the tile if it does not exist
		if (!contains)
			this.setTile( pos, tile );
		
		return tile;
	}
	public TileData setTile(Coordinate pos, TileData tile) {
		return this.tileData.put( pos, tile );
	}
	
	public boolean addToTile(Coordinate pos, AbstractTileData data) {
		return this.getTile(pos).add( data );
	}
	public void addToTile(Collection<Coordinate> positions, AbstractTileData data) {
		for (Coordinate pos : positions)
			this.addToTile(pos, data);
	}
	
	public boolean tick() {
		if ((--this.updateTimer) <= 0 ) {
			this.updateTimer = LiveMap.REFRESH_RATE;
			return true;
		}
		return false;
	}
}
