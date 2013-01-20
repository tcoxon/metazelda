package net.bytten.metazelda.constraints;

import java.util.Arrays;
import java.util.Collection;

import net.bytten.metazelda.IDungeon;
import net.bytten.metazelda.util.Coords;

/**
 * Limits the {@link net.bytten.metazelda.generators.IDungeonGenerator} only in
 * the <i>number</i> of keys, switches and rooms it is allowed to place.
 * 
 * @see IDungeonConstraints
 */
public class CountConstraints implements IDungeonConstraints {

    protected int maxSpaces, maxKeys, maxSwitches;
    
    public CountConstraints(int maxSpaces, int maxKeys, int maxSwitches) {
        this.maxSpaces = maxSpaces;
        this.maxKeys = maxKeys;
        this.maxSwitches = maxSwitches;
    }
    
    @Override
    public int getMaxSpaces() {
        return maxSpaces;
    }
    
    public void setMaxSpaces(int maxSpaces) {
        this.maxSpaces = maxSpaces;
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
    public int getMaxKeys() {
        return maxKeys;
    }
    
    public void setMaxKeys(int maxKeys) {
        this.maxKeys = maxKeys;
    }
    
    @Override
    public boolean isAcceptable(IDungeon dungeon) {
        return true;
    }

    @Override
    public int getMaxSwitches() {
        return maxSwitches;
    }

    public void setMaxSwitches(int maxSwitches) {
        this.maxSwitches = maxSwitches;
    }

}
