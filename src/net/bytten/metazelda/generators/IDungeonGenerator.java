package net.bytten.metazelda.generators;

import net.bytten.metazelda.IDungeon;

/**
 * Interface for classes that provide methods to procedurally generate new
 * {@link IDungeon}s.
 */
public interface IDungeonGenerator {

    /**
     * Generates a new {@link IDungeon}.
     */
    public void generate();
    
    /**
     * Gets the most recently generated {@link IDungeon}.
     * 
     * @return the most recently generated IDungeon
     */
    public IDungeon getDungeon();
    
}
