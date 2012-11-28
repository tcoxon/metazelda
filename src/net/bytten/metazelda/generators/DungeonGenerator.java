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

/**
 * The default and reference implementation of an {@link IDungeonGenerator}.
 */
public class DungeonGenerator implements IDungeonGenerator {
    
    public static final int MAX_RETRIES = 20;

    protected long seed;
    protected Random random;
    protected Dungeon dungeon;
    protected IDungeonConstraints constraints;
    
    /**
     * Creates a DungeonGenerator with a given random seed and places
     * specific constraints on {@link IDungeon}s it generates.
     * 
     * @param seed          the random seed to use
     * @param constraints   the constraints to place on generation
     * @see net.bytten.metazelda.constraints.IDungeonConstraints
     */
    public DungeonGenerator(long seed, IDungeonConstraints constraints) {
        System.out.println("Dungeon seed: "+seed);
        this.seed = seed;
        this.random = new Random(seed);
        assert constraints != null;
        this.constraints = constraints;
    }
    
    /**
     * Randomly chooses a {@link Room} within the given collection that has at
     * least one adjacent empty space.
     * 
     * @param roomCollection    the collection of rooms to choose from
     * @return  the room that was chosen, or null if there are no rooms with
     *          adjacent empty spaces
     */
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
    
    /**
     * Randomly chooses a {@link Direction} in which the given {@link Room} has
     * an adjacent empty space.
     * 
     * @param room  the room
     * @return  the Direction of the empty space chosen adjacent to the Room or
     *          null if there are no adjacent empty spaces
     */
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
    
    /**
     * Maps 'keyLevel' to the set of rooms within that keyLevel.
     * <p>
     * A 'keyLevel' is the count of the number of unique keys are needed for all
     * the locks we've placed. For example, all the rooms in keyLevel 0 are
     * accessible without collecting any keys, while to get to rooms in
     * keyLevel 3, the player must have collected at least 3 keys.
     */
    protected class KeyLevelRoomMapping {
        protected List<List<Room>> map = new ArrayList<List<Room>>(
                constraints.getMaxKeys());
        
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
    
    /**
     * Thrown by several IDungeonGenerator methods that can fail.
     * Should be caught and handled in {@link #generate}.
     */
    protected static class RetryException extends Exception {
        private static final long serialVersionUID = 1L;
    }
    
    /**
     * Comparator objects for sorting {@link Room}s in a couple of different
     * ways. These are used to determine in which rooms of a given keyLevel it
     * is best to place the next key.
     * 
     * @see #placeKeys
     */
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
    
    /**
     * Sets up the dungeon's entrance room.
     * 
     * @param levels    the keyLevel -> room-set mapping to update
     * @see KeyLevelRoomMapping 
     */
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
    
