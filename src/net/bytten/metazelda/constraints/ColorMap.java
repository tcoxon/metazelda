package net.bytten.metazelda.constraints;

import java.util.Map;
import java.util.TreeMap;

import net.bytten.metazelda.util.Coords;

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
    
}
