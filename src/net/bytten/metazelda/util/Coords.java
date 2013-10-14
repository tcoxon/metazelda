package net.bytten.metazelda.util;

/**
 * An AWT-agnostic 2D coordinate class.
 * <p>
 * Provided so that metazelda may be used on platforms without AWT (e.g.
 * Android).
 */
public class Coords implements Comparable<Coords> {

    public final int x, y;
    
    /**
     * Create coordinates at the given X and Y position.
     * 
     * @param x the position along the left-right dimension
     * @param y the position along the top-bottom dimension
     */
    public Coords(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Gets the coordinates of the next space in the given direction
     * 
     * @param d the direction
     */
    public Coords nextInDirection(Direction d) {
        return add(d.x,d.y);
    }
    
    public Coords add(int dx, int dy) {
        return new Coords(x + dx, y + dy);
    }
    
    @Override
    public boolean equals(Object other) {
         if (other instanceof Coords) {
             Coords o = (Coords)other;
             return this.x == o.x && this.y == o.y;
         } else {
             return super.equals(other);
         }
    }

    @Override
    public int compareTo(Coords other) {
        // For Dungeon's TreeMap
        int d = this.x - other.x;
        if (d == 0) {
            d = this.y - other.y;
        }
        return d;
    }
    
    /**
     * Determines whether this Coords and another Coords are next to each other.
     * 
     * @param other the other Coords
     * @return whether they are adjacent
     */
    public boolean isAdjacent(Coords other) {
        int dx = Math.abs(x - other.x),
            dy = Math.abs(y - other.y);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    /**
     * Gets the direction from this Coords to another Coords.
     * 
     * @param other the other Coords
     * @return the direction the other Coords is in
     * @throws RuntimeException if the direction to the other Coords cannot be
     *                          described with compass directions, e.g. if it's
     *                          diagonal
     */
    public Direction getDirectionTo(Coords other) {
        int dx = x - other.x,
            dy = y - other.y;
        assert dx == 0 || dy == 0;
        if (dx < 0) return Direction.E;
        if (dx > 0) return Direction.W;
        if (dy < 0) return Direction.S;
        if (dy > 0) return Direction.N;
        throw new RuntimeException("Coords do not align in one dimension, or are equal");
    }
    
    public double distance(Coords other) {
        int dx = x - other.x,
            dy = y - other.y;
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    public String toString() {
        return x+","+y;
    }
}
