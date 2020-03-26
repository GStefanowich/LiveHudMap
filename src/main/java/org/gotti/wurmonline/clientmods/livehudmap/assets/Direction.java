package org.gotti.wurmonline.clientmods.livehudmap.assets;

import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;

public enum Direction {
    NORTH( 0, -1 ) {
        @Override
        public Direction opposite() {
            return SOUTH;
        }
    },
    EAST( 1, 0 ) {
        @Override
        public Direction opposite() {
            return WEST;
        }
    },
    SOUTH( 0, 1 ) {
        @Override
        public Direction opposite() {
            return NORTH;
        }
    },
    WEST( -1 , 0 ) {
        @Override
        public Direction opposite() {
            return EAST;
        }
    };
    
    private final int x;
    private final int y;
    
    Direction(int xOffset, int yOffset) {
        this.x = xOffset;
        this.y = yOffset;
    }
    
    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    
    public abstract Direction opposite();
    public static Direction fromRotation(float rotation) {
        if (rotation < 45f)
            return Direction.SOUTH;
        if (rotation < 135)
            return Direction.EAST;
        if (rotation < 225)
            return Direction.NORTH;
        if (rotation < 315)
            return Direction.WEST;
        return Direction.SOUTH;
    }
}
