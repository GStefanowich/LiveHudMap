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
