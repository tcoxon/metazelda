package net.bytten.metazelda.constraints;

import java.util.Collection;

import net.bytten.metazelda.Coords;

public class SpaceConstraints extends CountConstraints {

    public static final int DEFAULT_MAX_KEYS = 4,
            DEFAULT_MAX_SWITCHES = 1;
    
    protected SpaceMap spaceMap;
    
    public SpaceConstraints(SpaceMap spaceMap) {
        super(spaceMap.numberSpaces(), DEFAULT_MAX_KEYS, DEFAULT_MAX_SWITCHES);
        this.spaceMap = spaceMap;
    }

    @Override
    public boolean validRoomCoords(Coords c) {
        return c != null && spaceMap.get(c);
    }

    @Override
    public Collection<Coords> initialCoords() {
        return spaceMap.getBottomSpaces();
    }
    
    

}
