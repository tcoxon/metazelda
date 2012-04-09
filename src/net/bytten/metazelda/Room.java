package net.bytten.metazelda;

import java.util.Random;

public class Room {

    public Condition precond;
    public final Coords coords;
    protected Symbol item;
    protected Edge[] edges; // index with Direction.{N,E,S,W}
    
    public Room(Coords coords, Symbol item, Condition precond) {
        this.coords = coords;
        this.item = item;
        this.edges = new Edge[Direction.NUM_DIRS];
        this.precond = precond;
        // all edges initially null
    }
    
    public Room(int x, int y, Symbol item, Condition precond) {
        this(new Coords(x,y), item, precond);
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
    
    public Edge getEdge(int d) {
        return edges[d];
    }
    
    public boolean isStart() {
        return item != null && item.isStart();
    }
    
    public boolean isGoal() {
        return item != null && item.isGoal();
    }
    
    public Integer getRandomFreeEdgeDirection(Random rand) {
        // Return a random direction for which there is no outgoing edge in the room
        int d = rand.nextInt(Direction.NUM_DIRS),
            tries = 0;
        while (edges[d] != null && tries < Direction.NUM_DIRS){
            d = (d+1) % Direction.NUM_DIRS;
            ++tries;
        }
        if (edges[d] == null)
            return d;
        return null;
    }

    public Condition getPrecond() {
        return precond;
    }
}
