package com.liquidenthusiasm.webapp.model.motive;

public class Desire {
    private final Motive motive;
    private final int min;
    private final int ideal;
    private final int max;

    public Desire(Motive motive, int min, int ideal, int max) {
        if (ideal < min || ideal > max) {
            throw new IllegalArgumentException("'ideal' value must be between min and max. Expected: " + min + " < " + ideal + " < " + max);
        }
        this.motive = motive;
        this.min = min;
        this.ideal = ideal;
        this.max = max;
    }

    public Motive getMotive() {
        return motive;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getIdeal() {
        return ideal;
    }
}
