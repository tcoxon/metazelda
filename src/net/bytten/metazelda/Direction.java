package net.bytten.metazelda;

public enum Direction {

    N(0,    0,-1),
    E(1,    1, 0),
    S(2,    0, 1),
    W(3,    -1,0);
    
    public static final int NUM_DIRS = 4;
    public final int code, x, y;
    
    private Direction(int code, int x, int y) {
        this.code = code;
        this.x = x;
        this.y = y;
    }
    
    public static Direction oppositeDirection(Direction d) {
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
    
    public static Direction fromCode(int code) {
        switch (code) {
        case 0: return N;
        case 1: return E;
        case 2: return S;
        case 3: return W;
        default: return null;
        }
    }
}
