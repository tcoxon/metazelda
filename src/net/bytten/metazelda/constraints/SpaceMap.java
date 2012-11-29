package net.bytten.metazelda.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.bytten.metazelda.Coords;

/**
 * Controls which spaces are valid for an
 * {@link net.bytten.metazelda.generators.IDungeonGenerator} to create
 * {@link Room}s in.
 * <p>
 * Essentially just a Set<{@link Coords}> with some convenience methods.
 * 
 * @see Coords
 * @see SpaceConstraints
 */
public class SpaceMap {
    protected Set<Coords> spaces = new TreeSet<Coords>();
    
    public int numberSpaces() {
        return spaces.size();
    }
    
    public boolean get(Coords c) {
        return spaces.contains(c);
    }
    
    public void set(Coords c, boolean val) {
        if (val)
            spaces.add(c);
        else
            spaces.remove(c);
    }
    
    private Coords getFirst() {
        return spaces.iterator().next();
    }
    
    public Collection<Coords> getBottomSpaces() {
        List<Coords> bottomRow = new ArrayList<Coords>();
        bottomRow.add(getFirst());
        int bottomY = getFirst().y;
        for (Coords space: spaces) {
            if (space.y > bottomY) {
                bottomY = space.y;
                bottomRow = new ArrayList<Coords>();
                bottomRow.add(space);
            } else if (space.y == bottomY) {
                bottomRow.add(space);
            }
        }
        return bottomRow;
    }
}