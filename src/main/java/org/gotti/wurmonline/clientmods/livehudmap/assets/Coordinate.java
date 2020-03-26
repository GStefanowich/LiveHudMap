package org.gotti.wurmonline.clientmods.livehudmap.assets;

import com.wurmonline.client.game.PlayerPosition;

import java.util.Objects;

public final class Coordinate {
    
    private static final Coordinate ROOT = new Coordinate( 0, 0 );
    private final int x;
    private final int y;
    
    private Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    
    public Coordinate add(Coordinate pos) {
        return this.add(pos.getX(), pos.getY());
    }
    public Coordinate add(int x, int y) {
        return Coordinate.of(
            this.getX() + x,
            this.getY() + y
        );
    }
    public Coordinate sub(Coordinate pos) {
        return this.sub(pos.getX(), pos.getY());
    }
    public Coordinate sub(int x, int y) {
        return Coordinate.of(
            this.getX() - x,
            this.getY() - y
        );
    }
    
    public Coordinate offset(Direction direction) {
        return this.offset( direction, 1 );
    }
    public Coordinate offset(Direction direction, int count) {
        return new Coordinate(this.getX() + (direction.getX() * count), this.getY() + (direction.getY() * count));
    }
    public Coordinate offset(Direction dirA, int cA, Direction dirB, int cB) {
        return new Coordinate(
            this.getX()
                + (dirA.getX() * cA)
                + (dirB.getX() * cB),
            this.getY()
                + (dirA.getY() * cA)
                + (dirB.getY() * cB)
        );
    }
    
    public static Coordinate zero() {
        return Coordinate.ROOT;
    }
    public static Coordinate of(int x, int y) {
        return new Coordinate(x, y);
    }
    public static Coordinate of(float x, float y) {
        return Coordinate.of((int)x, (int)y);
    }
    public static Coordinate of(PlayerPosition pos) {
        return Coordinate.of(pos.getTileX(), pos.getTileY());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Coordinate))
            return false;
        Coordinate pos = (Coordinate)obj;
        return (pos.getX() == this.getX()) && (pos.getY() == this.getY());
    }
    @Override
    public int hashCode() {
        return Objects.hash( this.getX(), this.getY() );
    }
    
}
