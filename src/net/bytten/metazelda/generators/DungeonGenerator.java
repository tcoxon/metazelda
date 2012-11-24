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
import net.bytten.metazelda.Room;
import net.bytten.metazelda.Symbol;
import net.bytten.metazelda.constraints.IDungeonConstraints;

public class DungeonGenerator implements IDungeonGenerator {
    
    public static final int MAX_RETRIES = 20;

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
        
        int keyCount() {
            return map.size();
        }
    }
    
    protected static class RetryException extends Exception {
        private static final long serialVersionUID = 1L;
    }
    
    protected static final Comparator<Room>
    EDGE_COUNT_COMPARATOR = new Comparator<Room>() {
        @Override
        public int compare(Room arg0, Room arg1) {
            return arg0.linkCount() - arg1.linkCount();
        }
    },
    INTENSITY_COMPARATOR = new Comparator<Room>() {
        @Override
        public int compare(Room arg0, Room arg1) {
            return arg0.getIntensity() > arg1.getIntensity() ? -1
                    : arg0.getIntensity() < arg1.getIntensity() ? 1
                            : 0;
        }
    };
    
    // Sets up the dungeon's entrance room
    protected void initEntranceRoom(KeyLevelRoomMapping levels)
            throws RetryException {
        Coords coords = null;
        List<Coords> possibleEntries = new ArrayList<Coords>(
                constraints.initialCoords());
        assert possibleEntries.size() > 0;
        coords = possibleEntries.get(random.nextInt(possibleEntries.size()));
        assert constraints.validRoomCoords(coords);
        
        Room entry = new Room(coords, null, new Symbol(Symbol.START),
                new Condition());
        dungeon.add(entry);
        
        levels.addRoom(0, entry);
    }
    
    // Fill the dungeon's space with rooms and doors (some locked)
    protected void placeRooms(KeyLevelRoomMapping levels) throws RetryException {
        
        final int roomsPerLock = constraints.numberSpaces() /
                constraints.numberKeys();
        
        // keyLevel: the number of keys required to get to the new room
        int keyLevel = 0;
        Symbol latestKey = null;
        // condition that must hold true for the player to reach the new room
        // (the set of keys they must have).
        Condition cond = new Condition();
        
        // Loop to place rooms and link them
        while (dungeon.roomCount() < constraints.numberSpaces()) {
            
            boolean doLock = false;
            
            // Decide whether we need to place a new lock
            // (Don't place the last lock, since that's reserved for the boss)
            if (levels.getRooms(keyLevel).size() >= roomsPerLock &&
                    keyLevel < constraints.numberKeys()-1) {
                latestKey = new Symbol(keyLevel++);
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
                parentRoom.addChild(room);
                dungeon.link(parentRoom, room, doLock ? latestKey : null);
            }
            levels.addRoom(keyLevel, room);
        }
    }
    
    protected void placeBossGoalRooms(KeyLevelRoomMapping levels)
            throws RetryException {
        List<Room> possibleGoalRooms = new ArrayList<Room>(dungeon.roomCount());
        
        for (Room room: dungeon.getRooms()) {
            if (room.getChildren().size() > 0 || room.getItem() != null)
                continue;
            Room parent = room.getParent();
            if (parent == null || parent.getChildren().size() != 1 ||
                    room.getItem() != null ||
                    !parent.getPrecond().implies(room.getPrecond()))
                continue;
            possibleGoalRooms.add(room);
        }
        
        if (possibleGoalRooms.size() == 0) throw new RetryException();
        
        Room goalRoom = possibleGoalRooms.get(random.nextInt(
                possibleGoalRooms.size())),
             bossRoom = goalRoom.getParent();
        
        goalRoom.setItem(new Symbol(Symbol.GOAL));
        bossRoom.setItem(new Symbol(Symbol.BOSS));
        
        int oldKeyLevel = bossRoom.getPrecond().getKeyLevel(),
            newKeyLevel = levels.keyCount();
        List<Room> oklRooms = levels.getRooms(oldKeyLevel);
        oklRooms.remove(goalRoom);
        oklRooms.remove(bossRoom);
        
        levels.addRoom(newKeyLevel, goalRoom);
        levels.addRoom(newKeyLevel, bossRoom);
        
        Symbol bossKey = new Symbol(newKeyLevel-1);
        Condition precond = bossRoom.getPrecond().and(bossKey);
        bossRoom.setPrecond(precond);
        goalRoom.setPrecond(precond);
        
        dungeon.link(bossRoom.getParent(), bossRoom, bossKey);
        dungeon.link(bossRoom, goalRoom);
    }
    
    // Link up adjacent rooms to make the graph less of a tree:
    protected void graphify() throws RetryException {
        for (Room room: dungeon.getRooms()) {
            
            if (room.isGoal() || room.isBoss()) continue;
            
            for (Direction d: Direction.values()) {
                if (room.getEdge(d) != null) continue;
                if (random.nextInt(6) != 0) continue;
                
                Room nextRoom = dungeon.get(room.coords.nextInDirection(d));
                if (nextRoom == null || nextRoom.isGoal() || nextRoom.isBoss())
                    continue;
                
                boolean forwardImplies = room.precond.implies(nextRoom.precond),
                        backwardImplies = nextRoom.precond.implies(room.precond);
                if (forwardImplies && backwardImplies) {
                    // both rooms are at the same keyLevel.
                    dungeon.link(room, nextRoom);
                } else {
                    Symbol difference = room.precond.singleSymbolDifference(
                            nextRoom.precond);
                    dungeon.link(room, nextRoom, difference);
                }
            }
        }
    }
    
    protected void placeKeys(KeyLevelRoomMapping levels) throws RetryException {
        // Now place the keys. For every key-level but the last one, place a
        // key for the next level in it, preferring rooms with fewest links
        // (dead end rooms).
        for (int key = 0; key < levels.keyCount()-1; ++key) {
            List<Room> rooms = levels.getRooms(key);
            
            Collections.shuffle(rooms, random);
            // Collections.sort is stable: it doesn't reorder "equal" elements,
            // which means the shuffling we just did is still useful.
            Collections.sort(rooms, INTENSITY_COMPARATOR);
            // Alternatively, use the EDGE_COUNT_COMPARATOR to put keys at
            // 'dead end' rooms.
            
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
    
    protected static final double
            INTENSITY_GROWTH_JITTER = 0.1,
            INTENSITY_EASE_OFF = 0.2;
    
    protected double applyIntensity(Room room, double intensity) {
        intensity *= 1.0 - INTENSITY_GROWTH_JITTER/2.0 +
                INTENSITY_GROWTH_JITTER * random.nextDouble();
        
        room.setIntensity(intensity);
        
        double maxIntensity = intensity;
        for (Room child: room.getChildren()) {
            if (room.getPrecond().implies(child.getPrecond())) {
                maxIntensity = Math.max(maxIntensity, applyIntensity(child,
                        intensity + 1.0));
            }
        }
        
        return maxIntensity;
    }

    protected void normalizeIntensity() {
        double maxIntensity = 0.0;
        for (Room room: dungeon.getRooms()) {
            maxIntensity = Math.max(maxIntensity, room.getIntensity());
        }
        for (Room room: dungeon.getRooms()) {
            room.setIntensity(room.getIntensity() / maxIntensity);
        }
    }
    
    // Compute the 'intensity' of each room, a number from 0.0 to 1.0 that
    // represents the relative difficulty of that room. Rooms generally get
    // more intense the deeper into the dungeon they are.
    protected void computeIntensity(KeyLevelRoomMapping levels)
            throws RetryException {
        
        double nextLevelBaseIntensity = 0.0;
        for (int level = 0; level < levels.keyCount(); ++level) {
            
            double intensity = nextLevelBaseIntensity *
                    (1.0 - INTENSITY_EASE_OFF);
            
            for (Room room: levels.getRooms(level)) {
                if (room.getParent() == null ||
                        !room.getParent().getPrecond().
                            implies(room.getPrecond())) {
                    nextLevelBaseIntensity = Math.max(
                            nextLevelBaseIntensity,
                            applyIntensity(room, intensity));
                }
            }
        }
        
        normalizeIntensity();
        
        dungeon.findBoss().setIntensity(1.0);
        dungeon.findGoal().setIntensity(0.0);
    }
    
    protected void checkAcceptable() throws RetryException {
        if (!constraints.isAcceptable(dungeon))
            throw new RetryException();
    }
    
    @Override
    public void generate() {
        int attempt = 0;
        while (true) {
            try {
                dungeon = new Dungeon();
                
                // Maps keyLevel -> Rooms that were created when lockCount had that
                // value
                KeyLevelRoomMapping levels = new KeyLevelRoomMapping();
                
                // Create the entrance to the dungeon:
                initEntranceRoom(levels);
                
                // Fill the dungeon with rooms:
                placeRooms(levels);
                
                // Place the boss and goal rooms:
                placeBossGoalRooms(levels);
        
                // Make the dungeon less tree-like:
                graphify();
                
                computeIntensity(levels);
                
                // Place the keys within the dungeon:
                placeKeys(levels);
                
                checkAcceptable();
                
                return;
            
            } catch (RetryException e) {
                if (++ attempt > MAX_RETRIES) {
                    throw new RuntimeException("Dungeon generator failed", e);
                }
                System.out.println("Retrying dungeon generation...");
            }
        }
        
    }

    @Override
    public IDungeon getDungeon() {
        return dungeon;
    }

}
