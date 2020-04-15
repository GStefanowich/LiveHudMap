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

public class SklotopolisServer extends Server {
    
    
    private final Map<Coordinate, TileDeedData> deedBorders;
    private final Map<Coordinate, SklotopolisDeed> serverDeeds;
    
    public SklotopolisServer(String name) {
        super(name);
        this.serverDeeds = new ConcurrentHashMap<>();
        this.deedBorders = new ConcurrentHashMap<>();
    }
    
    /*
     * Initialize the world
     */
    @Override
    public void initialize(World world) {
        super.initialize(world);
        
        this.loadDeeds();
    }
    @Override
    public void deInitialize() {
        this.serverDeeds.clear();
    }
    
    private void loadDeeds() {
        if (this.getDeedURL() == null)
            return;
        this.serverDeeds.clear();
        LiveMap.threadExecute(() -> {
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
                LiveHudMapMod.log(e);
            }
        });
    }
    private void addDeed(SklotopolisDeed deed) {
        Coordinate nw = deed.getPerimeterNorthWest();
        Coordinate se = deed.getPerimeterSouthEast();
        for (int x = nw.getX(); x <= se.getX(); x++) {
            for (int y = nw.getY(); y <= se.getY(); y++) {
                Coordinate c = Coordinate.of( x, y );
                
                this.serverDeeds.put(c, deed);
                
                TileDeedData tileData = deed.getTileData( c );
                if ( tileData != null )
                    this.deedBorders.put(c, tileData);
            }
        }
    }
    
    /*
     * Check for deeds
     */
    public boolean isDeedBorder(Coordinate pos) {
        return this.getDeedBorder(pos).isPresent();
    }
    public boolean isDeeded(Coordinate pos) {
        return this.getDeed(pos).isPresent();
    }
    
    public Optional<SklotopolisDeed> getDeed(Coordinate position) {
        SklotopolisDeed found = null;
        if (LiveMap.SHOW_DEEDS)
            found = this.serverDeeds.get( position );
        return Optional.ofNullable( found );
    }
    public Optional<TileDeedData> getDeedBorder(Coordinate position) {
        TileDeedData found = null;
        if (LiveMap.SHOW_DEEDS)
            found = this.deedBorders.get( position );
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
