package net.bytten.metazelda;

public class Symbol {
    public static final int
        START = -1,
        GOAL = -2,
        BOSS = -3,
        SWITCH_ON = -4,     // used as a condition (lock)
        SWITCH_OFF = -5,    // used as a condition (lock)
        SWITCH = -6;        // used as an item (key) within a room

    protected final int value;
    protected final String name;
    
    public Symbol(int value) {
        this.value = value;
        
        if (value == START)
            name = "Start";
        else if (value == GOAL)
            name = "Goal";
        else if (value == BOSS)
            name = "Boss";
        else if (value == SWITCH_ON)
            name = "ON";
        else if (value == SWITCH_OFF)
            name = "OFF";
        else if (value == SWITCH)
            name = "SW";
        else if (value >= 0 && value < 26)
            name = Character.toString((char)((int)'A' + value));
        else
            name = Integer.toString(value);
    }
    
    public Symbol(int value, String name) {
        this.value = value;
        this.name = name;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Symbol) {
            return value == ((Symbol)other).value;
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
    
    public boolean isBoss() {
        return value == BOSS;
    }
    
    public boolean isSwitch() {
        return value == SWITCH;
    }
    
    public boolean isSwitchState() {
        return value == SWITCH_ON || value == SWITCH_OFF;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
