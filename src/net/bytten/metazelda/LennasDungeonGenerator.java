package net.bytten.metazelda;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LennasDungeonGenerator extends DungeonGenerator {

    public static final Symbol ITEM_BOSS = new Symbol(-3, "Boss");
    
    // Red key, Green key, Blue key, Equipment (Spring)
    private int numKeys = 4;
    // Aim for 20 rooms +/- 25%
    private int targetRoomCount = 20;
    
    private boolean abort = false;
    
    public LennasDungeonGenerator(long seed) {
        super(seed);
    }
    
    public LennasDungeonGenerator(Random random) {
        super(random);
    }
    
    private float computeSpaceChoiceProbFactor(Room room, Coords xy) {
        int x = Math.abs(xy.x),
            y = Math.abs(xy.y);
        if (x < Math.abs(room.coords.x)) x = 0;
        if (y < Math.abs(room.coords.y)) y = 0;
        int dist = Math.max(x, y);
        return 1.0f / (2*dist + 1.0f);
    }

    @Override
    protected Integer chooseAdjacentSpace(Room room) {
        // Choose a random direction, but bias it so that we stay near 0,0
        float dirs[] = new float[Direction.NUM_DIRS];
        float sum = 0.0f;
        boolean atLeastOne = false;
        for (int d = 0; d < dirs.length; ++d) {
            Coords xy = room.coords.nextInDirection(d);
            if (newRoomAllowedInSpace(xy)) {
                float val = computeSpaceChoiceProbFactor(room, xy);
                dirs[d] = val;
                sum += val;
                atLeastOne = true;
            } else {
                dirs[d] = 0.0f;
            }
        }
        if (!atLeastOne) return null;
        for (int d = 0; d < dirs.length; ++d) {
            dirs[d] /= sum;
        }
        float p = getRandom().nextFloat();
        float x = 0.0f;
        int d = 0;
        while (d < dirs.length) {
            x += dirs[d];
            if (x > p) {
                return d;
            }
            ++d;
        }
        return null;
    }

    @Override
    protected boolean chooseCreateNewItem() {
        // choose probabilities to aim towards a specific number of keys
        return getRandom().nextFloat() <
            1.0f - (float)dungeon.itemCount() / (float)getNumKeys();
    }
    
    @Override
    protected boolean chooseCreatePaddingRoom(int depth) {
        // choose probabilities to aim towards a specific number of rooms
        return depth < 3 && getRandom().nextFloat() <
            1.0f - (float)dungeon.roomCount() / (float)getTargetRoomCount();
    }

    @Override
    protected Room chooseExistingRoom() {
        List<Room> rooms = dungeon.computeBoundaryRooms();
        List<Room> filtered = new ArrayList<Room>(rooms.size());
        for (Room room: rooms) {
            if (room.coords.y < 0)
                filtered.add(room);
        }
        if (filtered.size() == 0)
            filtered.add(dungeon.get(0,0));
        return filtered.get(getRandom().nextInt(filtered.size()));
    }

    @Override
    protected boolean chooseLinkNeighborOneWay(Room room, Room neighbor) {
        // TODO disabled because Lenna's Inception does not have a mechanism for
        // this yet.
        return false;
    }

    @Override
    protected Symbol choosePlacedItem() {
        return super.choosePlacedItem();
    }

    @Override
    protected boolean chooseReuseItem() {
        return super.chooseReuseItem();
    }
    
    protected float measureLinearity() {
        // Crudely measure linearity so we can generate more interesting
        // dungeons
        int linRooms = 0, allRooms = 0;
        for (Room room: dungeon.getRooms()) {
            ++allRooms;
            if (room.linkCount() < 3) {
                ++linRooms;
            }
        }
        return (float)linRooms / (float)allRooms;
    }
    
    // Obtained through several experiments. Designs with linearity > 0.7 tend
    // to be boring. Designs with linearity < 0.7 are more interesting.
    public static final float MAX_LINEARITY = 0.7f;
    
    public boolean desirable() {
        if (dungeon == null || abort) return false;
        
        float linearity = measureLinearity();
        System.out.println("Design linearity: "+linearity);
        
        return dungeon.itemCount() == getNumKeys() &&
            dungeon.roomCount() >= getTargetRoomCount() * 0.75 &&
            dungeon.roomCount() <= getTargetRoomCount() * 1.25 &&
            measureLinearity() < MAX_LINEARITY;
    }

    @Override
    public Dungeon generate() {
        while (!desirable()) {
            if (dungeon != null) {
                if (abort) {
                    System.out.println("Design aborted - no space for goal");
                } else {
                    System.out.println("Discarding undesirable design and "+
                            "trying again");
                }
            }
            abort = false;
            super.generate();
        }
        return dungeon;
    }

    public int getNumKeys() {
        return numKeys;
    }

    public void setNumKeys(int numKeys) {
        this.numKeys = numKeys;
    }

    public int getTargetRoomCount() {
        return targetRoomCount;
    }

    public void setTargetRoomCount(int targetRoomCount) {
        this.targetRoomCount = targetRoomCount;
    }

    @Override
    protected boolean newRoomAllowedInSpace(Coords xy) {
        return super.newRoomAllowedInSpace(xy) && xy.y <= 0;
    }

    @Override
    public Room addItemPath(Symbol item, int depth) {
        // overriding to add boss rooms
        if (item != null && item.isGoal()) {
            Room bossRoom = addItemPath(ITEM_BOSS, depth);
            Integer d = chooseAdjacentSpace(bossRoom);
            if (d == null) {
                // There are no spaces next to the boss room for the goal room
                // to go in. Abort the dungeon.
                abort = true;
                return bossRoom;
            } else {
                Room goalRoom = new Room(bossRoom.coords.nextInDirection(d),
                        item, bossRoom.getPrecond());
                synchronized (dungeon) {
                    dungeon.add(goalRoom);
                    dungeon.link(bossRoom, goalRoom);
                }
                return goalRoom;
            }
        } else {
            return super.addItemPath(item, depth);
        }
    }

}
