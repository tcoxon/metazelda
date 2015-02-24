package net.bytten.metazelda.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.bytten.gameutil.Vec2I;
import net.bytten.gameutil.algorithms.AStar;
import net.bytten.metazelda.Dungeon;
import net.bytten.metazelda.Edge;
import net.bytten.metazelda.Room;
import net.bytten.metazelda.Symbol;
import net.bytten.metazelda.constraints.IDungeonConstraints;
import net.bytten.gameutil.logging.ILogger;

/**
 * Extends DungeonGenerator to choose the least nonlinear one immediately
 * available. This saves the player from having to do a lot of backtracking.
 * 
 * Ignores switches for now.
 */
public class LinearDungeonGenerator extends DungeonGenerator {
    
    public static final int MAX_ATTEMPTS = 10;

    public LinearDungeonGenerator(ILogger logger, long seed,
            IDungeonConstraints constraints) {
        super(logger, seed, constraints);
    }
    
    public LinearDungeonGenerator(long seed, IDungeonConstraints constraints) {
        this(null, seed, constraints);
    }
    
    private class AStarClient implements AStar.IClient<Integer> {
        
        private int keyLevel;
        
        public AStarClient(int keyLevel) {
            this.keyLevel = keyLevel;
        }

        @Override
        public Collection<Integer> getNeighbors(Integer roomId) {
            List<Integer> ids = new ArrayList<Integer>();
            for (Edge edge: dungeon.get(roomId).getEdges()) {
                if (!edge.hasSymbol() || edge.getSymbol().getValue() < keyLevel) {
                    ids.add(edge.getTargetRoomId());
                }
            }
            return ids;
        }

        @Override
        public Vec2I getVec2I(Integer roomId) {
            return dungeon.get(roomId).getCenter();
        }
    }
    
    private List<Integer> astar(int start, int goal, final int keyLevel) {
        AStar<Integer> astar = new AStar<Integer>(new AStarClient(keyLevel), start, goal);
        return astar.solve();
    }
    
    /**
     * Nonlinearity is measured as the number of rooms the player would have to
     * pass through multiple times to get to the goal room (collecting keys and
     * unlocking doors along the way).
     * 
     * Uses A* to find a path from the entry to the first key, from each key to
     * the next key and from the last key to the goal.
     * 
     * @return  The number of rooms passed through multiple times
     */
    public int measureNonlinearity() {
        List<Room> keyRooms = new ArrayList<Room>(constraints.getMaxKeys());
        for (int i = 0; i < constraints.getMaxKeys(); ++i) {
            keyRooms.add(null);
        }
        for (Room room: dungeon.getRooms()) {
            if (room.getItem() == null) continue;
            Symbol item = room.getItem();
            if (item.getValue() >= 0 && item.getValue() < keyRooms.size())
                keyRooms.set(item.getValue(), room);
        }
        // for N >= 0: keyRooms[N] = location of key N
        
        Room current = dungeon.findStart(),
                goal = dungeon.findGoal();
        // Clients may disable generation of the goal room -- the equivalent
        // 'ending' room becomes the boss room.
        if (goal == null) goal = dungeon.findBoss();
        assert current != null && goal != null;
        int nextKey = 0, nonlinearity = 0;
        
        Set<Integer> visitedRooms = new TreeSet<Integer>();
        while (current != goal) {
            Room intermediateGoal;
            if (nextKey == constraints.getMaxKeys())
                intermediateGoal = goal;
            else
                intermediateGoal = keyRooms.get(nextKey);
            
            List<Integer> steps = astar(current.id, intermediateGoal.id,
                    nextKey);
            for (Integer id: steps) {
                if (visitedRooms.contains(id)) ++nonlinearity;
            }
            visitedRooms.addAll(steps);
            
            nextKey++;
            current = dungeon.get(steps.get(steps.size()-1));
        }
        return nonlinearity;
    }

    @Override
    public void generate() {
        int attempts = 0, currentNonlinearity = Integer.MAX_VALUE;
        int bestAttempt = 0;
        Dungeon currentBest = null;
        while (attempts++ < MAX_ATTEMPTS) {
            super.generate();
            
            int nonlinearity = measureNonlinearity();
            log("Dungeon " + attempts + " nonlinearity: "+
                    nonlinearity);
            if (nonlinearity < currentNonlinearity) {
                currentNonlinearity = nonlinearity;
                bestAttempt = attempts;
                currentBest = dungeon;
            }
        }
        assert currentBest != null;
        log("Chose " + bestAttempt + " nonlinearity: "+
                currentNonlinearity);
        dungeon = currentBest;
    }

}
