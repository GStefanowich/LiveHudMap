package org.gotti.wurmonline.clientmods.livehudmap.assets;

import org.json.JSONObject;

public final class SklotopolisDeed {
    
    private final Coordinate deedToken;
    private final Edge deedEdge = new Edge();
    
    private String deedName;
    private Kingdoms kingdom;
    
    private boolean isSpawn = false;
    
    public SklotopolisDeed(JSONObject blob) {
        this.deedToken = Coordinate.of(blob.getInt("x"), blob.getInt("y")); // Set the deed token
        this.update( blob ); // Call the update (Non final objects)
        /*{"name": "Port Malone","tag": "port-malone-2443","mayor": "Backpfeifengesicht","tilesNorth": 51,"tilesSouth": 81,"tilesEast": 146,"tilesWest": 59,"tilesPerimeter": 5,"guards": 0,"kingdom": 4,"isSpawnPoint": false,"type": "large","lastActive": "Last active: 0 days ago.","x": 2443,"y": 3496}*/
    }
    public void update(JSONObject blob) {
        // Update the deeds name
        this.deedName = blob.getString("name");
        
        // Update the deeds size
        this.deedEdge.setA(this.deedToken.offset(
            Direction.NORTH,
            blob.getInt("tilesNorth"),
            Direction.WEST,
            blob.getInt("tilesWest")
        ));
        this.deedEdge.setB(this.deedToken.offset(
            Direction.SOUTH,
            blob.getInt("tilesSouth"),
            Direction.EAST,
            blob.getInt("tilesEast")
        ));
        
        // If spawn point
        this.isSpawn = blob.getBoolean("isSpawnPoint");
        
        // Set the kingdom
        this.kingdom = Kingdoms.getById(blob.getInt("kingdom"));
    }
    
    public String getName() {
        return this.deedName;
    }
    public Kingdoms getKingdom() {
        return this.kingdom;
    }
    public String getDimensions() {
        Coordinate size = this.getNorthWest().sub(this.getSouthEast());
        return (Math.abs(size.getX()) + 1) + "x" + (Math.abs(size.getY()) + 1);
    }
    
    public Edge getDeedEdge() {
        return this.deedEdge;
    }
    public Coordinate getCenter() {
        return this.deedToken;
    }
    public Coordinate getNorthWest() {
        return this.getDeedEdge().getA();
    }
    public Coordinate getSouthEast() {
        return this.getDeedEdge().getB();
    }
    
    public boolean isSpawn() {
        return this.isSpawn;
    }
}
