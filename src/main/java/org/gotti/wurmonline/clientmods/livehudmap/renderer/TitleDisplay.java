package org.gotti.wurmonline.clientmods.livehudmap.renderer;

import com.wurmonline.client.game.SeasonManager.Season;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Coordinate;
import org.gotti.wurmonline.clientmods.livehudmap.assets.Server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
    private static String formatTime(long seconds) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        // TODO: Convert the Long to a Time of Day (8:00am/8:30am/9:00pm)
        return format.format(new Date(seconds * 1000L));
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
