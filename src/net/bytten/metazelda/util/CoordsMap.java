package net.bytten.metazelda.util;

import java.util.TreeMap;

public class CoordsMap<V> extends TreeMap<Coords, V> {
	private static final long serialVersionUID = 1L;

	public V get(int x, int y) {
        return get(new Coords(x,y));
    }
    
}
