package net.bytten.metazelda.algo;

public class Element {
    public static final int
        START = -1,
        GOAL = -2;

    protected final int value;
    
    public Element(int x) {
        value = x;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Element) {
            return value == ((Element)other).value;
        } else {
            return super.equals(other);
        }
    }
    
    @Override
    public int hashCode() {
        return value;
    }
    
    public int getValue() {
        return value;
    }
    
    public boolean isStart() {
        return value == START;
    }
    
    public boolean isGoal() {
        return value == GOAL;
    }
    
    @Override
    public String toString() {
        if (value == START)
            return "Start";
        if (value == GOAL)
            return "Goal";
        if (value < 26)
            return Character.toString((char)((int)'A' + value));
        return Integer.toString(value);
    }
    
}
