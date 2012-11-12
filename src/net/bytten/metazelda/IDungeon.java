package net.bytten.metazelda;

import java.util.Collection;
import java.util.Set;

public interface IDungeon {

    public abstract Collection<Room> getRooms();

    public abstract int itemCount();

    public abstract Set<Symbol> getPlacedItems();

    public abstract int roomCount();

    public abstract Room get(Coords coords);

    public abstract Room get(int x, int y);

    public abstract Condition getItemPrecond(Symbol item);

    public abstract void add(Room room);

    public abstract void linkOneWay(Room room1, Room room2);

    public abstract void link(Room room1, Room room2);

    public abstract void linkOneWay(Room room1, Room room2, Symbol cond);

    public abstract void link(Room room1, Room room2, Symbol cond);

    public abstract boolean roomsAreLinked(Room room1, Room room2);
    
    public abstract Bounds getExtentBounds();

}