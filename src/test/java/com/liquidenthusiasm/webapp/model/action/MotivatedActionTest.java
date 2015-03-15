package com.liquidenthusiasm.webapp.model.action;

import com.liquidenthusiasm.webapp.model.Actor;
import com.liquidenthusiasm.webapp.model.motive.Motive;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class MotivatedActionTest {
    public static final float EPSILON = 0.000001f;
    MotivatedAction action;
    Motive motive;
    private double motiveChange;
    private Actor actor;

    @Before
    public void setUp() throws Exception {
        action = new MotivatedAction("action");
        motive = new Motive("motive");
        motiveChange = Math.random() * 200 - 100;
        actor = mock(Actor.class);
    }

    @Test
    public void canApplyMotive() {
        assertEquals("fluent interface", action, action.motive(motive, motiveChange));
        assertEquals("number of motive changes", 1, action.getExpectedMotiveFulfillment(actor).size());
        assertEquals("motive change amt", motiveChange, action.getExpectedMotiveFulfillment(actor).get(motive), EPSILON);
    }

    @Test
    public void canTestForValidityRejection() {
        action.setValidityCheck((actor) -> false);
        assertFalse(action.isValid(actor));
    }

    @Test
    public void canTestForValidityAcceptance() {
        action.setValidityCheck((actor) -> true);
        assertTrue(action.isValid(actor));
    }

    @Test
    public void testForValidityWithNoCheckSetIsTrue() {
        assertTrue(action.isValid(actor));
    }

    @Test
    public void actionsCanTakeTimeToComplete() {
        int randomTime = (int) (Math.random() * 500);
        action.setCompletionTime(randomTime);
        assertEquals(randomTime, action.getCompletionTime());
    }

    @Test
    public void actionsCanNotTakeNonPositiveTime() {
        int randomTime = (int) (Math.random() * -500) - 1;
        action.setCompletionTime(randomTime);
        assertEquals(1, action.getCompletionTime());
    }

    @Test
    public void defaultActionTimeIs1() {
        assertEquals(1, action.getCompletionTime());
    }
}