package net.bytten.metazelda.constraints;

import net.bytten.metazelda.Coords;
import net.bytten.metazelda.Dungeon;

public interface IDungeonConstraints {

    public boolean validRoomCoords(Coords c);
    public int numberSpaces();
    public int numberKeys();
    public Coords initialCoords();
    public boolean isAcceptable(Dungeon dungeon);
    
}
