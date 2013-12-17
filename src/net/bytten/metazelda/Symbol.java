package net.bytten.metazelda;

/**
 * Represents a single key or lock within the lock-and-key puzzle.
 * <p>
 * Each Symbol has a 'value'. Two Symbols are equivalent iff they have the same
 * 'value'.
 */
public class Symbol {
    /**
     * Symbol map with special meanings.
     * <p>
     * Certain items (such as START, GOAL, BOSS) serve no purpose in the puzzle
     * other than as markers for the client of the library to place special game
     * objects.
     * <p>
     * The SWITCH_ON and SWITCH_OFF symbols do not appear in rooms, only in
     * {@link Condition}s and {@link Edge}s.
     * <p>
     * The SWITCH item does not give the player the SWITCH symbol, instead the
     * player may choose to either
     * <ul>
     * <li>lose the SWITCH_OFF symbol (if they have it), and gain the SWITCH_ON
     *      symbol; or
     * <li>lose the SWITCH_ON symbol (if they have it), and gain the SWITCH_OFF
     *      symbol.
     * <ul>
     * <p>
     */
    public static final int
        START = -1,
        GOAL = -2,
        BOSS = -3,
        SWITCH_ON = -4,     // used as a condition (lock)
        SWITCH_OFF = -5,    // used as a condition (lock)
        SWITCH = -6;        // used as an item (key) within a room

    protected final int value;
    protected final String name;
    
    /**
     * Creates a Symbol with the given value.
     * 
     * @param value value to give the Symbol
     */
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
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Symbol) {
            return value == ((Symbol)other).value;
        } else {
            return super.equals(other);
        }
    }
    
    public static boolean equals(Symbol a, Symbol b) {
        if (a == b) return true;
        if (b == null) return a.equals(b);
        return b.equals(a);
    }
    
    @Override
    public int hashCode() {
        return value;
    }
    
    /**
     * @return the value of the Symbol
     */
    public int getValue() {
        return value;
    }
    
    /**
     * @return whether the symbol is the special START symbol
     */
    public boolean isStart() {
        return value == START;
    }
    
    /**
     * @return whether the symbol is the special GOAL symbol
     */
    public boolean isGoal() {
        return value == GOAL;
    }
    
    /**
     * @return whether the symbol is the special BOSS symbol
     */
    public boolean isBoss() {
        return value == BOSS;
    }
    
    /**
     * @return whether the symbol is the special SWITCH symbol
     */
    public boolean isSwitch() {
        return value == SWITCH;
    }
    
    /**
     * @return whether the symbol is one of the special SWITCH_{ON,OFF} symbols
     */
    public boolean isSwitchState() {
        return value == SWITCH_ON || value == SWITCH_OFF;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
