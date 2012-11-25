package net.bytten.metazelda.constraints;

import java.util.Collection;

import net.bytten.metazelda.Coords;

public class SpaceConstraints extends CountConstraints {

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
