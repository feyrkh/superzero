package com.liquidenthusiasm.webapp.model.action;

import com.liquidenthusiasm.webapp.model.Actor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActionLoggerTest {
    Actor a1 = new Actor("a1");
    Actor a2 = new Actor("a2");
    Action eat = new MotivatedAction("eat");
    Action sleep = new MotivatedAction("sleep");

    @Before
    public void setUp() throws Exception {
        ActionLogger.logger = new ActionLogger();
    }

    @Test
    public void canLogStuff() {
        ActionLogger.logger.log(a1, eat);
        ActionLogger.logger.log(a1, eat);
        ActionLogger.logger.log(a1, eat);
        ActionLogger.logger.log(a1, sleep);
        ActionLogger.logger.log(a1, eat);
        ActionLogger.logger.log(a1, sleep);

        ActionLogger.logger.log(a2, eat);
        ActionLogger.logger.log(a2, eat);
        ActionLogger.logger.log(a2, eat);
        ActionLogger.logger.log(a2, sleep);
        ActionLogger.logger.log(a2, sleep);
        ActionLogger.logger.log(a2, sleep);

        assertEquals("total eats", 7, (long) ActionLogger.logger.actions.get(eat.getName()));
        assertEquals("total sleeps", 5, (long) ActionLogger.logger.actions.get(sleep.getName()));
        assertEquals("a1 eats", 4, (long) ActionLogger.logger.actorActions.get(a1.getName()).get(eat.getName()));
        assertEquals("a1 sleep", 2, (long) ActionLogger.logger.actorActions.get(a1.getName()).get(sleep.getName()));
        assertEquals("a2 eats", 3, (long) ActionLogger.logger.actorActions.get(a2.getName()).get(eat.getName()));
        assertEquals("a2 sleep", 3, (long) ActionLogger.logger.actorActions.get(a2.getName()).get(sleep.getName()));

        System.out.println(ActionLogger.logger.actionReport());
        System.out.println(ActionLogger.logger.actorReport());
    }

    @Test
    public void canGetActionsByActor() {
        ActionLogger.logger.log(a1, eat);
        ActionLogger.logger.log(a1, eat);
        ActionLogger.logger.log(a1, sleep);
        ActionLogger.logger.log(a2, sleep);
        ActionLogger.logger.log(a2, sleep);
        ActionLogger.logger.log(a2, sleep);
        assertEquals("eat", 2, (long) ActionLogger.logger.actionsBy(a1).get("eat"));
        assertEquals("sleep a1", 1, (long) ActionLogger.logger.actionsBy(a1).get("sleep"));
        assertEquals("sleep a2", 3, (long) ActionLogger.logger.actionsBy(a2).get("sleep"));
    }

    @Test
    public void nonexistentActionsReturn0() {
        ActionLogger.logger.log(a1, eat);
        assertEquals("burp", 1, (long) ActionLogger.logger.actorActionCount(a1, "eat"));
        assertEquals("burp", 0, (long) ActionLogger.logger.actorActionCount(a1, "sleep"));
        assertEquals("burp", 0, (long) ActionLogger.logger.actorActionCount(a1, "burp"));
        assertEquals("burp", 0, (long) ActionLogger.logger.actorActionCount(a2, "burp"));
    }
}