package net.bytten.metazelda;

import java.util.Random;

public class DungeonGenerator {
    
    public static final int MAX_ELEMS = 26;
    
    private Random random;
    private Dungeon dungeon;
    
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
    // Lines marked with XXX are where tweakable probabilities are. Play around
    // with them to see if you can generate better dungeons.
    public Room addItemPath(Dungeon dungeon, Symbol item) {
        // Add a new room to the dungeon containing the given item. Conditions
        // to enter the room are randomly generated, and if requiring a new item
        // in the dungeon, will cause other rooms to be added, too.
        Condition cond = null;
        Random rand = getRandom();
        
        // Choose condition to enter the room
        float r = rand.nextFloat();
        if (dungeon.itemCount() < MAX_ELEMS && r < 0.2) {       // XXX
            // create a new condition and item for it
            Symbol elem = dungeon.makeNewItem();
            cond = new Condition(elem);
            addItemPath(dungeon, elem);
        } else if (r < 0.6) {                                   // XXX
            // make the condition one which we've used before
            Symbol elem = dungeon.getRandomPlacedItem(rand);
            if (elem != null)
                cond = new Condition(elem);
        }
        
        // Choose where to place the new room
        Room locRoom = null;
        Integer locD = null;
        r = rand.nextFloat();
        if (dungeon.roomCount() < 20 && r < 0.7) {              // XXX
            // Add padding rooms (and potentially more conditions and branches
            // along the way)
            locRoom = addItemPath(dungeon, null);
            locD = dungeon.getRandomAdjacentSpaceDirection(rand, locRoom);
            // addItemPath can create a room with no adjacent spaces, so
            // loc.second (the direction to add the new room in) might still be
            // null.
        }
        
        if (locRoom == null || locD == null) {
            // Choose an existing room with a free edge
            locRoom  = dungeon.getRandomExternalRoom(rand);
            locD = dungeon.getRandomAdjacentSpaceDirection(rand, locRoom);
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
        dungeon.add(room);
        dungeon.link(locRoom, room, cond);
        
        linkNeighbors(dungeon, room);
        
        return room;
    }
    
    protected void linkNeighbors(Dungeon dungeon, Room room) {
        Condition precond = room.getPrecond();
        Random rand = getRandom();
        
        // for each neighboring room:
        for (int d = 0; d < Direction.NUM_DIRS; ++d) {
            Room neighbor = dungeon.get(room.coords.nextInDirection(d));
            if (neighbor == null) continue;
            
            if (precond.equals(neighbor.getPrecond())) {
                // these two rooms have equivalent preconditions -- linking both
                // ways will not break any puzzles
                dungeon.link(room, neighbor);
            } else if (precond.implies(neighbor.getPrecond())) {
                if (rand.nextFloat() < 0.3) {                // XXX
                    // link from the new room to the neighbor. A link that way
                    // won't break any puzzles, but a link back the other way
                    // would!
                    dungeon.linkOneWay(room, neighbor);
                }
            }
        }
        
    }
    
    public Dungeon generate() {
        dungeon = new Dungeon();
        Room startRoom = new Room(0,0, null, new Condition());
        startRoom.setItem(new Symbol(Symbol.START));
        dungeon.add(startRoom);
        
        addItemPath(dungeon, new Symbol(Symbol.GOAL));
        
        return dungeon;
    }
    
}
