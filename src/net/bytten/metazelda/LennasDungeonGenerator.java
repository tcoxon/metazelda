package net.bytten.metazelda;

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
        return super.chooseExistingRoom();
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

}
