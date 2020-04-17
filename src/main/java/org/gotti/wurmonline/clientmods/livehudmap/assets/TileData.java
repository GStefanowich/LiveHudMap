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

import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Deprecated
public class TileData {
    private boolean dirty = true;
    private final Coordinate pos;
    
    public TileData(Coordinate coordinate) {
        this.pos = coordinate;
    }
    
    private List<TileEntityData> players   = new ArrayList<>();
    private List<TileEntityData> vehicles  = new ArrayList<>();
    private List<TileEntityData> monsters  = new ArrayList<>();
    private List<TileEntityData> creatures = new ArrayList<>();
    private List<TileEntityData> objects   = new ArrayList<>();
    
    //private AbstractTileData top = null;
    //private AbstractTileType topStructure = AbstractTileType.GROUND;
    //private List<TileStructureData> bridges = new ArrayList<>();
    //private TileStructureData building = null;
    
    public boolean add(AbstractTileData data) {
        try {
            if (data instanceof TileEntityData) return this.addEntity((TileEntityData) data);
            //else if (data instanceof TileStructureData) return this.addStructure((TileStructureData) data);
            return false;
        } finally {
            this.markDirty();
        }
    }
    private boolean addEntity(TileEntityData entity) {
        if (entity.isPlayer()) return this.players.add(entity);
        else if (entity.isVehicle()) return this.vehicles.add(entity);
        else if (entity.isCreature()) return this.creatures.add(entity);
        else if (entity.isMonster()) return this.monsters.add(entity);
        else return this.objects.add(entity);
    }
    /*private boolean addStructure(TileStructureData structure) {
        if (structure.isBridge()) return this.bridges.add(structure);
        else if (structure.isBuilding()) {
            boolean update;
            if(update = this.building != structure)
                this.building = structure;
            return update;
        }
        return false;
    }*/
    
    /*
     * Entities
     */
    public List<TileEntityData> getPlayers() {
        return this.players;
    }
    public List<TileEntityData> getVehicles() {
        return this.vehicles;
    }
    public List<TileEntityData> getCreatures() {
        return this.creatures;
    }
    public List<TileEntityData> getMonsters() {
        return this.monsters;
    }
    public List<TileEntityData> getObjects() {
        return this.objects;
    }
    
    /*
     * Structures
     */
    /*public AbstractTileData getTop() {
        if (this.dirty || this.topStructure == null) {
            this.top = this.computeTop();
            this.topStructure = (this.top == null ? AbstractTileType.GROUND : this.top.getType());
            this.dirty = false;
        }
        return this.top;
    }*/
    /*public AbstractTileType getTopType() {
        return this.topStructure;
    }*/
    /*public List<TileStructureData> getBridges() {
        return this.bridges;
    }*/
    /*public List<TileStructureData> getBuildings() {
        if (this.building == null)
            return Collections.emptyList();
        return Collections.singletonList( this.building );
    }*/
    public Optional<TileDeedData> getDeed() {
        Server server = Servers.getServer();
        if (server instanceof SklotopolisServer)
            return ((SklotopolisServer) server).getDeedBorder( this.pos );
        return Optional.empty();
    }
    
    private AbstractTileData computeTop() {
        // If there are no entities, use the structure color
        return Stream.of( this.collect() )
            .flatMap(Collection::stream)
            .max((base, other) -> {
                if (base instanceof TilePlayerData)
                    return 1;
                return Double.compare(base.getHeight(), other.getHeight());
            }).orElse( null );
    }
    private List<AbstractTileData> collect() {
        List<AbstractTileData> list = new ArrayList<>();
        
        // Sift out the players from the tile
        if (LiveMap.SHOW_PLAYERS)
            list.addAll(this.players);
        
        // Sift out the vehicles from the tile
        if (LiveMap.SHOW_VEHICLES)
            list.addAll(this.vehicles);
        
        // Sift out the hostiles from the tile
        if (LiveMap.SHOW_HOSTILES)
            list.addAll(this.monsters);
        
        // Sift out the creatures from the tile
        if (LiveMap.SHOW_CREATURES)
            list.addAll(this.creatures);
        
        // Sift out the buildings from the tile
        /*if (LiveMap.SHOW_BUILDINGS && this.building != null)
            list.add(this.building);*/
        
        // Sift out the deeds from the tile
        Optional<TileDeedData> deed = this.getDeed();
        if (LiveMap.SHOW_DEEDS && deed.isPresent())
            list.add(deed.get());
        
        // Sift out the bridges from the tile
        /*list.addAll(this.bridges);*/
        
        // Sift out the objects from the tile
        list.addAll(this.objects);
        
        return list;
    }
    private void markDirty() {
        this.dirty = true;
    }
}
