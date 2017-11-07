package net.bytten.metazelda.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.bytten.gameutil.Vec2I;
import net.bytten.gameutil.Pair;
import net.bytten.gameutil.RandUtil;
import net.bytten.metazelda.Condition;
import net.bytten.metazelda.Dungeon;
import net.bytten.metazelda.Edge;
import net.bytten.metazelda.IDungeon;
import net.bytten.metazelda.Room;
import net.bytten.metazelda.Symbol;
import net.bytten.metazelda.constraints.IDungeonConstraints;
import net.bytten.metazelda.util.GenerationFailureException;
import net.bytten.gameutil.logging.ILogger;

/**
 * The default and reference implementation of an {@link IDungeonGenerator}.
 */
public class DungeonGenerator implements IDungeonGenerator, ILogger {

    protected ILogger logger;
    protected long seed;
    protected Random random;
    protected Dungeon dungeon;
    protected IDungeonConstraints constraints;
    protected int maxRetries = 20;

    protected boolean bossRoomLocked, generateGoal;

    /**
     * Creates a DungeonGenerator with a given random seed and places
     * specific constraints on {@link IDungeon}s it generates.
     *
     * @param seed          the random seed to use
     * @param constraints   the constraints to place on generation
     * @see net.bytten.metazelda.constraints.IDungeonConstraints
     */
    public DungeonGenerator(ILogger logger, long seed,
            IDungeonConstraints constraints) {
        this.logger = logger;
        log("Dungeon seed: "+seed);
        this.seed = seed;
        this.random = new Random(seed);
        assert constraints != null;
        this.constraints = constraints;

        bossRoomLocked = generateGoal = true;
    }

    public DungeonGenerator(long seed, IDungeonConstraints constraints) {
        this(null, seed, constraints);
    }

