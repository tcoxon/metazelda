package net.bytten.metazelda.algo;

import java.util.Random;

public class Room {

    public final Coords coords;
    protected Element item;
    protected Edge[] edges; // index with Direction.{N,E,S,W}
    
    public Room(Coords coords) {
        this.coords = coords;
        this.item = null;
        this.edges = new Edge[Direction.NUM_DIRS];
        // all edges initially null
    }
    
    public Room(int x, int y) {
        this(new Coords(x,y));
    }

    public Element getItem() {
        return item;
    }

    public void setItem(Element item) {
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
}
