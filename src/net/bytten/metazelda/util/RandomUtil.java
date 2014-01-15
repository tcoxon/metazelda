package net.bytten.metazelda.util;

import java.util.List;
import java.util.Random;

public class RandomUtil {

    public static<T> Pair<Double, T> choice(Random random,
            List<Pair<Double,T>> options) {
        
        double total = 0.0;
        for (Pair<Double,T> elem: options) {
            total += elem.first;
        }
        total *= random.nextDouble();
        for (Pair<Double,T> elem: options) {
            total -= elem.first;
            if (total < 0) return elem;
        }
        return null;
    }
    
}
