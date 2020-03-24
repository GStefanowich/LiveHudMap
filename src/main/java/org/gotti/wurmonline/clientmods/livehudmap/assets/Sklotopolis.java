package org.gotti.wurmonline.clientmods.livehudmap.assets;

import java.util.Collection;
import java.util.HashSet;

public class Sklotopolis {
    public static Collection<SklotopolisServer> SERVERS = new HashSet<>();
    
    public static final SklotopolisServer NOVUS;
    public static final SklotopolisServer LIBERTY;
    public static final SklotopolisServer CAZA;
    
    private static SklotopolisServer addServer(SklotopolisServer server) {
        Sklotopolis.SERVERS.add( server );
        return server;
    }
    public static SklotopolisServer getServerByName( String name ) {
        for (SklotopolisServer server : SERVERS) {
            if ( server.getName().equalsIgnoreCase( name ) )
                return server;
        }
        return null;
    }
    
    static {
        NOVUS = addServer(new SklotopolisServer("Novus"));
        LIBERTY = addServer(new SklotopolisServer("Liberty"));
        CAZA = addServer(new SklotopolisServer("Caza"));
    }
}
