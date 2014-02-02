package net.bytten.metazelda.constraints;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.bytten.metazelda.util.Coords;
import net.bytten.metazelda.util.Direction;
import net.bytten.metazelda.util.GenerationFailureException;

public class ColorMap {
    
    protected int xsum, ysum, xmin, xmax, ymin, ymax;
    protected Map<Coords, Integer> map;

    public ColorMap() {
        map = new TreeMap<Coords,Integer>();
        ymin = xmin = Integer.MAX_VALUE;
        ymax = xmax = Integer.MIN_VALUE;
    }
    
    public void set(int x, int y, int color) {
        Coords xy = new Coords(x,y);
        if (map.get(xy) == null) {
            xsum += x;
            ysum += y;
        }
        map.put(xy, color);
        
        if (x < xmin) xmin = x;
        if (x > xmax) xmax = x;
        if (y < ymin) ymin = y;
        if (y > ymax) ymax = y;
    }
    
    public Integer get(int x, int y) {
        return map.get(new Coords(x,y));
    }
    
    public Coords getCenter() {
        return new Coords(xsum/map.size(), ysum/map.size());
    }
    
    public int getWidth() {
        return xmax-xmin+1;
    }
    
    public int getHeight() {
        return ymax-ymin+1;
    }
    
    public int getLeft() {
        return xmin;
    }
    
    public int getTop() {
        return ymin;
    }
    
    public int getRight() {
        return xmax;
    }
    
    public int getBottom() {
        return ymax;
    }
    
    protected boolean isConnected() {
        if (map.size() == 0) return false;
        
        // Do a breadth first search starting at the top left to check if
        // every position is reachable.
        Set<Coords> world = new TreeSet<Coords>(map.keySet()),
                    queue = new TreeSet<Coords>();
        
        Coords first = world.iterator().next();
        world.remove(first);
        queue.add(first);
        
        while (!queue.isEmpty()) {
            Coords pos = queue.iterator().next();
            queue.remove(pos);
            
            for (Direction d: Direction.values()) {
                Coords neighbor = pos.add(d.x,d.y);
                
                if (world.contains(neighbor)) {
                    world.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        return world.size() == 0;
    }
    
    public void checkConnected() {
        if (!isConnected()) {
            // Parts of the map are unreachable!
            throw new GenerationFailureException("ColorMap is not fully connected");
        }
    }
    
}
