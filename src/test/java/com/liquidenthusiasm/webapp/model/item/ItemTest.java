package com.liquidenthusiasm.webapp.model.item;

import com.liquidenthusiasm.webapp.model.Actor;
import com.liquidenthusiasm.webapp.model.action.Action;
import com.liquidenthusiasm.webapp.model.action.MotivatedAction;
import com.liquidenthusiasm.webapp.model.motive.Motive;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class ItemTest {

    private static final double EPSILON = 0.000001f;
    Item item;
    private Actor actor;
    private Action action;

    @Before
    public void setUp() throws Exception {
        item = new Item("match");
        actor = mock(Actor.class);
        action = mock(MotivatedAction.class);
    }

    @Test
    public void itemCanHaveName() {
        Item item = new Item("match");
        assertEquals("match", item.getName());
    }

    @Test
    public void itemCanProvideActions() {
        item.addAction(new MotivatedAction("light"));
        assertEquals(1, item.getActions(actor).size());
    }

    @Test
    public void invalidActionsAreFilteredOut() {
        item.addAction(new MotivatedAction("light"));
        item.addAction(new MotivatedAction("drink", (actor) -> false));
        item.addAction(new MotivatedAction("eat", (actor) -> true));
        assertEquals(2, item.getActions(actor).size());
    }

    @Test
    public void itemCanBeUsedUp() {
        item.setUses(1);
        assertFalse(item.isUsedUp());
        item.actionPerformed(actor, action);
        assertTrue(item.isUsedUp());
    }

    @Test
    public void tryingToUseAUsedUpItemDoesNotMakeItInfinite() {
        item.setUses(1);
        assertFalse(item.isUsedUp());
        item.actionPerformed(actor, action);
        item.actionPerformed(actor, action);
        assertEquals(0, item.getUses());
    }

    @Test
    public void nullActorDoesNotMakeItemExplode() {
        item.setUses(1);
        assertFalse(item.isUsedUp());
        item.actionPerformed(null, action);
        assertEquals(0, item.getUses());
    }

    @Test
    public void itemCanHaveInfiniteUses() {
        item.setUses(Item.INFINITE_USES);
        assertFalse(item.isUsedUp());
        item.actionPerformed(actor, action);
        assertFalse(item.isUsedUp());
        assertEquals(Item.INFINITE_USES, item.getUses());
    }

    @Test
    public void itemsWithSameNameAreIdentical() {
        Item i1 = new Item("match");
        Item i2 = new Item("match");
        Item i3 = new Item("shoe");
        i1.setUses(1);
        i2.setUses(500);
        i3.setUses(10000);
        assertEquals("i1 vs i2, same name", i1.hashCode(), i2.hashCode());
        assertNotEquals("i1 vs i3, diff name", i1.hashCode(), i3.hashCode());
    }

    @Test
    public void canDuplicateChargedItem() {
        int chargeCount = (int) (Math.random() * 5) + 1;
        item.setUses(chargeCount);
        Item copy = item.copy();
        assertNotSame("Copied item with charges should not be same", item, copy);
        assertEquals("copy charges", chargeCount, copy.getUses());
        assertEquals("orig charges", chargeCount, item.getUses());
        copy.actionPerformed(actor, action);
        assertEquals("copy charges after copy use", chargeCount - 1, copy.getUses());
        assertEquals("orig charges after copy use", chargeCount, item.getUses());
    }

    @Test
    public void duplicatingInfiniteItemDoesNotCopy() {
        int chargeCount = Item.INFINITE_USES;
        item.setUses(chargeCount);
        Item copy = item.copy();
        assertSame("Copied item with charges should be same", item, copy);
    }

    @Test
    public void canGetActionByName() {
        Item food = new Item("food");
        Action eat = new MotivatedAction("eats");
        Action compost = new MotivatedAction("composts");
        Action play = new MotivatedAction("plays");
        food.addAction(eat);
        food.addAction(compost);
        food.addAction(play);
        assertEquals("eat", eat, food.getAction("eats"));
        assertEquals("compost", compost, food.getAction("composts"));
        assertEquals("play", play, food.getAction("plays"));
        assertEquals("null", null, food.getAction("null"));
    }

    @Test
    public void noActionItemUtilityIsEmpty() {
        Item worthless = new Item("junk");
        assertEquals(0, worthless.getExpectedMotiveFulfillment(actor).size());
        assertEquals("Actual motive fulfillment is always 0", 0, worthless.getActualMotiveFulfillment(actor).size());
    }

    @Test
    public void singleActionItemUtilityIsSameAsAction() {
        Item food = new Item("food");
        Motive calories = new Motive("calories");
        int satiation = 10;
        int calorieCount = 1000;
        Action eat = new MotivatedAction("eats").motive(Motive.hunger, satiation).motive(calories, calorieCount);
        food.addAction(eat);
        assertEquals(2, food.getExpectedMotiveFulfillment(actor).size());
        assertEquals(satiation, food.getExpectedMotiveFulfillment(actor).get(Motive.hunger), EPSILON);
        assertEquals(calorieCount, food.getExpectedMotiveFulfillment(actor).get(calories), EPSILON);
        assertEquals("Actual motive fulfillment is always 0", 0, food.getActualMotiveFulfillment(actor).size());
    }

    @Test
    public void multiActionItemUtilityIsEqualToMainAction() {
        Item food = new Item("food");
        Motive calories = new Motive("calories");
        int satiation = 10;
        int calorieCount = 1000;
        Action eat = new MotivatedAction("eats").motive(Motive.hunger, satiation).motive(calories, calorieCount);
        Action benchpress = new MotivatedAction("benchpresses").motive(calories, -1);
        food.addAction(eat);
        food.addAction(benchpress);
        assertEquals(2, food.getExpectedMotiveFulfillment(actor).size());
        assertEquals(satiation, food.getExpectedMotiveFulfillment(actor).get(Motive.hunger), EPSILON);
        assertEquals(calorieCount, food.getExpectedMotiveFulfillment(actor).get(calories), EPSILON);
        assertEquals("Actual motive fulfillment is always 0", 0, food.getActualMotiveFulfillment(actor).size());
    }
}