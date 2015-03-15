package com.liquidenthusiasm.webapp.model.motive;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MotiveTest {
    @Test
    public void nameConstructorSetsName() {
        assertEquals("boredom", new Motive("boredom").getName());
    }

    @Test
    public void namesGetSet() {
        assertEquals("hunger", Motive.hunger.getName());
    }

    @Test
    public void canSetMinMax() {
        Motive m = new Motive("m").min(0).max(50);
        assertEquals("min", 0, m.getMin());
        assertEquals("max", 50, m.getMax());
    }

    @Test(expected = IllegalArgumentException.class)
    public void minCanNotBeHigherThanMax() {
        Motive m = new Motive("m").max(10).min(15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void maxCanNotBeLowerThanMin() {
        Motive m = new Motive("m").min(10).max(5);
    }
}