package org.gotti.wurmonline.clientmods.livehudmap;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;

import org.gotti.wurmonline.clientmods.livehudmap.assets.AbstractTileData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileData;
import org.gotti.wurmonline.clientmods.livehudmap.assets.TileEntityData;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.MapRenderer;
import org.gotti.wurmonline.clientmods.livehudmap.renderer.RenderType;

import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.PickData;
import com.wurmonline.math.FastMath;

public final class MapLayerView {
	private final HashMap<Coordinate, TileData> structureLayer = new HashMap<>();
	private final HashMap<Coordinate, TileData> entityLayer = new HashMap<>();
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
	
	public void tooltip(final LiveMap map, final PickData pickData, float xMouse, float yMouse) {
		this.renderer.tooltip(
			map,
			pickData,
			this.mousePosToCoordinate(map, xMouse, yMouse),
			map.getPlayerPosition()
		);
	}
	public Coordinate mousePosToCoordinate(final LiveMap map, final float xMouse, final float yMouse) {
		return this.mousePosToCoordinate(map.getCurrentMapCenter(), xMouse, yMouse);
	}
	public Coordinate mousePosToCoordinate(final Coordinate center, final float xMouse, final float yMouse) {
		final int size = this.type.getMapSize() / this.zoom;
		
		// Offset cursor and window to get tile pos
		return Coordinate.of(
			center.getX() + (int)(xMouse * size) - size / 2,
			center.getY() + (int)(yMouse * size) - size / 2
		);
	}
	
	public void clearEntities() {
		this.entityLayer.clear();
	}
	public TileData getStructureLayer(Coordinate pos) {
		boolean contains;
		
		TileData tile = this.structureLayer.get( pos );
		
		// Create a new Tile Data if it is not in the map
		if (!(contains = tile != null))
			tile = new TileData( pos );
		
		// Save the tile if it does not exist
		if (!contains)
			this.setStructureLayer( pos, tile );
		
		return tile;
	}
	private TileData setStructureLayer(Coordinate pos, TileData tile) {
		return this.structureLayer.put( pos, tile );
	}
	
	public TileData getEntityLayer(Coordinate pos) {
		boolean contains;
		
		TileData tile = this.entityLayer.get( pos );
		
		// Create a new Tile Data if it is not in the map
		if (!(contains = tile != null))
			tile = new TileData( pos );
		
		// Save the tile if it does not exist
		if (!contains)
			this.setEntityLayer( pos, tile );
		
		return tile;
	}
	private TileData setEntityLayer(Coordinate pos, TileData tile) {
		return this.entityLayer.put(pos, tile);
	}
	
	public boolean addToTile(Coordinate pos, AbstractTileData data) {
		if (data instanceof TileEntityData) return this.getEntityLayer(pos).add( data );
		return this.getStructureLayer(pos).add( data );
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
