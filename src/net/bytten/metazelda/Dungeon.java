package net.bytten.metazelda;

import java.util.Collection;

import net.bytten.metazelda.util.Coords;
import net.bytten.metazelda.util.IntMap;

/**
 * @see IDungeon
 * 
 * Due to the fact it uses IntMap to store the rooms, it makes the assumption
 * that room ids are low in value, tight in range, and all positive.
 */
public class Dungeon implements IDungeon {

    protected int itemCount;
    protected IntMap<Room> rooms;
    protected Bounds bounds;
    
    public Dungeon() {
        rooms = new IntMap<Room>();
        bounds = new Bounds(Integer.MAX_VALUE,Integer.MAX_VALUE,
                Integer.MIN_VALUE,Integer.MIN_VALUE);
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
    public Room get(int id) {
        return rooms.get(id);
    }
    
    @Override
    public void add(Room room) {
        rooms.put(room.id, room);
        
        for (Coords xy: room.getCoords()) {
            if (xy.x < bounds.left) {
                bounds = new Bounds(xy.x, bounds.top,
                        bounds.right, bounds.bottom);
            }
            if (xy.x > bounds.right) {
                bounds = new Bounds(bounds.left, bounds.top,
                        xy.x, bounds.bottom);
            }
            if (xy.y < bounds.top) {
                bounds = new Bounds(bounds.left, xy.y,
                        bounds.right, bounds.bottom);
            }
            if (xy.y > bounds.bottom) {
                bounds = new Bounds(bounds.left, bounds.top,
                        bounds.right, xy.y);
            }
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
        room1.setEdge(room2.id, cond);
    }
    
    @Override
    public void link(Room room1, Room room2, Symbol cond) {
        linkOneWay(room1, room2, cond);
        linkOneWay(room2, room1, cond);
    }
    
    @Override
    public boolean roomsAreLinked(Room room1, Room room2) {
        return room1.getEdge(room2.id) != null ||
            room2.getEdge(room1.id) != null;
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
