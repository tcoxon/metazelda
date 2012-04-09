package net.bytten.metazelda;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Condition {

    // The condition is the logical intersection of these symbols
    protected final Set<Symbol> symbols;

    public Condition() {
        this.symbols = new HashSet<Symbol>();
    }
    public Condition(Symbol e) {
        this();
        symbols.add(e);
    }
    public Condition(Condition other) {
        this.symbols = new HashSet<Symbol>(other.symbols);
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Condition) {
            return symbols.equals(((Condition)other).symbols);
        } else {
            return super.equals(other);
        }
    }
    
    private void add(Symbol sym) {
        symbols.add(sym);
    }
    private void add(Condition cond) {
        symbols.addAll(cond.symbols);
    }
    public Condition and(Symbol sym) {
        Condition result = new Condition(this);
        result.add(sym);
        return result;
    }
    public Condition and(Condition other) {
        if (other == null) return this;
        Condition result = new Condition(this);
        result.symbols.addAll(other.symbols);
        return result;
    }
    
    public boolean implies(Condition other) {
        return symbols.containsAll(other.symbols);
    }
    public boolean implies(Symbol s) {
        return symbols.contains(s);
    }
    
    public Condition closure(Map<Symbol,Condition> implications) {
        // given s => a1,a2,...an, where:
        //     for each (s,a) entry in implications,
        // returns entire set of symbols that are implied by this condition
        Condition result = new Condition(this);
        for (Symbol s: implications.keySet()) {
            if (result.implies(s)) {
                result.add(implications.get(s));
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Symbol> syms = new ArrayList<Symbol>(symbols);
        for (int i = 0; i < syms.size(); ++i) {
            if (i != 0) sb.append(',');
            sb.append(syms.get(i).toString());
        }
        return sb.toString();
    }
    
}
