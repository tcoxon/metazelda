package net.bytten.metazelda.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.bytten.gameutil.Vec2I;
import net.bytten.gameutil.Vec2ISet;

/**
 * Controls which spaces are valid for an
 * {@link net.bytten.metazelda.generators.IDungeonGenerator} to create
 * {@link Room}s in.
 * <p>
 * Essentially just a Set<{@link Vec2I}> with some convenience methods.
 *
 * @see Vec2I
 * @see SpaceConstraints
 */
public class SpaceMap {
    protected Set<Vec2I> spaces = new Vec2ISet();
    
    public int numberSpaces() {
        return spaces.size();
    }
    
    public boolean get(Vec2I c) {
        return spaces.contains(c);
    }
    
    public void set(Vec2I c, boolean val) {
        if (val)
            spaces.add(c);
        else
            spaces.remove(c);
    }
    
    private Vec2I getFirst() {
        return spaces.iterator().next();
    }
    
    public Collection<Vec2I> getBottomSpaces() {
        List<Vec2I> bottomRow = new ArrayList<Vec2I>();
        bottomRow.add(getFirst());
        int bottomY = getFirst().y;
        for (Vec2I space: spaces) {
            if (space.y > bottomY) {
                bottomY = space.y;
                bottomRow = new ArrayList<Vec2I>();
                bottomRow.add(space);
            } else if (space.y == bottomY) {
                bottomRow.add(space);
            }
        }
        return bottomRow;
    }
}
