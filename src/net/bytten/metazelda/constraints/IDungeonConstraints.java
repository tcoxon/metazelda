package net.bytten.metazelda.constraints;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.bytten.gameutil.Vec2I;
import net.bytten.gameutil.Pair;
import net.bytten.metazelda.IDungeon;
import net.bytten.metazelda.Symbol;

/**
 * Implementing classes may specify constraints to be placed on Dungeon
 * generation.
 * 
 * @see net.bytten.metazelda.generators.IDungeonGenerator
 */
public interface IDungeonConstraints {

    /**
     * @return  the maximum number of Rooms an 
     * {@link net.bytten.metazelda.generators.IDungeonGenerator} may
     *          place in an {@link net.bytten.metazelda.IDungeon}
     */
    public int getMaxRooms();
    
    /**
     * @return  the maximum number of keys an 
     * {@link net.bytten.metazelda.generators.IDungeonGenerator} may
     *          place in an {@link net.bytten.metazelda.IDungeon}
     */
    public int getMaxKeys();

    /**
     * Gets the number of switches the
     * {@link net.bytten.metazelda.generators.IDungeonGenerator} is allowed to
     * place in an {@link net.bytten.metazelda.IDungeon}.
     * Note only one switch is ever placed due to limitations of the current
     * algorithm.
     * 
     * @return  the maximum number of switches an
     * {@link net.bytten.metazelda.generators.IDungeonGenerator} may
     *          place in an {@link net.bytten.metazelda.IDungeon}
     */
    public int getMaxSwitches();
    
    /**
     * Gets the collection of ids from which an
     * {@link net.bytten.metazelda.generators.IDungeonGenerator} is allowed to
     * pick the entrance room.
     * 
     * @return the collection of ids
     */
    public Collection<Integer> initialRooms();
    
    /**
     * @return a weighted list of ids of rooms that are adjacent to the room
     * with the given id.
     */
    public List<Pair<Double,Integer>> getAdjacentRooms(int id, int keyLevel);
    
    /**
     * @return desired probability for an extra edge to be added between the
     * given rooms during the graphify phase.
     */
    public double edgeGraphifyProbability(int id, int nextId);
    
    /**
     * @return a set of Coords which the room with the given id occupies.
     */
    public Set<Vec2I> getCoords(int id);
    
    /**
     * Runs post-generation checks to determine the suitability of the dungeon.
     * 
     * @param dungeon   the {@link net.bytten.metazelda.IDungeon} to check
     * @return  true to keep the dungeon, or false to discard the dungeon and
     *          attempt generation again
     */
    public boolean isAcceptable(IDungeon dungeon);
    
    public boolean roomCanFitItem(int id, Symbol key);
    
}
