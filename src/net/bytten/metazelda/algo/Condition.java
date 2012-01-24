package net.bytten.metazelda.algo;

public class Condition {

    protected Symbol symbol;
    
    // For the moment, implements only one symbol per condition.
    // It would make sense to make Condition a set of Elements that the player
    // must have.
    public Condition(Symbol e) {
        this.symbol = e;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Condition) {
            return symbol == ((Condition)other).symbol;
        } else {
            return super.equals(other);
        }
    }
    
    @Override
    public String toString() {
        return symbol.toString();
    }
    
}
