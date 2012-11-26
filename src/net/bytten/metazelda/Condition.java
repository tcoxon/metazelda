package net.bytten.metazelda;

public class Condition {

    public static enum SwitchState {
        EITHER, OFF, ON;
        
        public Symbol toSymbol() {
            switch (this) {
            case OFF:
                return new Symbol(Symbol.SWITCH_OFF);
            case ON:
                return new Symbol(Symbol.SWITCH_ON);
            default:
                return null;
            }
        }
        
        public SwitchState invert() {
            switch (this) {
            case OFF: return ON;
            case ON: return OFF;
            default:
                return this;
            }
        }
    };
    
    protected int keyLevel;
    
    /* Tristate variable meanings:
     *  EITHER: switch could be in any state
     *  OFF: switch is off
     *  ON: switch is on
     */
    protected SwitchState switchState;

    public Condition() {
        keyLevel = 0;
        switchState = SwitchState.EITHER;
    }
    
    public Condition(Symbol e) {
        if (e.getValue() == Symbol.SWITCH_OFF) {
            keyLevel = 0;
            switchState = SwitchState.OFF;
        } else if (e.getValue() == Symbol.SWITCH_ON) {
            keyLevel = 0;
            switchState = SwitchState.ON;
        } else {
            keyLevel = e.getValue()+1;
            switchState = SwitchState.EITHER;
        }
    }
    
    public Condition(Condition other) {
        keyLevel = other.keyLevel;
        switchState = other.switchState;
    }
    
    public Condition(SwitchState switchState) {
        keyLevel = 0;
        this.switchState = switchState;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Condition) {
            Condition o = (Condition)other;
            return keyLevel == o.keyLevel && switchState == o.switchState;
        } else {
            return super.equals(other);
        }
    }
    
    private void add(Symbol sym) {
        if (sym.getValue() == Symbol.SWITCH_OFF) {
            assert switchState == null;
            switchState = SwitchState.OFF;
        } else if (sym.getValue() == Symbol.SWITCH_ON) {
            assert switchState == null;
            switchState = SwitchState.ON;
        } else {
            keyLevel = Math.max(keyLevel, sym.getValue()+1);
        }
    }
    private void add(Condition cond) {
        if (switchState == SwitchState.EITHER) {
            switchState = cond.switchState;
        } else {
            assert switchState == cond.switchState;
        }
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
        return keyLevel >= other.keyLevel &&
                (switchState == other.switchState ||
                other.switchState == SwitchState.EITHER);
    }
    public boolean implies(Symbol s) {
        return implies(new Condition(s));
    }
    
    public Symbol singleSymbolDifference(Condition other) {
        // If the difference between this and other can be made up by obtaining
        // a single new symbol, this returns the symbol. If multiple or no
        // symbols are required, returns null.
        
        if (this.equals(other)) return null;
        if (switchState == other.switchState) {
            return new Symbol(Math.max(keyLevel, other.keyLevel)-1);
        } else {
            if (keyLevel != other.keyLevel) return null;
            // Multiple symbols needed        ^^^
            
            assert switchState != other.switchState;
            if (switchState != SwitchState.EITHER &&
                    other.switchState != SwitchState.EITHER)
                return null;
            
            SwitchState nonEither = switchState != SwitchState.EITHER
                    ? switchState
                    : other.switchState;
            
            return new Symbol(nonEither == SwitchState.ON
                    ? Symbol.SWITCH_ON
                    : Symbol.SWITCH_OFF);
        }
    }
    
    @Override
    public String toString() {
        String result = "";
        if (keyLevel != 0) {
            result += new Symbol(keyLevel-1).toString();
        }
        if (switchState != SwitchState.EITHER) {
            if (!result.equals("")) result += ",";
            result += switchState.toSymbol().toString();
        }
        return result;
    }
    
    public int getKeyLevel() {
        return keyLevel;
    }
    
    public SwitchState getSwitchState() {
        return switchState;
    }
    
}
