package net.bytten.metazelda.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Horribly messy (but compact and generic) A* implementation.
 */
public class AStar {
    public static interface IClient {
        public Collection<Integer> getNeighbors(int roomId);
        public Coords getCoords(int roomId);
    }
    
    protected class DistanceComparator implements Comparator<Integer> {
        public int compare(Integer id1, Integer id2) {
            Coords c1 = client.getCoords(id1),
                   c2 = client.getCoords(id2);
            double s1 = fScore.get(c1),
                   s2 = fScore.get(c2);
            if (s1 < s2) return -1;
            if (s1 == s2) return 0;
            return 1;
        }
    }
    protected final DistanceComparator DISTCMP = new DistanceComparator();
    
    protected IntMap<Double> gScore = new IntMap<Double>(),
                                 fScore = new IntMap<Double>();
    protected IntMap<Integer> cameFrom = new IntMap<Integer>();
    protected Set<Integer> closedSet = new TreeSet<Integer>();
    protected Queue<Integer> openSet = new PriorityQueue<Integer>(110, DISTCMP);
    protected IClient client;
    protected Integer from, to;
    
    public AStar(IClient client, Integer from, Integer to) {
        this.client = client;
        this.from = from;
        this.to = to;
    }
    
    protected double heuristicDistance(Coords pos) {
        // Manhattan distance heuristic
        Coords toPos = client.getCoords(to);
        return Math.abs(toPos.x - pos.x) + Math.abs(toPos.y - pos.y);
    }
    
    protected void updateFScore(Integer id) {
        fScore.put(id, gScore.get(id) + heuristicDistance(client.getCoords(id)));
    }
    
    public List<Integer> solve() {
        /* See this page for the algorithm:
         * http://en.wikipedia.org/wiki/A*_search_algorithm
         */
        openSet.add(from);
        gScore.put(from, 0.0);
        updateFScore(from);
        
        while (!openSet.isEmpty()) {
            Integer current = openSet.remove();
            
            if (current.equals(to))
                return reconstructPath();
            
            closedSet.add(current);
            
            for (Integer neighbor: client.getNeighbors(current)) {
                
                if (closedSet.contains(neighbor))
                    continue;
                
                double dist = client.getCoords(current).distance(
                        client.getCoords(neighbor));
                double g = gScore.get(current) + dist;
                
                if (!openSet.contains(neighbor) || g < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, g);
                    updateFScore(neighbor);
                    openSet.add(neighbor);
                }
            }
        }
        return null;
    }
    
    protected Integer nextStep() {
        List<Integer> path = solve();
        if (path == null || path.size() == 0) return null;
        return path.get(0);
    }
    
    protected List<Integer> reconstructPath() {
        List<Integer> result = new ArrayList<Integer>();
        Integer current = to;
        while (!current.equals(from)) {
            result.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(result);
        return result;
    }

}
