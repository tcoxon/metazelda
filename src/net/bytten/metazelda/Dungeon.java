package net.bytten.metazelda;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Dungeon implements IDungeon {

    protected int itemCount;
    protected Map<Coords, Room> rooms;
    protected Bounds bounds;
    protected Map<Symbol, Condition> placedItems;
    
    // Used for getting external rooms:
    protected Map<Integer, Integer> minX, maxX, minY, maxY;
    
    public Dungeon() {
        rooms = new TreeMap<Coords, Room>();
        bounds = new Bounds(0,0,0,0);
        placedItems = new HashMap<Symbol, Condition>();
        
        minX = new HashMap<Integer,Integer>();
        maxX = new HashMap<Integer,Integer>();
        minY = new HashMap<Integer,Integer>();
        maxY = new HashMap<Integer,Integer>();
    }
    
    @Override
    public Bounds getExtentBounds() {
        return bounds;
    }
    
    @Override
    public Collection<Room> getRooms() {
        return rooms.values();
    }
    
    @Override
    public int itemCount() {
        return itemCount;
    }
    
    @Override
    public Set<Symbol> getPlacedItems() {
        return placedItems.keySet();
    }
    
    @Override
    public int roomCount() {
        return rooms.size();
    }
    
    @Override
    public Room get(Coords coords) {
        return rooms.get(coords);
    }
    
    @Override
    public Room get(int x, int y) {
        return get(new Coords(x,y));
    }
    
    private void placeItem(Symbol e, Condition precond) {
        assert precond != null;
        if (e != null && !e.isGoal() && !e.isStart())
            placedItems.put(e, precond);
    }
    
    @Override
    public Condition getItemPrecond(Symbol item) {
        // return the precondition to getting the given item in the dungeon
        return placedItems.get(item);
    }
    
    public Condition precondClosure(Condition cond) {
        // return the entire set of symbols that must be held by the player
        // for cond to be true.
        return cond.closure(placedItems);
    }
    
    @Override
    public void add(Room room) {
        rooms.put(room.coords, room);
        
        placeItem(room.getItem(), room.getPrecond());
        
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
    
    @Override
    public void linkOneWay(Room room1, Room room2) {
        linkOneWay(room1, room2, null);
    }
    
    @Override
    public void link(Room room1, Room room2) {
        link(room1, room2, null);
    }
    
    @Override
    public void linkOneWay(Room room1, Room room2, Symbol cond) {
        assert rooms.values().contains(room1) && rooms.values().contains(room2);
        assert room1.coords.isAdjacent(room2.coords);
        Direction d = room1.coords.getDirectionTo(room2.coords);
        room1.getEdges()[d.code] = new Edge(cond);
    }
    
    @Override
    public void link(Room room1, Room room2, Symbol cond) {
        assert rooms.values().contains(room1) && rooms.values().contains(room2);
        assert room1.coords.isAdjacent(room2.coords);
        Direction d = room1.coords.getDirectionTo(room2.coords);
        room1.getEdges()[d.code] = new Edge(cond);
        room2.getEdges()[Direction.oppositeDirection(d).code] = new Edge(cond);
    }
    
    @Override
    public boolean roomsAreLinked(Room room1, Room room2) {
        Direction d = room1.coords.getDirectionTo(room2.coords);
        return room1.getEdge(d) != null ||
            room2.getEdge(Direction.oppositeDirection(d)) != null;
    }
    
    public static Dungeon makeTestDungeon() {
        Dungeon dungeon = new Dungeon();
        
        Symbol key = new Symbol(0),
            feather = new Symbol(1),
            boss = new Symbol(2),
            goal = new Symbol(Symbol.GOAL),
            start = new Symbol(Symbol.START);
    
        Room room0 = new Room(0,0, null, new Condition());
        room0.setItem(start);
        dungeon.add(room0);
        
        Room room1 = new Room(0,-1, null, new Condition());
        dungeon.add(room1);
        dungeon.link(room0, room1);
        
        Room room2 = new Room(-1,-1, feather, new Condition(key));
        dungeon.add(room2);
        dungeon.link(room1, room2, key);
        
        room2 = new Room(1,-1, null, new Condition());
        dungeon.add(room2);
        dungeon.link(room1,room2);
        
        room1 = new Room(2,-1, key, new Condition());
        dungeon.add(room1);
        dungeon.link(room2,room1);
        
        room1 = new Room(1,-2, boss, new Condition(key).and(feather));
        dungeon.add(room1);
        dungeon.link(room2,room1, feather);
        
        room2 = new Room(0,-2, goal, new Condition(key).and(feather).and(boss));
        dungeon.add(room2);
        dungeon.link(room1, room2, boss);
        
        return dungeon;
    }

}
