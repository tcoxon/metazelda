package net.bytten.metazelda;

public class Direction {

    public static final int
        N = 0,
        E = 1,
        S = 2,
        W = 3,
        NUM_DIRS = 4;
    
    public static int oppositeDirection(int d) {
        switch (d) {
        case N: return S;
        case E: return W;
        case S: return N;
        case W: return E;
        default:
            // Should not occur
            throw new RuntimeException("Unknown direction");
        }
    }
}
