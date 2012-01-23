package net.bytten.metazelda.algo;

public class Condition {

    protected Element element;
    
    // For the moment, implements only one element per condition.
    // It would make sense to make Condition a set of Elements that the player
    // must have.
    public Condition(Element e) {
        this.element = e;
    }
    
}
