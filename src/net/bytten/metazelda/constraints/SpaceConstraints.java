package net.bytten.metazelda.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.bytten.metazelda.Coords;

public class SpaceConstraints extends CountConstraints {

    public static class SpaceMap {
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
    
    protected SpaceMap spaceMap;
    
    public SpaceConstraints(SpaceMap spaceMap, int numKeys) {
        super(spaceMap.numberSpaces(), numKeys);
        this.spaceMap = spaceMap;
    }

    @Override
    public boolean validRoomCoords(Coords c) {
        return super.validRoomCoords(c) && spaceMap.get(c);
    }

    @Override
    public Collection<Coords> initialCoords() {
        return spaceMap.getBottomSpaces();
    }
    
    

}
