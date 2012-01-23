package net.bytten.metazelda.algo;

public class Condition {

    protected Element element;
    
    // For the moment, implements only one element per condition.
    // It would make sense to make Condition a set of Elements that the player
    // must have.
    public Condition(Element e) {
        this.element = e;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Condition) {
            return element == ((Condition)other).element;
        } else {
            return super.equals(other);
        }
    }
    
    @Override
    public String toString() {
        return element.toString();
    }
    
}
