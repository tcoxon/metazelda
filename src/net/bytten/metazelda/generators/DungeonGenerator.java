package net.bytten.metazelda.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.bytten.metazelda.Condition;
import net.bytten.metazelda.Coords;
import net.bytten.metazelda.Direction;
import net.bytten.metazelda.Dungeon;
import net.bytten.metazelda.IDungeon;
import net.bytten.metazelda.IDungeonConstraints;
import net.bytten.metazelda.IDungeonGenerator;
import net.bytten.metazelda.Room;
import net.bytten.metazelda.Symbol;

public class DungeonGenerator implements IDungeonGenerator {

    protected long seed;
    protected Random random;
    protected Dungeon dungeon;
    protected IDungeonConstraints constraints;
    
    public DungeonGenerator(long seed, IDungeonConstraints constraints) {
        this.seed = seed;
        this.random = new Random(seed);
        assert constraints != null;
        this.constraints = constraints;
    }
    
    protected Room chooseRoomWithFreeEdge(Collection<Room> roomCollection) {
        List<Room> rooms = new ArrayList<Room>(roomCollection);
        Collections.shuffle(rooms, random);
        for (int i = 0; i < rooms.size(); ++i) {
            Room room = rooms.get(i);
            for (Direction d: Direction.values()) {
                Coords coords = room.coords.nextInDirection(d);
                if (dungeon.get(coords) == null &&
                        constraints.validRoomCoords(coords)) {
                    return room;
                }
            }
        }
        return null;
    }
    
    protected Direction chooseFreeEdge(Room room) {
        int d0 = random.nextInt(4);
        for (int i = 0; i < 4; ++i) {
            Direction d = Direction.fromCode((d0 + i) % Direction.NUM_DIRS);
            Coords coords = room.coords.nextInDirection(d);
            if (dungeon.get(coords) == null &&
                    constraints.validRoomCoords(coords)) {
                return d;
            }
        }
        assert false : "Room does not have a free edge";
        return null;
    }
    
    protected class KeyLevelRoomMapping {
        protected List<List<Room>> map = new ArrayList<List<Room>>(
                constraints.numberKeys());
        
        List<Room> getRooms(int keyLevel) {
            while (keyLevel >= map.size()) map.add(null);
            if (map.get(keyLevel) == null)
                map.set(keyLevel, new ArrayList<Room>());
            return map.get(keyLevel);
        }
        
        void addRoom(int keyLevel, Room room) {
            getRooms(keyLevel).add(room);
        }
    }
    
    protected static class RoomListEdgeComparator implements Comparator<Room> {
        @Override
        public int compare(Room arg0, Room arg1) {
            return arg0.linkCount() - arg1.linkCount();
        }
    }
    
    protected static final RoomListEdgeComparator EDGE_COUNT_COMPARATOR
        = new RoomListEdgeComparator();
    
    @Override
    public void generate() {
        dungeon = new Dungeon();
        
        int roomsPerLock = constraints.numberSpaces() /
                (constraints.numberKeys()+1);
        
        // keyLevel: the number of keys required to get to the new room
        int keyLevel = 0;
        Symbol latestKey = null;
        // condition that must hold true for the player to reach the new room
        // (the set of keys they must have).
        Condition cond = new Condition();
        
        // Maps keyLevel -> Rooms that were created when lockCount had that
        // value
        KeyLevelRoomMapping levels = new KeyLevelRoomMapping();
        
        // Set up entrance room:
        assert constraints.validRoomCoords(constraints.initialCoords());
        Room entry = new Room(constraints.initialCoords(), null,
                new Symbol(Symbol.START), cond);
        dungeon.add(entry);
        levels.addRoom(keyLevel, entry);
        
        // Loop to place rooms and link them
        while (dungeon.roomCount() < constraints.numberSpaces()) {
            
            boolean doLock = false;
            
            // Decide whether we need to place a new lock
            if (levels.getRooms(keyLevel).size() >= roomsPerLock &&
                    keyLevel < constraints.numberKeys()) {
                latestKey = new Symbol(keyLevel ++);
                cond = cond.and(latestKey);
                doLock = true;
            }
            
            // Find an existing room with a free edge:
            Room parentRoom = null;
            if (!doLock && random.nextInt(10) > 0)
                parentRoom = chooseRoomWithFreeEdge(levels.getRooms(keyLevel));
            if (parentRoom == null) {
                parentRoom = chooseRoomWithFreeEdge(dungeon.getRooms());
                doLock = true;
            }
            
            // Decide which direction to put the new room in relative to the
            // parent
            Direction d = chooseFreeEdge(parentRoom);
            Coords coords = parentRoom.coords.nextInDirection(d);
            Room room = new Room(coords, parentRoom, null, cond);
            
            // Add the room to the dungeon
            assert dungeon.get(room.coords) == null;
            synchronized (dungeon) {
                dungeon.add(room);
                dungeon.link(parentRoom, room, doLock ? latestKey : null);
            }
            levels.addRoom(keyLevel, room);
        }
        
        // TODO link adjacent rooms
        
        // Now place the keys. For every key-level but the last one, place a
        // key for the next level in it, preferring rooms with fewest links
        // (dead end rooms).
        for (int key = 0; key < keyLevel; ++key) {
            List<Room> rooms = levels.getRooms(key);
            
            Collections.shuffle(rooms, random);
            // Collections.sort is stable: it doesn't reorder "equal" elements,
            // which means the shuffling we just did is still useful.
            Collections.sort(rooms, EDGE_COUNT_COMPARATOR);
            
            boolean placedKey = false;
            for (Room room: rooms) {
                if (room.getItem() == null) {
                    room.setItem(new Symbol(key));
                    placedKey = true;
                    break;
                }
            }
            assert placedKey;
        }
        
    }

    @Override
    public IDungeon getDungeon() {
        return dungeon;
    }

}
