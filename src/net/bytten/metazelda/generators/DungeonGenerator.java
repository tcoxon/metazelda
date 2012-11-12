package net.bytten.metazelda.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    
    @Override
    public void generate() {
        dungeon = new Dungeon();
        List<Room> currentRooms = new ArrayList<Room>(
                constraints.numberSpaces());
        
        int roomsPerLock = constraints.numberSpaces() /
                constraints.numberKeys();
        
        int lockCount = 0;
        Symbol latestLock = null;
        Condition cond = new Condition();
        
        // Set up entrance room:
        assert constraints.validRoomCoords(constraints.initialCoords());
        Room entry = new Room(constraints.initialCoords(),
                new Symbol(Symbol.START), cond);
        dungeon.add(entry);
        currentRooms.add(entry);
        
        // Loop to place rooms and link them
        while (dungeon.roomCount() < constraints.numberSpaces()) {
            
            boolean doLock = false;
            
            // Find an existing room with a free edge:
            Room parentRoom = chooseRoomWithFreeEdge(currentRooms);
            if (parentRoom == null) {
                parentRoom = chooseRoomWithFreeEdge(dungeon.getRooms());
                doLock = true;
            }
            
            if (currentRooms.size() >= roomsPerLock &&
                    lockCount < constraints.numberKeys()) {
                currentRooms.clear();
                latestLock = new Symbol(lockCount ++);
                cond = cond.and(latestLock);
                doLock = true;
            }
            
            Direction d = chooseFreeEdge(parentRoom);
            Coords coords = parentRoom.coords.nextInDirection(d);
            Room room = new Room(coords, null, cond);
            
            assert dungeon.get(room.coords) == null;
            synchronized (dungeon) {
                dungeon.add(room);
                dungeon.link(parentRoom, room, doLock ? latestLock : null);
            }
            currentRooms.add(room);
        }
        
    }

    @Override
    public IDungeon getDungeon() {
        return dungeon;
    }

}
