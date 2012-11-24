package net.bytten.metazelda;

public class Condition {

    protected int keyLevel;

    public Condition() {
        keyLevel = 0;
    }
    public Condition(Symbol e) {
        keyLevel = e.getValue()+1;
    }
    public Condition(Condition other) {
        keyLevel = other.keyLevel;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Condition) {
            return keyLevel == ((Condition)other).keyLevel;
        } else {
            return super.equals(other);
        }
    }
    
    private void add(Symbol sym) {
        keyLevel = Math.max(keyLevel, sym.getValue()+1);
    }
    private void add(Condition cond) {
        keyLevel = Math.max(keyLevel, cond.keyLevel);
    }
    public Condition and(Symbol sym) {
        Condition result = new Condition(this);
        result.add(sym);
        return result;
    }
    public Condition and(Condition other) {
        if (other == null) return this;
        Condition result = new Condition(this);
        result.add(other);
        return result;
    }
    
    public boolean implies(Condition other) {
        return keyLevel >= other.keyLevel;
    }
    public boolean implies(Symbol s) {
        return keyLevel >= s.getValue();
    }
    
    public Symbol singleSymbolDifference(Condition other) {
        // If the difference between this and other can be made up by obtaining
        // a single new symbol, this returns the symbol. If multiple or no
        // symbols are required, returns null.
        
        // Since keys are progressive (0 < 1 < 2 <...) only a single key (the
        // max) is ever needed to make up the difference.
        
        // But when we add on/off switch symbols, multiple symbols may start to
        // be needed.
        
        if (this.equals(other)) return null;
        return new Symbol(Math.max(keyLevel, other.keyLevel)-1);
    }
    
    @Override
    public String toString() {
        if (keyLevel == 0) return "";
        return new Symbol(keyLevel-1).toString();
    }
    
}
