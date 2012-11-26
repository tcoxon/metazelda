package net.bytten.metazelda.constraints;

import java.util.Collection;

import net.bytten.metazelda.Coords;
import net.bytten.metazelda.Dungeon;

public interface IDungeonConstraints {

    public boolean validRoomCoords(Coords c);
    public int getMaxSpaces();
    public int getMaxKeys();
    public int getMaxSwitches();
    public Collection<Coords> initialCoords();
    public boolean isAcceptable(Dungeon dungeon);
    
}
