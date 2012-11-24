package net.bytten.metazelda.generators;

import net.bytten.metazelda.IDungeon;

public interface IDungeonGenerator {

    public void generate();
    public IDungeon getDungeon();
    
}
