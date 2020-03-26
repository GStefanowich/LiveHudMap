package org.gotti.wurmonline.clientmods.livehudmap.assets;

import com.wurmonline.client.game.PlayerObj;
import com.wurmonline.client.renderer.GroundItemData;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;
import com.wurmonline.client.renderer.cell.GroundItemCellRenderable;
import com.wurmonline.client.renderer.cell.PlayerCellRenderable;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TileEntityData extends AbstractTileData<List<Coordinate>> {
    
    private final String name;
    private final List<Coordinate> pos;
    private final float layer;
    private final EntityType type;
    
    public TileEntityData(EntityType type, CreatureCellRenderable creature) {
        this.name  = (creature instanceof PlayerCellRenderable ? creature.getCreatureData().getName() : creature.getHoverName());
        this.pos   = TileEntityData.sizeUp(creature);
        this.layer = creature.getHPos();
        this.type  = type;
    }
    public TileEntityData(EntityType type, GroundItemData groundItem) {
        this.name = groundItem.getName();
        this.pos  = Collections.singletonList(Coordinate.of(
            groundItem.getX() / 4,
            groundItem.getY() / 4
        ));
        this.layer = groundItem.getH();
        this.type  = type;
    }
    public TileEntityData(PlayerObj player) {
        this.name  = player.getPlayerName();
        this.pos   = Collections.singletonList(Coordinate.of( player.getPos() ));
        this.layer = player.getPos().getH();
        this.type  = EntityType.PLAYER;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public List<Coordinate> getPos() {
        return this.pos;
    }
    @Override
    public float getHeight() {
        return this.layer;
    }
    
    public boolean isPlayer() {
        return this.type.getParent() == EntityFilter.PLAYER;
    }
    public boolean isCreature() {
        return this.type.getParent() == EntityFilter.CREATURE;
    }
    public boolean isMonster() {
        return this.type.getParent() == EntityFilter.HOSTILE;
    }
    public boolean isVehicle() {
        return this.type.getParent() == EntityFilter.VEHICLE;
    }
    
    @Override
    public Color getColor() {
        return this.type.getColor();
    }
    
    private static List<Coordinate> sizeUp(CreatureCellRenderable creature) {
        Coordinate position = Coordinate.of(
            creature.getXPos() / 4,
            creature.getYPos() / 4
        );
        
        // If creature is not a large boat, it's only a single tile
        if (!creature.getModelName().toString().startsWith("model.structure.boat"))
            return Collections.singletonList(position);
        
        // Get where the large entity is facing
        Direction facing = Direction.fromRotation(creature.getRot());
        
        // Return the list of positions
        return Arrays.asList(
            position,
            position.offset(facing),
            position.offset(facing.opposite())
        );
    }
}
