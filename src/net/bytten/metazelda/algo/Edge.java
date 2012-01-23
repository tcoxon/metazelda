package net.bytten.metazelda.algo;

public class Edge {

    protected Condition condition;
    
    public Edge() {
        condition = null;
    }
    
    public Edge(Condition condition) {
        this.condition = condition;
    }
    
    public boolean hasCondition() {
        return condition != null;
    }
    
    public Condition getCondition() {
        return condition;
    }
    
}
