package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.SeasonManager.Season;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Server;

public final class TitleDisplay {
    private TitleDisplay() {}
    
    private static String formatServer(Server server) {
        if (server == null)
            return "";
        return server.getName() + ", ";
    }
    private static String formatSeason(Season season) {
        String name = season.name();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
    private static String formatTime(long time) {
        return String.valueOf(time);
    }
    private static String formatPos(Coordinate pos) {
        return pos.getX() + ", " + pos.getY();
    }
    
    public static String format(Server server, Season season, Coordinate pos, int zoom, long time) {
        return formatServer(server)
            + formatSeason(season)
            + " [" + formatTime(time) + "]: "
            + formatPos(pos)
            + " (" + zoom + "x)";
    }
    
}
