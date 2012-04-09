package net.bytten.metazelda;

import java.util.ArrayList;
import java.util.List;

public class LennasDungeonGenerator extends DungeonGenerator {

    // Red key, Green key, Blue key, Equipment (Spring)
    private int numKeys = 4;
    // Aim for 20 rooms +/- 25%
    private int targetRoomCount = 20;
    
    public LennasDungeonGenerator(long seed) {
        super(seed);
    }

    @Override
    protected Integer chooseAdjacentSpace(Room room) {
        return super.chooseAdjacentSpace(room);
    }

    @Override
    protected boolean chooseCreateNewItem() {
        return getRandom().nextFloat() <
            1.0f - (float)dungeon.itemCount() / (float)getNumKeys();
    }
    
    @Override
    protected boolean chooseCreatePaddingRoom() {
        return getRandom().nextFloat() <
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
        return super.chooseLinkNeighborOneWay(room, neighbor);
    }

    @Override
    protected Symbol choosePlacedItem() {
        return super.choosePlacedItem();
    }

    @Override
    protected boolean chooseReuseItem() {
        return super.chooseReuseItem();
    }
    
    public boolean desirable() {
        return dungeon != null &&
            dungeon.itemCount() == getNumKeys() &&
            dungeon.roomCount() >= getTargetRoomCount() * 0.75 &&
            dungeon.roomCount() <= getTargetRoomCount() * 1.25;
    }

    @Override
    public Dungeon generate() {
        while (!desirable()) {
            if (dungeon != null) {
                System.out.println("Discarding undesirable design and trying again");
            }
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

}
