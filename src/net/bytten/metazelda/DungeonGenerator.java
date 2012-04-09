package net.bytten.metazelda;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class DungeonGenerator {
    
    private Random random;
    protected Dungeon dungeon;
    
    public DungeonGenerator(long seed) {
        random = new Random(seed);
    }
    
    protected Random getRandom() {
        return random;
    }
    
    public Dungeon getDungeon() {
        return dungeon;
    }

    // The actual core algorithm from
    //     http://bytten.net/devlog/tag/metazelda/
    // The choose* methods below control the decisions the algorithm makes.
    // Try tweaking the probabilities to see if you can produce better dungeons.
    // 'depth' is a count of how many rooms in the current chain have been
    // generated without any keys or locks. Subclasses may use this count to
    // optimize dungeons.
    public Room addItemPath(Symbol item, int depth) {
        // Add a new room to the dungeon containing the given item. Conditions
        // to enter the room are randomly generated, and if requiring a new item
        // in the dungeon, will cause other rooms to be added, too.
        Condition cond = null;
        
        // Choose condition to enter the room
        if (chooseCreateNewItem()) {
            // create a new condition and item for it
            Symbol elem = dungeon.makeNewItem();
            cond = new Condition(elem);
            addItemPath(elem, 0);
        } else if (chooseReuseItem()) {
            // make the condition one which we've used before
            Symbol elem = choosePlacedItem();
            if (elem != null) {
                cond = new Condition(elem);
                depth = 0;
            }
        }
        
        // Choose where to place the new room
        Room locRoom = null;
        Integer locD = null;
        if (chooseCreatePaddingRoom(depth)) {
            // Add padding rooms (and potentially more conditions and branches
            // along the way)
            locRoom = addItemPath(null, depth+1);
            locD = chooseAdjacentSpace(locRoom);
            // addItemPath can create a room with no adjacent spaces, so
            // loc.second (the direction to add the new room in) might still be
            // null.
        }
        
        if (locRoom == null || locD == null) {
            // Choose an existing room with a free edge
            locRoom  = chooseExistingRoom();
            locD = chooseAdjacentSpace(locRoom);
        }
        
        // Compute the new room's preconditions
        // (NB. cond is the condition to enter the room along the ingoing edge,
        // while precond is the set of all symbols that the player must be
        // holding to have reached the room)
        Condition precond = locRoom.getPrecond();
        if (cond != null) {
            precond = precond.and(dungeon.precondClosure(cond));
        }
        
        // Finally create the new room and link it to the parent room
        Room room = new Room(locRoom.coords.nextInDirection(locD),
                item, precond);
        synchronized (dungeon) {
            dungeon.add(room);
            dungeon.link(locRoom, room, cond);
        }
        
        linkNeighbors(room);
        
        return room;
    }
    
    protected void linkNeighbors(Room room) {
        Condition precond = room.getPrecond();
        
        // for each neighboring room:
        for (int d = 0; d < Direction.NUM_DIRS; ++d) {
            Room neighbor = dungeon.get(room.coords.nextInDirection(d));
            if (neighbor == null || dungeon.roomsAreLinked(room, neighbor))
                continue;
            
            if (precond.equals(neighbor.getPrecond())) {
                // these two rooms have equivalent preconditions -- linking both
                // ways will not break any puzzles
                synchronized (dungeon) {
                    dungeon.link(room, neighbor);
                }
            } else if (precond.implies(neighbor.getPrecond())) {
                if (chooseLinkNeighborOneWay(room, neighbor)) {
                    // link from the new room to the neighbor. A link that way
                    // won't break any puzzles, but a link back the other way
                    // would!
                    synchronized (dungeon) {
                        dungeon.linkOneWay(room, neighbor);
                    }
                }
            }
        }
        
    }
    
    private static final int MAX_ELEMS = 26;
    
    protected boolean chooseCreateNewItem() {
        return dungeon.itemCount() < MAX_ELEMS && getRandom().nextFloat() < 0.2;
    }
    
    protected boolean chooseReuseItem() {
        return getRandom().nextFloat() < 0.3;
    }
    
    protected boolean chooseCreatePaddingRoom(int depth) {
        return dungeon.roomCount() < 20 && getRandom().nextFloat() < 0.7;
    }
    
    protected boolean chooseLinkNeighborOneWay(Room room, Room neighbor) {
        return getRandom().nextFloat() < 0.3;
    }
    
    protected Symbol choosePlacedItem() {
        Set<Symbol> placedItems = dungeon.getPlacedItems();
        if (placedItems.size() == 0) return null;
        int i = getRandom().nextInt(placedItems.size());
        return new ArrayList<Symbol>(placedItems).get(i);
    }
    
    protected Room chooseExistingRoom() {
        List<Room> rooms = dungeon.computeBoundaryRooms();
        return rooms.get(getRandom().nextInt(rooms.size()));
    }
    
    protected boolean newRoomAllowedInSpace(Coords xy) {
        return dungeon.get(xy) == null;
    }
    
    protected Integer chooseAdjacentSpace(Room room) {
        // Return a random direction of travel from room to an adjacent empty
        // space, or null if there are no nearby spaces
        int d = getRandom().nextInt(Direction.NUM_DIRS),
            tries = 0;
        Coords xy = room.coords.nextInDirection(d);
        while (!newRoomAllowedInSpace(xy) && tries < Direction.NUM_DIRS) {
            d = (d+1) % Direction.NUM_DIRS;
            ++tries;
            xy = room.coords.nextInDirection(d);
        }
        if (newRoomAllowedInSpace(xy))
            return d;
        return null;
    }
    
    public Dungeon generate() {
        dungeon = new Dungeon();
        Room startRoom = new Room(0,0, null, new Condition());
        startRoom.setItem(new Symbol(Symbol.START));
        dungeon.add(startRoom);
        
        addItemPath(new Symbol(Symbol.GOAL), 0);
        
        return dungeon;
    }
    
}
