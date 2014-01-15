package net.bytten.metazelda.util;

import java.util.Arrays;

public class Pair<T1, T2> {

    public final T1 first;
    public final T2 second;
    
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (first == null) {
            sb.append("null");
        } else {
            sb.append(first.toString());
        }
        sb.append(",");
        if (second == null) {
            sb.append("null");
        } else {
            sb.append(second.toString());
        }
        return sb.toString();
    }
    
    
    public boolean equals(Object other) {
        if (!(other instanceof Pair<?,?>)) {
            return super.equals(other);
        }
        Pair<?,?> o = (Pair<?,?>)other;
        return (first == o.first || (first != null && first.equals(o.first))) &&
            (second == o.second || (second != null && second.equals(o.second)));
    }
    
    public int hashCode() {
        return Arrays.hashCode(new Object[]{ first, second });
    }
    
}
