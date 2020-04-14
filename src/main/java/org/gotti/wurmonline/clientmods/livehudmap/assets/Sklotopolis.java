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

import java.util.Collection;
import java.util.HashSet;

public class Sklotopolis {
    public static Collection<SklotopolisServer> SERVERS = new HashSet<>();
    
    public static final SklotopolisServer NOVUS;
    public static final SklotopolisServer LIBERTY;
    public static final SklotopolisServer CAZA;
    
    private static SklotopolisServer ACTIVE = null;
    
    private static SklotopolisServer addServer(SklotopolisServer server) {
        Sklotopolis.SERVERS.add( server );
        return server;
    }
    public static SklotopolisServer getServerByName( String name ) {
        // Get the server by it's name
        for (SklotopolisServer server : SERVERS) if ( server.getName().equalsIgnoreCase( name ) )
            return Sklotopolis.setActive(server);
        return null;
    }
    
    private static SklotopolisServer setActive(SklotopolisServer server) {
        // DeInitialize a previous server to save RAM (For server swapping)
        if (Sklotopolis.ACTIVE != null)
            Sklotopolis.ACTIVE.deInitialize();
        // Set the new active server
        return (Sklotopolis.ACTIVE = server);
    }
    public static SklotopolisServer getActive() {
        return Sklotopolis.ACTIVE;
    }
    
    static {
        NOVUS = addServer(new SklotopolisServer("Novus") {
            @Override public String getMapURL() { return "https://andistyr.github.io/wu-map/14821/"; }
            @Override public String getDeedURL() { return "https://sklotopolis.ddns.net/unlimited/3/deeds.json"; }
        });
        LIBERTY = addServer(new SklotopolisServer("Liberty") {
            @Override public String getMapURL() { return "https://andistyr.github.io/wu-map/14816/"; }
            @Override public String getDeedURL() { return "https://sklotopolis.ddns.net/unlimited/2/deeds.json"; }
        });
        CAZA = addServer(new SklotopolisServer("Caza"));
    }
}
