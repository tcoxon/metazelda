package net.bytten.metazelda.constraints;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import net.bytten.metazelda.IDungeon;
import net.bytten.metazelda.util.Coords;
import net.bytten.metazelda.util.Direction;
import net.bytten.metazelda.util.IntMap;

public class FreeformConstraints implements IDungeonConstraints {
    
    public static final int DEFAULT_MAX_KEYS = 8;
    
    protected static class Group {
        public int id;
        public Set<Coords> coords;
        public Set<Integer> adjacentGroups;
        
        public Group(int id) {
            this.id = id;
            this.coords = new TreeSet<Coords>();
            this.adjacentGroups = new TreeSet<Integer>();
        }
    }
    
    protected ColorMap colorMap;
    protected IntMap<Group> groups;
    protected int maxKeys;

    public FreeformConstraints(ColorMap colorMap) {
        this.colorMap = colorMap;
        this.groups = new IntMap<Group>();
        this.maxKeys = DEFAULT_MAX_KEYS;
        
        analyzeMap();
    }
    
    protected void analyzeMap() {
        for (int x = colorMap.getLeft(); x <= colorMap.getRight(); ++x)
            for (int y = colorMap.getTop(); y <= colorMap.getBottom(); ++y) {
                Integer val = colorMap.get(x,y);
                if (val == null) continue;
                Group group = groups.get(val);
                if (group == null) {
                    group = new Group(val);
                    groups.put(val, group);
                }
                group.coords.add(new Coords(x,y));
            }
        System.out.println(groups.size() + " groups");
        
        for (Group group: groups.values()) {
            for (Coords xy: group.coords) {
                for (Direction d: Direction.values()) {
                    Coords neighbor = xy.add(d.x, d.y);
                    if (group.coords.contains(neighbor)) continue;
                    Integer val = colorMap.get(neighbor.x, neighbor.y);
                    if (val != null) {
                        group.adjacentGroups.add(val);
                    }
                }
            }
        }
    }
    
    @Override
    public int getMaxRooms() {
        return groups.size();
    }

    @Override
    public int getMaxKeys() {
        return maxKeys;
    }
    
    public void setMaxKeys(int maxKeys) {
        this.maxKeys = maxKeys;
    }

    @Override
    public int getMaxSwitches() {
        return 0;
    }

    @Override
    public Collection<Integer> initialRooms() {
        Set<Integer> result = new TreeSet<Integer>();
        
        // TODO place the initial room elsewhere than the center?
        Coords center = colorMap.getCenter();
        result.add(colorMap.get(center.x, center.y));
        
        return result;
    }

    @Override
    public Collection<Integer> getAdjacentRooms(int id) {
        return Collections.unmodifiableSet(groups.get(id).adjacentGroups);
    }

    @Override
    public Set<Coords> getCoords(int id) {
        return Collections.unmodifiableSet(groups.get(id).coords);
    }

    @Override
    public boolean isAcceptable(IDungeon dungeon) {
        return true;
    }

}