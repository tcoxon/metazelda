package net.bytten.metazelda.util;

import java.util.TreeMap;

public class IntMap<V> extends TreeMap<Integer,V> {
    private static final long serialVersionUID = 1L;

    public int newInt() {
        int k = size();
        while (containsKey(k)) k++;
        return k;
    }
    
}
