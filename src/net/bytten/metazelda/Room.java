package net.bytten.metazelda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Room {

    public Condition precond;
    public final Coords coords;
    protected Symbol item;
    protected Edge[] edges; // index with Direction.{N,E,S,W}
    protected double intensity;
    protected Room parent;
    protected List<Room> children;
    
    public Room(Coords coords, Room parent, Symbol item, Condition precond) {
        this.coords = coords;
        this.item = item;
        this.edges = new Edge[Direction.NUM_DIRS];
        this.precond = precond;
        this.intensity = 0.0;
        this.parent = parent;
        this.children = new ArrayList<Room>(3);
        // all edges initially null
    }
    
    public Room(int x, int y, Room parent, Symbol item, Condition precond) {
        this(new Coords(x,y), parent, item, precond);
    }
    
    public double getIntensity() {
        return intensity;
    }
    
    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    public Symbol getItem() {
        return item;
    }

    public void setItem(Symbol item) {
        this.item = item;
    }

    public Edge[] getEdges() {
        return edges;
    }
    
    public Edge getEdge(Direction d) {
        return edges[d.code];
    }
    
    public int linkCount() {
        int result = 0;
        for (int d = 0; d < Direction.NUM_DIRS; ++d) {
            if (edges[d] != null)
                ++result;
        }
        return result;
    }
    
    public boolean isStart() {
        return item != null && item.isStart();
    }
    
    public boolean isGoal() {
        return item != null && item.isGoal();
    }
    
    public boolean isBoss() {
        return item != null && item.isBoss();
    }
    
    public boolean isSwitch() {
        return item != null && item.isSwitch();
    }
    
    public Condition getPrecond() {
        return precond;
    }
    
    public void setPrecond(Condition precond) {
        this.precond = precond;
    }

    public Room getParent() {
        return parent;
    }

    public void setParent(Room parent) {
        this.parent = parent;
    }
    
    public Collection<Room> getChildren() {
        return children;
    }
    
    public void addChild(Room child) {
        children.add(child);
    }
    
    public String toString() {
        return "Room(" + coords.toString() + ")";
    }
    
}
