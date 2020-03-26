package org.gotti.wurmonline.clientmods.livehudmap.assets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileData {
    private List<TileEntityData> players   = new ArrayList<>();
    private List<TileEntityData> vehicles  = new ArrayList<>();
    private List<TileEntityData> monsters  = new ArrayList<>();
    private List<TileEntityData> creatures = new ArrayList<>();
    private List<TileEntityData> objects   = new ArrayList<>();
    
    private StructureType topStructure = StructureType.GROUND;
    private List<TileStructureData> bridges = new ArrayList<>();
    private TileStructureData building = null;
    
    public boolean add(AbstractTileData data) {
        if (data instanceof TileEntityData) return this.addEntity((TileEntityData) data);
        else if (data instanceof TileStructureData) return this.addStructure((TileStructureData) data);
        return false;
    }
    public boolean addEntity(TileEntityData entity) {
        if (entity.isPlayer()) return this.players.add(entity);
        else if (entity.isVehicle()) return this.vehicles.add(entity);
        else if (entity.isCreature()) return this.creatures.add(entity);
        else if (entity.isMonster()) return this.monsters.add(entity);
        else return this.objects.add(entity);
    }
    public boolean addStructure(TileStructureData structure) {
        if (structure.isBridge()) return this.bridges.add(structure);
        else if (structure.isBuilding()) {
            this.building = structure;
            return true;
        }
        return false;
    }
    
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
    public StructureType topStructure() {
        return this.topStructure;
    }
    public List<TileStructureData> getBridges() {
        return this.bridges;
    }
    public List<TileStructureData> getBuildings() {
        if (this.building == null)
            return Collections.emptyList();
        return Collections.singletonList( this.building );
    }
}
