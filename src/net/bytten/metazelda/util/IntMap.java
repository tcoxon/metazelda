package net.bytten.metazelda.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class IntMap<V> implements Map<Integer,V> {

    protected List<V> values;
    
    public IntMap() {
        values = new ArrayList<V>();
    }
    
    public V get(int k) {
        assert k >= 0;
        if (k >= values.size()) return null;
        return values.get(k);
    }
    
    public void put(int k, V val) {
        assert k >= 0;
        while (k >= values.size())
            values.add(null);
        values.set(k, val);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public boolean containsKey(Object arg0) {
        if (!(arg0 instanceof Integer)) return false;
        int k = (Integer)arg0;
        return 0 <= k && k < values.size() && values.get(k) != null;
    }

    @Override
    public boolean containsValue(Object arg0) {
        return values.contains(arg0);
    }

    @Override
    public Set<java.util.Map.Entry<Integer, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(Object arg0) {
        if (!(arg0 instanceof Integer)) throw new IllegalArgumentException();
        int k = (Integer)arg0;
        return get(k);
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public Set<Integer> keySet() {
        Set<Integer> keys = new TreeSet<Integer>();
        for (int i = 0; i < values.size(); ++i) {
            if (get(i) != null) keys.add(i);
        }
        return keys;
    }

    @Override
    public V put(Integer arg0, V arg1) {
        put(arg0, arg1);
        return arg1;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends V> arg0) {
        for (int k: arg0.keySet()) {
            put(k, arg0.get(k));
        }
    }

    @Override
    public V remove(Object arg0) {
        V val = get((Integer)arg0);
        put((Integer)arg0, null);
        return val;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public Collection<V> values() {
        return values;
    }
}
