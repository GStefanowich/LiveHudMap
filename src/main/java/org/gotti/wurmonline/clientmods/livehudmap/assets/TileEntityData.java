/*
 * This software is licensed under the MIT License
 * https://github.com/GStefanowich/LiveHudMap
 *
 * Copyright (c) 2019 Gregory Stefanowich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.gotti.wurmonline.clientmods.livehudmap.assets;

import com.wurmonline.client.game.PlayerObj;
import com.wurmonline.client.renderer.GroundItemData;
import com.wurmonline.client.renderer.cell.CreatureCellRenderable;
import com.wurmonline.client.renderer.cell.PlayerCellRenderable;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TileEntityData extends AbstractTileData<List<Coordinate>> {
    
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
    @Override
    public AbstractTileType getType() {
        return AbstractTileType.GROUND;
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