    /**
     * Fill the dungeon's space with rooms and doors (some locked).
     * Keys are not inserted at this point.
     * 
     * @param levels    the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     */
    protected void placeRooms(KeyLevelRoomMapping levels) throws RetryException {
        
        final int roomsPerLock = constraints.getMaxSpaces() /
                constraints.getMaxKeys();
        
        // keyLevel: the number of keys required to get to the new room
        int keyLevel = 0;
        Symbol latestKey = null;
        // condition that must hold true for the player to reach the new room
        // (the set of keys they must have).
        Condition cond = new Condition();
        
        // Loop to place rooms and link them
        while (dungeon.roomCount() < constraints.getMaxSpaces()) {
            
            boolean doLock = false;
            
            // Decide whether we need to place a new lock
            // (Don't place the last lock, since that's reserved for the boss)
            if (levels.getRooms(keyLevel).size() >= roomsPerLock &&
                    keyLevel < constraints.getMaxKeys()-1) {
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
    
    /**
     * Places the BOSS and GOAL rooms within the dungeon, in existing rooms.
     * These rooms are moved into the next keyLevel.
     * 
     * @param levels    the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     */
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
    
    protected void removeDescendantsFromList(List<Room> rooms, Room room) {
        rooms.remove(room);
        for (Room child: room.getChildren()) {
            removeDescendantsFromList(rooms, child);
        }
    }
    
    protected void addPrecond(Room room, Condition cond) {
        room.setPrecond(room.getPrecond().and(cond));
        for (Room child: room.getChildren()) {
            addPrecond(child, cond);
        }
    }
    
    protected boolean switchLockChildRooms(Room room,
            Condition.SwitchState givenState) {
        boolean anyLocks = false;
        Condition.SwitchState state = givenState != Condition.SwitchState.EITHER
                ? givenState
                : (random.nextInt(2) == 0
                    ? Condition.SwitchState.ON
                    : Condition.SwitchState.OFF);
        
        for (Direction d: Direction.values()) {
            if (room.getEdge(d) != null) {
                Room nextRoom = dungeon.get(room.coords.nextInDirection(d));
                if (room.getChildren().contains(nextRoom)) {
                    if (room.getEdge(d).getSymbol() == null &&
                            random.nextInt(4) != 0) {
                        dungeon.link(room, nextRoom, state.toSymbol());
                        addPrecond(nextRoom, new Condition(state.toSymbol()));
                        anyLocks = true;
                    } else {
                        anyLocks |= switchLockChildRooms(nextRoom, state);
                    }
                    
                    if (givenState == Condition.SwitchState.EITHER) {
                        state = state.invert();
                    }
                }
            }
        }
        return anyLocks;
    }
    
    protected List<Room> getSolutionPath() {
        List<Room> solution = new ArrayList<Room>();
        Room room = dungeon.findGoal();
        while (room != null) {
            solution.add(room);
            room = room.getParent();
        }
        return solution;
    }
    
    /**
     * Makes some {@link Edge}s within the dungeon require the dungeon's switch
     * to be in a particular state, and places the switch in a room in the
     * dungeon.
     * 
     * @throws RetryException if it fails
     */
    protected void placeSwitches() throws RetryException {
        // Possible TODO: have multiple switches on separate circuits
        // At the moment, we only have one switch per dungeon.
        if (constraints.getMaxSwitches() <= 0) return;
        
        List<Room> solution = getSolutionPath();
        
        for (int attempt = 0; attempt < 10; ++attempt) {
            
            List<Room> rooms = new ArrayList<Room>(dungeon.getRooms());
            Collections.shuffle(rooms, random);
            Collections.shuffle(solution, random);
            
            // Pick a base room from the solution path so that the player
            // will have to encounter a switch-lock to solve the dungeon.
            Room baseRoom = null;
            for (Room room: solution) {
                if (room.getChildren().size() > 1 && room.getParent() != null) {
                    baseRoom = room;
                    break;
                }
            }
            if (baseRoom == null) throw new RetryException();
            Condition baseRoomCond = baseRoom.getPrecond();
            
            removeDescendantsFromList(rooms, baseRoom);
            
            Room switchRoom = null;
            for (Room room: rooms) {
                if (room.getItem() == null &&
                        baseRoomCond.implies(room.getPrecond())) {
                    switchRoom = room;
                    break;
                }
            }
            if (switchRoom == null) continue;
            
            if (switchLockChildRooms(baseRoom, Condition.SwitchState.EITHER)) {
                switchRoom.setItem(new Symbol(Symbol.SWITCH));
                return;
            }
        }
        throw new RetryException();
    }
    
    /**
     * Randomly links up some adjacent rooms to make the dungeon graph less of
     * a tree.
     * 
     * @throws RetryException if it fails
     */
    protected void graphify() throws RetryException {
        for (Room room: dungeon.getRooms()) {
            
            if (room.isGoal() || room.isBoss()) continue;
            
            for (Direction d: Direction.values()) {
                if (room.getEdge(d) != null) continue;
                
                Room nextRoom = dungeon.get(room.coords.nextInDirection(d));
                if (nextRoom == null || nextRoom.isGoal() || nextRoom.isBoss())
                    continue;
                
                boolean forwardImplies = room.precond.implies(nextRoom.precond),
                        backwardImplies = nextRoom.precond.implies(room.precond);
                if (forwardImplies && backwardImplies) {
                    // both rooms are at the same keyLevel.
                    if (random.nextInt(6) != 0) continue;
                    
                    dungeon.link(room, nextRoom);
                } else {
                    Symbol difference = room.precond.singleSymbolDifference(
                            nextRoom.precond);
                    if (difference == null || (!difference.isSwitchState() &&
                            random.nextInt(6) != 0))
                        continue;
                    dungeon.link(room, nextRoom, difference);
                }
            }
        }
    }
    
    /**
     * Places keys within the dungeon in such a way that the dungeon is
     * guaranteed to be solvable.
     * 
     * @param levels    the keyLevel -> room-set mapping to use
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     */
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
            room.setIntensity(room.getIntensity() * 0.99 / maxIntensity);
        }
    }
    
    /**
     * Computes the 'intensity' of each {@link Room}. Rooms generally get more
     * intense the deeper they are into the dungeon.
     * 
     * @param levels    the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     * @see Room
     */
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
    
    /**
     * Checks with the
     * {@link net.bytten.metazelda.constraints.IDungeonConstraints} that the
     * dungeon is OK to use.
     * 
     * @throws RetryException if the IDungeonConstraints decided generation must
     *                        be re-attempted
     * @see net.bytten.metazelda.constraints.IDungeonConstraints
     */
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
                
                // Place switches and the locks that require it:
                placeSwitches();
        
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
