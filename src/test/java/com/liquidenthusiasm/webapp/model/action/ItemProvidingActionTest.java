package com.liquidenthusiasm.webapp.model.action;

import com.liquidenthusiasm.webapp.model.Actor;
import com.liquidenthusiasm.webapp.model.item.Item;
import com.liquidenthusiasm.webapp.model.motive.Motive;
import com.liquidenthusiasm.webapp.model.sandbox.BaseSandbox;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

public class ItemProvidingActionTest extends BaseSandbox {
    ItemProvidingAction action;
    Actor actor;
    Item item;
    Item duplicateItem;
    private java.util.Map<Motive, Double> itemExpectedMotives;
    private java.util.Map<Motive, Double> itemActualMotives;

    @Before
    public void setUp() throws Exception {
        actor = mock(Actor.class);
        action = new ItemProvidingAction("farm");
        item = mock(Item.class);
        duplicateItem = mock(Item.class);
        itemExpectedMotives = new HashMap<>();
        itemExpectedMotives.put(Motive.hunger, -100d);
        itemActualMotives = new HashMap<>();
        itemActualMotives.put(Motive.hunger, -100d);
        when(item.copy()).thenReturn(duplicateItem);
        when(item.getExpectedMotiveFulfillment(any(Actor.class))).thenReturn(itemExpectedMotives);
        when(item.getActualMotiveFulfillment(any(Actor.class))).thenReturn(itemActualMotives);
        action.providesItem(item);
    }

    @Test
    public void canProvideItemToActor() {
        action.perform(actor, actor);
        verify(item).copy();
        verify(actor).addItem(duplicateItem);
    }

    @Test
    public void actionExpectedUtilityEqualToItemUtility() {
        assertEquals(item.getExpectedMotiveFulfillment(actor), action.getExpectedMotiveFulfillment(actor));
    }

    @Test
    public void itemActualUtilityIsNotSameAsExpected() {
        assertNotEquals(item.getActualMotiveFulfillment(actor), action.getActualMotiveFulfillment(actor));
    }

    @Test
    public void nullActualUtilityReturnsEmptyMap() {
        assertEquals(0, action.getActualMotiveFulfillment(actor).size());
    }

    @Test
    public void actualUtilityCanBeReturned() {
        double randomVal = Math.random() * 200 - 100;
        Motive greed = new Motive("greed");
        action.motive(greed, randomVal);
        assertEquals(randomVal, action.getActualMotiveFulfillment(actor).get(greed), EPSILON);
    }

    @Test
    public void itemExpectedUtilityIsDecreasedWhenAlreadyOwned() {
        assertEquals("before getting one", -100d, action.getExpectedMotiveFulfillment(actor).get(Motive.hunger), EPSILON);
        when(actor.getItems()).thenReturn(Sets.newSet(item));
        assertEquals("after getting one", -100d * ItemProvidingAction.PREVIOUSLY_OWNED_ITEM_ADJUSTMENT, action.getExpectedMotiveFulfillment(actor).get(Motive.hunger), EPSILON);
    }

}