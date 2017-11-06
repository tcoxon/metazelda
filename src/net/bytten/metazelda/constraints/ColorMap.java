package net.bytten.metazelda.constraints;

import java.util.Map;
import java.util.Set;

import net.bytten.gameutil.Vec2I;
import net.bytten.gameutil.Vec2IMap;
import net.bytten.gameutil.Vec2ISet;
import net.bytten.gameutil.Direction;
import net.bytten.metazelda.util.GenerationFailureException;

public class ColorMap {
    
    protected int xsum, ysum, xmin, xmax, ymin, ymax;
    protected Map<Vec2I, Integer> map;

    public ColorMap() {
        map = new Vec2IMap<Integer>();
        ymin = xmin = Integer.MAX_VALUE;
        ymax = xmax = Integer.MIN_VALUE;
    }
    
    public void set(int x, int y, int color) {
        Vec2I xy = new Vec2I(x,y);
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
        return map.get(new Vec2I(x,y));
    }
    
    public Vec2I getCenter() {
        return new Vec2I(xsum/map.size(), ysum/map.size());
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
        Set<Vec2I> world = new Vec2ISet(map.keySet()),
                    queue = new Vec2ISet();
        
        Vec2I first = world.iterator().next();
        world.remove(first);
        queue.add(first);
        
        while (!queue.isEmpty()) {
            Vec2I pos = queue.iterator().next();
            queue.remove(pos);
            
            for (Direction d: Direction.CARDINALS) {
                Vec2I neighbor = pos.add(d);
                
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
