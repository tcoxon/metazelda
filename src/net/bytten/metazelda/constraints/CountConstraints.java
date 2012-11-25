package net.bytten.metazelda.constraints;

import java.util.Arrays;
import java.util.Collection;

import net.bytten.metazelda.Coords;
import net.bytten.metazelda.Dungeon;

public class CountConstraints implements IDungeonConstraints {

    protected final int numSpaces, numKeys;
    
    public CountConstraints(int numSpaces, int numKeys) {
        this.numSpaces = numSpaces;
        this.numKeys = numKeys;
    }
    
    @Override
    public int numberSpaces() {
        return numSpaces;
    }
    
    @Override
    public boolean validRoomCoords(Coords c) {
        return c != null && c.y <= 0;
    }

    @Override
    public Collection<Coords> initialCoords() {
        return Arrays.asList(new Coords(0,0));
    }

    @Override
    public int numberKeys() {
        return numKeys;
    }
    
    @Override
    public boolean isAcceptable(Dungeon dungeon) {
        return true;
    }

}
