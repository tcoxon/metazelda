package net.bytten.metazelda.util;

import java.util.TreeMap;

public class CoordsMap<V> extends TreeMap<Coords, V> {

    public V get(int x, int y) {
        return get(new Coords(x,y));
    }
    
}
