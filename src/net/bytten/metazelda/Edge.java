package net.bytten.metazelda;

public class Edge {

    protected Symbol symbol;
    
    public Edge() {
        symbol = null;
    }
    
    public Edge(Symbol symbol) {
        this.symbol = symbol;
    }
    
    public boolean hasSymbol() {
        return symbol != null;
    }
    
    public Symbol getSymbol() {
        return symbol;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Edge) {
            Edge o = (Edge)other;
            return symbol == o.symbol || symbol.equals(o.symbol);
        } else {
            return super.equals(other);
        }
    }
    
}
