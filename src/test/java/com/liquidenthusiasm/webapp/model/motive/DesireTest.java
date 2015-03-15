package com.liquidenthusiasm.webapp.model.motive;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class DesireTest {
    int base;

    @Before
    public void setup() {
        base = (int) (Math.random() * 10 - 20);
    }

    private Motive motive = mock(Motive.class);

    private Desire buildDesire(int idealOffset, int maxOffset) {
        return new Desire(motive, base, base + idealOffset, base + maxOffset);
    }

    @Test
    public void idealCanBeBetweenMinMax() {
        Desire d = buildDesire(5, 10);
    }

    @Test
    public void idealCanBeAtMin() {
        Desire d = buildDesire(0, 10);
    }

    @Test
    public void idealCanBeAtMax() {
        Desire d = buildDesire(10, 10);
    }

    @Test
    public void minAndMaxCanBeSame() {
        Desire d = buildDesire(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void idealCanNotBeBelowMin() {
        Desire d = buildDesire(-1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void idealCanNotBeAboveMax() {
        Desire d = buildDesire(11, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void minCanNotBeAboveMax() {
        Desire d = buildDesire(0, -1);
    }

}