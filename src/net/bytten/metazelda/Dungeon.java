package net.bytten.metazelda;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class Dungeon implements IDungeon {

    protected int itemCount;
    protected Map<Coords, Room> rooms;
    protected Bounds bounds;
    
    public Dungeon() {
        rooms = new TreeMap<Coords, Room>();
        bounds = new Bounds(0,0,0,0);
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
    
    @Override
    public void add(Room room) {
        rooms.put(room.coords, room);
        
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
    
        Room room0 = new Room(0,0, null, null, new Condition());
        room0.setItem(start);
        dungeon.add(room0);
        
        Room room1 = new Room(0,-1, room0, null, new Condition());
        dungeon.add(room1);
        dungeon.link(room0, room1);
        
        Room room2 = new Room(-1,-1, room1, feather, new Condition(key));
        dungeon.add(room2);
        dungeon.link(room1, room2, key);
        
        room2 = new Room(1,-1, room1, null, new Condition());
        dungeon.add(room2);
        dungeon.link(room1,room2);
        
        room1 = new Room(2,-1, room2, key, new Condition());
        dungeon.add(room1);
        dungeon.link(room2,room1);
        
        room1 = new Room(1,-2, room2, boss, new Condition(key).and(feather));
        dungeon.add(room1);
        dungeon.link(room2,room1, feather);
        
        room2 = new Room(0,-2, room1, goal,
                new Condition(key).and(feather).and(boss));
        dungeon.add(room2);
        dungeon.link(room1, room2, boss);
        
        return dungeon;
    }

    @Override
    public Room findStart() {
        for (Room room: getRooms()) {
            if (room.isStart()) return room;
        }
        return null;
    }

    @Override
    public Room findBoss() {
        for (Room room: getRooms()) {
            if (room.isBoss()) return room;
        }
        return null;
    }

    @Override
    public Room findGoal() {
        for (Room room: getRooms()) {
            if (room.isGoal()) return room;
        }
        return null;
    }

    @Override
    public Room findSwitch() {
        for (Room room: getRooms()) {
            if (room.isSwitch()) return room;
        }
        return null;
    }

}
