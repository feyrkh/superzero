package com.liquidenthusiasm.webapp.util;

import java.util.stream.Collector;
import java.util.stream.Stream;

public class Maxinator {

    @FunctionalInterface
    public interface QualityFunction<V> {
        double computeQuality(V value);
    }

    private Maxinator() {
//        no public instances
    }

    private static final class AccumulateResult<T> {
        T bestItem = null;
        double bestScore = Double.MIN_VALUE;

        public T getBestItem() {
            return bestItem;
        }

        public void accept(QualityFunction<T> function, T item) {
            System.out.println("Quality function executed for " + item);
            double score = function.computeQuality(item);
            if (score > bestScore) {
                bestScore = score;
                bestItem = item;
            }
        }

        public AccumulateResult<T> combine(AccumulateResult<T> r) {
            if (r.bestScore > bestScore) {
                bestScore = r.bestScore;
                bestItem = r.bestItem;
            }
            return this;
        }

    }

    public static <T> T getBest(final QualityFunction<T> qualityFunction, final Stream<T> population) {
        return population.parallel().collect(Collector.of(
                        AccumulateResult<T>::new,
                        (a, t) -> a.accept(qualityFunction, t),
                        (a, b) -> a.combine(b))
        ).getBestItem();
    }
}