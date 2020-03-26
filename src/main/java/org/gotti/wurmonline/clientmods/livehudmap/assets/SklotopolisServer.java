package org.gotti.wurmonline.clientmods.livehudmap.assets;

import com.wurmonline.client.game.World;
import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;
import org.gotti.wurmonline.clientmods.livehudmap.LiveMap;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class SklotopolisServer {
    
    private World world = null;
    
    private final String serverName;
    private final Map<Coordinate, SklotopolisDeed> serverDeeds;
    
    public SklotopolisServer(String name) {
        this.serverName = name;
        this.serverDeeds = new ConcurrentHashMap<>();
    }
    
    /*
     * Getters
     */
    public String getName() {
        return this.serverName;
    }
    public World getWorld() {
        return this.world;
    }
    
    /*
     * Initialize deeds
     */
    public void initialize(World world) {
        this.world = world;
        
        //this.serverDeeds.clear();
        this.loadDeeds();
    }
    public void deInitialize() {
        this.serverDeeds.clear();
    }
    private void loadDeeds() {
        if (this.getDeedURL() == null)
            return;
        this.serverDeeds.clear();
        new Thread(() -> {
            try {
                Scanner scanner = new Scanner(new URL(this.getDeedURL()).openStream());
                String body = scanner.useDelimiter("\\Z").next();
                
                JSONArray list;
                if (body.startsWith("[") && body.endsWith("]")) {
                    list = new JSONArray( body );
                } else {
                    int s = body.indexOf("["),
                        e = body.lastIndexOf("]");
                    if (s < 0 || e < 0) return;
                    list = new JSONArray( body.substring( s, e + 1 ) );
                }
                
                for (int i = 0; i < list.length(); i++) {
                    this.addDeed(new SklotopolisDeed(list.getJSONObject( i )));
                }
                
                LiveHudMapMod.log("Updated " + this.serverDeeds.size() + " deeds for " + this.getName());
            } catch (IOException | JSONException e) {
                LiveHudMapMod.log( e );
            }
        }).start();
    }
    private void addDeed(SklotopolisDeed deed) {
        Coordinate nw = deed.getNorthWest();
        Coordinate se = deed.getSouthEast();
        for (int x = nw.getX(); x < se.getX(); x++) {
            for (int y = nw.getY(); y < se.getY(); y++) {
                this.serverDeeds.put(Coordinate.of( x, y ), deed);
            }
        }
    }
    
    /*
     * Check for deeds
     */
    public boolean isDeedBorder(Coordinate pos) {
        int sides = 0;
        if (this.isDeeded(pos.offset(Direction.NORTH))) sides++;
        if (this.isDeeded(pos.offset(Direction.EAST))) sides++;
        if (this.isDeeded(pos.offset(Direction.SOUTH))) sides++;
        if (this.isDeeded(pos.offset(Direction.WEST))) sides++;
        return sides == 1;
    }
    public boolean isDeeded(Coordinate pos) {
        return this.getDeed(pos).isPresent();
    }
    
    public Optional<SklotopolisDeed> getDeed(Coordinate position) {
        SklotopolisDeed found = null;
        if (LiveMap.SHOW_DEEDS)
            found = this.serverDeeds.get( position );
        /*if (LiveMap.SHOW_DEEDS) {
            for (SklotopolisDeed deed : this.serverDeeds.values()) {
                if (deed.getDeedEdge().isWithin(position)) {
                    found = deed;
                    break;
                }
            }
        }*/
        return Optional.ofNullable( found );
    }
    
    /*
     * Per-Server Information
     */
    public String getMapURL() {
        return null;
    }
    public final String getMapURL(Coordinate pos) {
        String url = this.getMapURL();
        if (url == null)
            return url;
        return url + "#" + pos.getX() + "_" + pos.getY();
    }
    public String getDeedURL() {
        return null;
    }
    
    /*
     * Highways
     */
    public boolean isHighway(Coordinate pos) {
        return false;
    }
    
}
