package org.gotti.wurmonline.clientmods.livehudmap.assets;

import com.wurmonline.client.game.World;

public abstract class Server {
    protected World world = null;
    
    private final String serverName;
    
    public Server(String name) {
        this.serverName = name;
    }
    
    /*
     * Getters
     */
    public final String getName() {
        return this.serverName;
    }
    public final World getWorld() {
        return this.world;
    }
    
    public void initialize(World world) {
        this.world = world;
    }
    public void deInitialize() {
    }
    
}
