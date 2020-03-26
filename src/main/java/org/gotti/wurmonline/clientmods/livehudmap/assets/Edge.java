package org.gotti.wurmonline.clientmods.livehudmap.assets;

import org.gotti.wurmonline.clientmods.livehudmap.LiveHudMapMod;

public final class Edge {
    private Coordinate pointA;
    private Coordinate pointB;
    
    public Edge() {
        this(Coordinate.zero(), Coordinate.zero());
    }
    public Edge(Coordinate a, Coordinate b) {
        this.setA( a );
        this.setB( b );
    }
    
    public void setA(Coordinate point) {
        this.pointA = point;
    }
    public void setB(Coordinate point) {
        this.pointB = point;
    }
    
    public Coordinate getA() {
        return this.pointA;
    }
    public Coordinate getB() {
        return this.pointB;
    }
    
    public boolean isWithin(Coordinate c) {
        int upperX = Math.max( this.pointA.getX(), this.pointB.getX() );
        int upperY = Math.max( this.pointA.getY(), this.pointB.getY() );
        
        int lowerX = Math.min( this.pointA.getX(), this.pointB.getX() );
        int lowerY = Math.min( this.pointA.getY(), this.pointB.getY() );
        
        return  ( c.getX() >= lowerX ) && ( c.getY() >= lowerY ) && ( c.getX() <= upperX ) && ( c.getY() <= upperY );
    }
    
}
