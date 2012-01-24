package net.bytten.metazelda.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class Dungeon {

    protected long seed;
    protected int itemCount;
    protected Map<Coords, Room> rooms;
    protected Bounds bounds;
    protected Set<Symbol> placedItems;
    
    // Used for getting external rooms:
    protected Map<Integer, Integer> minX, maxX, minY, maxY;
    
    public Dungeon(long seed) {
        rooms = new TreeMap<Coords, Room>();
        bounds = new Bounds(0,0,0,0);
        placedItems = new HashSet<Symbol>();
        this.seed = seed;
        
        minX = new HashMap<Integer,Integer>();
        maxX = new HashMap<Integer,Integer>();
        minY = new HashMap<Integer,Integer>();
        maxY = new HashMap<Integer,Integer>();
    }
    
    public Bounds getBounds() {
        return bounds;
    }
    
    public Collection<Room> getRooms() {
        return rooms.values();
    }
    
    public Symbol makeNewItem() {
        return new Symbol(itemCount++);
    }
    public int itemCount() {
        return itemCount;
    }
    public Symbol getRandomPlacedElement(Random rand) {
        if (placedItems.size() == 0) return null;
        return new ArrayList<Symbol>(placedItems)
            .get(rand.nextInt(placedItems.size()));
    }
    
    public int roomCount() {
        return rooms.size();
    }
    
    public Room get(Coords coords) {
        return rooms.get(coords);
    }
    
    public Room get(int x, int y) {
        return get(new Coords(x,y));
    }
    
    public void placeItem(Symbol e) {
        if (e != null && !e.isGoal() && !e.isStart())
            placedItems.add(e);
    }
    
    public void add(Room room) {
        rooms.put(room.coords, room);
        
        placeItem(room.getItem());
        
        if (room.coords.x < bounds.left) {
            bounds = new Bounds(room.coords.x, bounds.top,
                    bounds.right, bounds.bottom);
        }
        if (room.coords.x > bounds.right) {
            bounds = new Bounds(bounds.left, bounds.top,
                    room.coords.x, bounds.bottom);
        }
        if (room.coords.y < bounds.top) {
            bounds = new Bounds(bounds.left, room.coords.y,
                    bounds.right, bounds.bottom);
        }
        if (room.coords.y > bounds.bottom) {
            bounds = new Bounds(bounds.left, bounds.top,
                    bounds.right, room.coords.y);
        }
        
        updateBoundaryMin(minX, room.coords.y, room.coords.x);
        updateBoundaryMax(maxX, room.coords.y, room.coords.x);
        updateBoundaryMin(minY, room.coords.x, room.coords.y);
        updateBoundaryMax(maxY, room.coords.x, room.coords.y);
    }
    
    private void updateBoundaryMin(Map<Integer,Integer> minMap, int k, int v) {
        Integer cv = minMap.get(k);
        if (cv == null || v < cv) {
            minMap.put(k,v);
        }
    }
    
    private void updateBoundaryMax(Map<Integer,Integer> maxMap, int k, int v) {
        Integer cv = maxMap.get(k);
        if (cv == null || v > cv) {
            maxMap.put(k,v);
        }
    }
    
    private Room randBoundaryX(Random rand, Map<Integer,Integer> xmap) {
        List<Integer> keys = new ArrayList<Integer>(xmap.keySet());
        int y = keys.get(rand.nextInt(keys.size())),
            x = xmap.get(y);
        return get(x,y);
    }
    
    private Room randBoundaryY(Random rand, Map<Integer,Integer> ymap) {
        List<Integer> keys = new ArrayList<Integer>(ymap.keySet());
        int x = keys.get(rand.nextInt(keys.size())),
            y = ymap.get(x);
        return get(x,y);
    }
    
    public Room getRandomExternalRoom(Random rand) {
        // All this boundary stuff is a big hack.
        // Min and Max of each dimension are kept in maps. A random map is
        // selected, and from that a random key,value pair that represents a
        // pair of coordinates.
        if (roomCount() <= 0) return null;
        switch (rand.nextInt(4)) {
        case 0: return randBoundaryX(rand, minX);
        case 1: return randBoundaryX(rand, maxX);
        case 2: return randBoundaryY(rand, minY);
        case 3: return randBoundaryY(rand, maxY);
        default:
            throw new RuntimeException("The laws of probability have been rewritten");
        }
    }
    
    public void linkOneWay(Room room1, Room room2) {
        linkOneWay(room1, room2, null);
    }
    
    public void link(Room room1, Room room2) {
        link(room1, room2, null);
    }
    
    public void linkOneWay(Room room1, Room room2, Condition cond) {
        assert rooms.values().contains(room1) && rooms.values().contains(room2);
        assert room1.coords.isAdjacent(room2.coords);
        int d = room1.coords.getDirectionTo(room2.coords);
        room1.getEdges()[d] = new Edge(cond);
    }
    
    public void link(Room room1, Room room2, Condition cond) {
        assert rooms.values().contains(room1) && rooms.values().contains(room2);
        assert room1.coords.isAdjacent(room2.coords);
        int d = room1.coords.getDirectionTo(room2.coords);
        room1.getEdges()[d] = new Edge(cond);
        room2.getEdges()[Direction.oppositeDirection(d)] = new Edge(cond);
    }
    
    public Integer getRandomAdjacentSpaceDirection(Random rand, Room room) {
        // Return a random direction of travel from room to an adjacent empty
        // space, or null if there are no nearby spaces
        int d = rand.nextInt(Direction.NUM_DIRS),
            tries = 0;
        Coords xy = room.coords.nextInDirection(d);
        while (get(xy) != null && tries < Direction.NUM_DIRS) {
            d = (d+1) % Direction.NUM_DIRS;
            ++tries;
            xy = room.coords.nextInDirection(d);
        }
        if (get(xy) == null)
            return d;
        return null;
    }
    
    public static Dungeon makeTestDungeon() {
        Dungeon dungeon = new Dungeon(0);
        
        Symbol key = dungeon.makeNewItem(),
            feather = dungeon.makeNewItem(),
            boss = dungeon.makeNewItem(),
            goal = new Symbol(Symbol.GOAL),
            start = new Symbol(Symbol.START);
    
        Room room0 = new Room(0,0, null);
        room0.setItem(start);
        dungeon.add(room0);
        
        Room room1 = new Room(0,-1, null);
        dungeon.add(room1);
        dungeon.link(room0, room1);
        
        Room room2 = new Room(-1,-1, feather);
        dungeon.add(room2);
        dungeon.link(room1, room2, new Condition(key));
        
        room2 = new Room(1,-1, null);
        dungeon.add(room2);
        dungeon.link(room1,room2);
        
        room1 = new Room(2,-1, key);
        dungeon.add(room1);
        dungeon.link(room2,room1);
        
        room1 = new Room(1,-2, boss);
        dungeon.add(room1);
        dungeon.link(room2,room1, new Condition(feather));
        
        room2 = new Room(0,-2, goal);
        dungeon.add(room2);
        dungeon.link(room1, room2, new Condition(boss));
        
        return dungeon;
    }

    public long getSeed() {
        return seed;
    }
}