    @Override
    public void log(String msg) {
        if (logger != null) logger.log(msg);
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * Randomly chooses a {@link Room} within the given collection that has at
     * least one adjacent empty space.
     *
     * @param roomCollection    the collection of rooms to choose from
     * @return  the room that was chosen, or null if there are no rooms with
     *          adjacent empty spaces
     */
    protected Room chooseRoomWithFreeEdge(Collection<Room> roomCollection,
            int keyLevel) {
        List<Room> rooms = new ArrayList<Room>(roomCollection);
        Collections.shuffle(rooms, random);
        for (int i = 0; i < rooms.size(); ++i) {
            Room room = rooms.get(i);
            for (Pair<Double,Integer> next:
                    constraints.getAdjacentRooms(room.id, keyLevel)) {
                if (dungeon.get(next.second) == null) {
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
    protected int chooseFreeEdge(Room room, int keyLevel) {
        List<Pair<Double,Integer>> neighbors = new ArrayList<Pair<Double,Integer>>(
                constraints.getAdjacentRooms(room.id, keyLevel));
        Collections.shuffle(neighbors, random);
        while (!neighbors.isEmpty()) {
            Integer choice = RandUtil.choice(random, neighbors);
            if (dungeon.get(choice) == null)
                return choice;
            neighbors.remove(choice);
        }
        assert false;
        throw new GenerationFailureException("Internal error: Room doesn't have a free edge");
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

    protected static class OutOfRoomsException extends Exception {
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
        int id;
        List<Integer> possibleEntries = new ArrayList<Integer>(
                constraints.initialRooms());
        assert possibleEntries.size() > 0;
        id = possibleEntries.get(random.nextInt(possibleEntries.size()));

        Room entry = new Room(id, constraints.getCoords(id), null,
                new Symbol(Symbol.START), new Condition());
        dungeon.add(entry);

        levels.addRoom(0, entry);
    }

    /**
     * Decides whether to add a new lock (and keyLevel) at this point.
     *
     * @param keyLevel the number of distinct locks that have been placed into
     *      the map so far
     * @param numRooms the number of rooms at the current keyLevel
     * @param targetRoomsPerLock the number of rooms the generator has chosen
     *      as the target number of rooms to place at each keyLevel (which
     *      subclasses can ignore, if desired).
     */
    protected boolean shouldAddNewLock(int keyLevel, int numRooms, int targetRoomsPerLock) {
        int usableKeys = constraints.getMaxKeys();
        if (isBossRoomLocked())
            usableKeys -= 1;
        return numRooms >= targetRoomsPerLock && keyLevel < usableKeys;
    }

    /**
     * Fill the dungeon's space with rooms and doors (some locked).
     * Keys are not inserted at this point.
     *
     * @param levels    the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     */
    protected void placeRooms(KeyLevelRoomMapping levels, int roomsPerLock)
            throws RetryException, OutOfRoomsException {

        // keyLevel: the number of keys required to get to the new room
        int keyLevel = 0;
        Symbol latestKey = null;
        // condition that must hold true for the player to reach the new room
        // (the set of keys they must have).
        Condition cond = new Condition();

        // Loop to place rooms and link them
        while (dungeon.roomCount() < constraints.getMaxRooms()) {

            boolean doLock = false;

            // Decide whether we need to place a new lock
            // (Don't place the last lock, since that's reserved for the boss)
            if (shouldAddNewLock(keyLevel, levels.getRooms(keyLevel).size(), roomsPerLock)) {
                latestKey = new Symbol(keyLevel++);
                cond = cond.and(latestKey);
                doLock = true;
            }

            // Find an existing room with a free edge:
            Room parentRoom = null;
            if (!doLock && random.nextInt(10) > 0)
                parentRoom = chooseRoomWithFreeEdge(levels.getRooms(keyLevel),
                        keyLevel);
            if (parentRoom == null) {
                parentRoom = chooseRoomWithFreeEdge(dungeon.getRooms(),
                        keyLevel);
                doLock = true;
            }

            if (parentRoom == null)
                throw new OutOfRoomsException();

            // Decide which direction to put the new room in relative to the
            // parent
            int nextId = chooseFreeEdge(parentRoom, keyLevel);
            Set<Vec2I> coords = constraints.getCoords(nextId);
            Room room = new Room(nextId, coords, parentRoom, null, cond);

            // Add the room to the dungeon
            assert dungeon.get(room.id) == null;
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

        Symbol goalSym = new Symbol(Symbol.GOAL),
               bossSym = new Symbol(Symbol.BOSS);

        for (Room room: dungeon.getRooms()) {
            if (room.getChildren().size() > 0 || room.getItem() != null)
                continue;
            Room parent = room.getParent();
            if (parent == null)
                continue;
            if (isGenerateGoal() && (parent.getChildren().size() != 1 ||
                    !parent.getPrecond().implies(room.getPrecond())))
                continue;
            if (isGenerateGoal()) {
                if (!constraints.roomCanFitItem(room.id, goalSym) ||
                        !constraints.roomCanFitItem(parent.id, bossSym))
                    continue;
            } else {
                if (!constraints.roomCanFitItem(room.id, bossSym))
                    continue;
            }
            possibleGoalRooms.add(room);
        }

        if (possibleGoalRooms.size() == 0) throw new RetryException();

        Room goalRoom = possibleGoalRooms.get(random.nextInt(
                possibleGoalRooms.size())),
             bossRoom = goalRoom.getParent();

        if (!isGenerateGoal()) {
            bossRoom = goalRoom;
            goalRoom = null;
        }

        if (goalRoom != null) goalRoom.setItem(goalSym);
        bossRoom.setItem(bossSym);

        int oldKeyLevel = bossRoom.getPrecond().getKeyLevel(),
            newKeyLevel = Math.min(levels.keyCount(), constraints.getMaxKeys());

        if (oldKeyLevel != newKeyLevel) {
            List<Room> oklRooms = levels.getRooms(oldKeyLevel);
            if (goalRoom != null) oklRooms.remove(goalRoom);
            oklRooms.remove(bossRoom);

            if (goalRoom != null) levels.addRoom(newKeyLevel, goalRoom);
            levels.addRoom(newKeyLevel, bossRoom);

            Symbol bossKey = new Symbol(newKeyLevel-1);
            Condition precond = bossRoom.getPrecond().and(bossKey);
            bossRoom.setPrecond(precond);
            if (goalRoom != null) goalRoom.setPrecond(precond);

            if (newKeyLevel == 0) {
                dungeon.link(bossRoom.getParent(), bossRoom);
            } else {
                dungeon.link(bossRoom.getParent(), bossRoom, bossKey);
            }
            if (goalRoom != null) dungeon.link(bossRoom, goalRoom);
        }
    }

    /**
     * Removes the given {@link Room} and all its descendants from the given
     * list.
     *
     * @param rooms the list of Rooms to remove nodes from
     * @param room  the Room whose descendants to remove from the list
     */
    protected void removeDescendantsFromList(List<Room> rooms, Room room) {
        rooms.remove(room);
        for (Room child: room.getChildren()) {
            removeDescendantsFromList(rooms, child);
        }
    }

    /**
     * Adds extra conditions to the given {@link Room}'s preconditions and all
     * of its descendants.
     *
     * @param room  the Room to add extra preconditions to
     * @param cond  the extra preconditions to add
     */
    protected void addPrecond(Room room, Condition cond) {
        room.setPrecond(room.getPrecond().and(cond));
        for (Room child: room.getChildren()) {
            addPrecond(child, cond);
        }
    }

    /**
     * Randomly locks descendant rooms of the given {@link Room} with
     * {@link Edge}s that require the switch to be in the given state.
     * <p>
     * If the given state is EITHER, the required states will be random.
     *
     * @param room          the room whose child to lock
     * @param givenState    the state to require the switch to be in for the
     *                      child rooms to be accessible
     * @return              true if any locks were added, false if none were
     *                      added (which can happen due to the way the random
     *                      decisions are made)
     * @see Condition.SwitchState
     */
    protected boolean switchLockChildRooms(Room room,
            Condition.SwitchState givenState) {
        boolean anyLocks = false;
        Condition.SwitchState state = givenState != Condition.SwitchState.EITHER
                ? givenState
                : (random.nextInt(2) == 0
                    ? Condition.SwitchState.ON
                    : Condition.SwitchState.OFF);

        for (Edge edge: room.getEdges()) {
            int neighborId = edge.getTargetRoomId();
            Room nextRoom = dungeon.get(neighborId);
            if (room.getChildren().contains(nextRoom)) {
                if (room.getEdge(neighborId).getSymbol() == null &&
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
        return anyLocks;
    }

    /**
     * Returns a path from the goal to the dungeon entrance, along the 'parent'
     * relations.
     *
     * @return  a list of linked {@link Room}s starting with the goal room and
     *          ending with the start room.
     */
    protected List<Room> getSolutionPath() {
        List<Room> solution = new ArrayList<Room>();
        Room room;
        if (isGenerateGoal()) {
            room = dungeon.findGoal();
        } else {
            room = dungeon.findBoss();
        }
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

            Symbol switchSym = new Symbol(Symbol.SWITCH);

            Room switchRoom = null;
            for (Room room: rooms) {
                if (room.getItem() == null &&
                        baseRoomCond.implies(room.getPrecond()) &&
                        constraints.roomCanFitItem(room.id, switchSym)) {
                    switchRoom = room;
                    break;
                }
            }
            if (switchRoom == null) continue;

            if (switchLockChildRooms(baseRoom, Condition.SwitchState.EITHER)) {
                switchRoom.setItem(switchSym);
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

            for (Pair<Double,Integer> next:
                    // Doesn't matter what the keyLevel is; later checks about
                    // preconds ensure linkage doesn't trivialize the puzzle.
                    constraints.getAdjacentRooms(room.id, Integer.MAX_VALUE)) {
                int nextId = next.second;
                if (room.getEdge(nextId) != null) continue;

                Room nextRoom = dungeon.get(nextId);
                if (nextRoom == null || nextRoom.isGoal() || nextRoom.isBoss())
                    continue;

                boolean forwardImplies = room.getPrecond().implies(nextRoom.getPrecond()),
                        backwardImplies = nextRoom.getPrecond().implies(room.getPrecond());
                if (forwardImplies && backwardImplies) {
                    // both rooms are at the same keyLevel.
                    if (random.nextDouble() >=
                            constraints.edgeGraphifyProbability(room.id, nextRoom.id))
                        continue;

                    dungeon.link(room, nextRoom);
                } else {
                    Symbol difference = room.getPrecond().singleSymbolDifference(
                            nextRoom.getPrecond());
                    if (difference == null || (!difference.isSwitchState() &&
                            random.nextDouble() >=
                                constraints.edgeGraphifyProbability(room.id, nextRoom.id)))
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

            Symbol keySym = new Symbol(key);

            boolean placedKey = false;
            for (Room room: rooms) {
                if (room.getItem() == null && constraints.roomCanFitItem(room.id, keySym)) {
                    room.setItem(keySym);
                    placedKey = true;
                    break;
                }
            }
            if (!placedKey)
                // there were no rooms into which the key would fit
                throw new RetryException();
        }
    }

    protected static final double
            INTENSITY_GROWTH_JITTER = 0.1,
            INTENSITY_EASE_OFF = 0.2;

    /**
     * Recursively applies the given intensity to the given {@link Room}, and
     * higher intensities to each of its descendants that are within the same
     * keyLevel.
     * <p>
     * Intensities set by this method may (will) be outside of the normal range
     * from 0.0 to 1.0. See {@link #normalizeIntensity} to correct this.
     *
     * @param room      the room to set the intensity of
     * @param intensity the value to set intensity to (some randomn variance is
     *                  added)
     * @see Room
     */
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

    /**
     * Scales intensities within the dungeon down so that they all fit within
     * the range 0 <= intensity < 1.0.
     *
     * @see Room
     */
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
        Room goalRoom = dungeon.findGoal();
        if (goalRoom != null)
            goalRoom.setIntensity(0.0);
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
                KeyLevelRoomMapping levels;
                int roomsPerLock;
                if (constraints.getMaxKeys() > 0) {
                    roomsPerLock = constraints.getMaxRooms() /
                        constraints.getMaxKeys();
                } else {
                    roomsPerLock = constraints.getMaxRooms();
                }
                while (true) {
                    dungeon = new Dungeon();

                    // Maps keyLevel -> Rooms that were created when lockCount had that
                    // value
                    levels = new KeyLevelRoomMapping();

                    // Create the entrance to the dungeon:
                    initEntranceRoom(levels);

                    try {
                        // Fill the dungeon with rooms:
                        placeRooms(levels, roomsPerLock);
                        break;
                    } catch (OutOfRoomsException e) {
                        // We can run out of rooms where certain links have
                        // predetermined locks. Example: if a river bisects the
                        // map, the keyLevel for rooms in the river > 0 because
                        // crossing water requires a key. If there are not
                        // enough rooms before the river to build up to the
                        // key for the river, we've run out of rooms.
                        log("Ran out of rooms. roomsPerLock was "+roomsPerLock);
                        roomsPerLock = roomsPerLock * constraints.getMaxKeys() /
                                (constraints.getMaxKeys() + 1);
                        log("roomsPerLock is now "+roomsPerLock);

                        if (roomsPerLock == 0) {
                            throw new GenerationFailureException(
                                    "Failed to place rooms. Have you forgotten to disable boss-locking?");
                            // If the boss room is locked, the final key is used
                            // only for the boss room. So if the final key is
                            // also used to cross the river, rooms cannot be
                            // placed.
                        }
                    }
                }

                // Place the boss and goal rooms:
                placeBossGoalRooms(levels);

                // Place switches and the locks that require it:
                placeSwitches();

                computeIntensity(levels);

                // Place the keys within the dungeon:
                placeKeys(levels);

                if (levels.keyCount()-1 != constraints.getMaxKeys())
                    throw new RetryException();

                // Make the dungeon less tree-like:
                graphify();

                checkAcceptable();

                return;

            } catch (RetryException e) {
                if (++ attempt > maxRetries) {
                    throw new GenerationFailureException("Dungeon generator failed", e);
                }
                log("Retrying dungeon generation...");
            }
        }

    }

    @Override
    public IDungeon getDungeon() {
        return dungeon;
    }

    public boolean isBossRoomLocked() {
        return bossRoomLocked;
    }

    public void setBossRoomLocked(boolean bossRoomLocked) {
        this.bossRoomLocked = bossRoomLocked;
    }

    public boolean isGenerateGoal() {
        return generateGoal;
    }

    public void setGenerateGoal(boolean generateGoal) {
        this.generateGoal = generateGoal;
    }

}
