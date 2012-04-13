package net.bytten.metazelda;

public class Room {

    public Condition precond;
    public final Coords coords;
    protected Symbol item;
    protected Edge[] edges; // index with Direction.{N,E,S,W}
    protected double intensity;
    
    public Room(Coords coords, Symbol item, Condition precond) {
        this.coords = coords;
        this.item = item;
        this.edges = new Edge[Direction.NUM_DIRS];
        this.precond = precond;
        this.intensity = 0.0;
        // all edges initially null
    }
    
    public Room(int x, int y, Symbol item, Condition precond) {
        this(new Coords(x,y), item, precond);
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
    
    public Edge getEdge(int d) {
        return edges[d];
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
    
    public Condition getPrecond() {
        return precond;
    }
}
