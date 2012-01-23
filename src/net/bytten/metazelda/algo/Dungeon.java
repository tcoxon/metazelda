package net.bytten.metazelda.algo;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class Dungeon {

    protected int elementCount;
    protected Map<Coords, Room> rooms;
    
    public Dungeon() {
        rooms = new TreeMap<Coords, Room>();
    }
    
    public Element makeNewElement() {
        return new Element(elementCount++);
    }
    
    public Room get(Coords coords) {
        return rooms.get(coords);
    }
    
    public Room get(int x, int y) {
        return get(new Coords(x,y));
    }
    
    public void add(Room room) {
        rooms.put(room.coords, room);
    }
    
    public void linkOneWay(Room room1, Room room2) {
        linkOneWay(room1, room2, null);
    }
    
    public void link(Room room1, Room room2) {
        link(room1, room2, null);
    }
    
    public void linkOneWay(Room room1, Room room2, Condition cond) {
        assert room1.coords.isAdjacent(room2.coords);
        int d = room1.coords.getDirectionTo(room2.coords);
        room1.getEdges()[d] = new Edge(cond);
    }
    
    public void link(Room room1, Room room2, Condition cond) {
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
        Dungeon dungeon = new Dungeon();
        
        Element key = dungeon.makeNewElement(),
            feather = dungeon.makeNewElement(),
            boss = dungeon.makeNewElement(),
            goal = new Element(Element.GOAL),
            start = new Element(Element.START);
    
        Room room0 = new Room(0,0);
        room0.setItem(start);
        dungeon.add(room0);
        
        Room room1 = new Room(0,-1);
        dungeon.add(room1);
        dungeon.link(room0, room1);
        
        Room room2 = new Room(-1,-1);
        room2.setItem(feather);
        dungeon.link(room1, room2, new Condition(key));
        
        room2 = new Room(1,-1);
        dungeon.link(room1,room2);
        
        room1 = new Room(2,-1);
        room1.setItem(key);
        dungeon.link(room2,room1);
        
        room1 = new Room(1,-2);
        room1.setItem(boss);
        dungeon.link(room2,room1, new Condition(feather));
        
        room2 = new Room(0,-2);
        room2.setItem(goal);
        dungeon.link(room1, room2, new Condition(boss));
        
        return dungeon;
    }
}
