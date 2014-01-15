package net.bytten.metazelda.constraints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.bytten.metazelda.IDungeon;
import net.bytten.metazelda.util.Coords;
import net.bytten.metazelda.util.CoordsMap;
import net.bytten.metazelda.util.Direction;
import net.bytten.metazelda.util.IntMap;
import net.bytten.metazelda.util.Pair;

/**
 * Limits the {@link net.bytten.metazelda.generators.IDungeonGenerator} in
 * the <i>number</i> of keys, switches and rooms it is allowed to place.
 * 
 * Also restrict to a grid of 1x1 rooms.
 * 
 * @see IDungeonConstraints
 */
public class CountConstraints implements IDungeonConstraints {

    protected int maxSpaces, maxKeys, maxSwitches;
    
    protected IntMap<Coords> gridCoords;
    protected CoordsMap<Integer> roomIds;
    protected int firstRoomId;
    
    public CountConstraints(int maxSpaces, int maxKeys, int maxSwitches) {
        this.maxSpaces = maxSpaces;
        this.maxKeys = maxKeys;
        this.maxSwitches = maxSwitches;

        gridCoords = new IntMap<Coords>();
        roomIds = new CoordsMap<Integer>();
        Coords first = new Coords(0,0);
        firstRoomId = getRoomId(first);
    }
    
    public int getRoomId(Coords xy) {
        if (roomIds.containsKey(xy)) {
            assert gridCoords.get(roomIds.get(xy)).equals(xy);
            return roomIds.get(xy);
        } else {
            int id = gridCoords.newInt();
            gridCoords.put(id, xy);
            roomIds.put(xy, id);
            return id;
        }
    }
    
    public Coords getRoomCoords(int id) {
        assert gridCoords.containsKey(id);
        return gridCoords.get(id);
    }
    
    @Override
    public int getMaxRooms() {
        return maxSpaces;
    }
    
    public void setMaxSpaces(int maxSpaces) {
        this.maxSpaces = maxSpaces;
    }
    
    @Override
    public Collection<Integer> initialRooms() {
        return Arrays.asList(firstRoomId);
    }

    @Override
    public int getMaxKeys() {
        return maxKeys;
    }
    
    public void setMaxKeys(int maxKeys) {
        this.maxKeys = maxKeys;
    }
    
    @Override
    public boolean isAcceptable(IDungeon dungeon) {
        return true;
    }

    @Override
    public int getMaxSwitches() {
        return maxSwitches;
    }

    public void setMaxSwitches(int maxSwitches) {
        this.maxSwitches = maxSwitches;
    }

    protected boolean validRoomCoords(Coords c) {
        return c.y <= 0;
    }
    
    @Override
    public List<Pair<Double,Integer>> getAdjacentRooms(int id) {
        Coords xy = gridCoords.get(id);
        List<Pair<Double,Integer>> ids = new ArrayList<Pair<Double,Integer>>();
        for (Direction d: Direction.values()) {
            Coords neighbor = xy.add(d.x, d.y);
            if (validRoomCoords(neighbor)) ids.add(
                    new Pair<Double,Integer>(1.0,getRoomId(neighbor)));
        }
        return ids;
    }

    @Override
    public Set<Coords> getCoords(int id) {
        return new TreeSet<Coords>(Arrays.asList(getRoomCoords(id)));
    }

    @Override
    public double edgeGraphifyProbability(int id, int nextId) {
        return 0.2;
    }

}
