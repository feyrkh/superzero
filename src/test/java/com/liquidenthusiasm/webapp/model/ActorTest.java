package com.liquidenthusiasm.webapp.model;

import com.liquidenthusiasm.webapp.model.action.Action;
import com.liquidenthusiasm.webapp.model.action.ActionLogger;
import com.liquidenthusiasm.webapp.model.action.MotivatedAction;
import com.liquidenthusiasm.webapp.model.item.Item;
import com.liquidenthusiasm.webapp.model.motive.ConstantMotiveUpdate;
import com.liquidenthusiasm.webapp.model.motive.Motive;
import com.liquidenthusiasm.webapp.model.motive.MotiveUpdate;
import com.liquidenthusiasm.webapp.model.sandbox.BaseSandbox;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ActorTest extends BaseSandbox {
    private static final double EPSILON = 0.00001f;
    Motive hunger;
    Motive boredom;
    Actor actor;

    @Before
    public void setUp() throws Exception {
        actor = new Actor("bob");
        hunger = new Motive("mockHunger");
        boredom = new Motive("boredom");
    }

    @Test
    public void actorsStartWithNoMotives() {
        assertEquals(0, actor.getMotives().size());
    }

    @Test
    public void canRegisterMotive() {
        actor.registerMotiveUpdate(arbitraryMotive(), arbitraryMotiveUpdate());
        assertEquals(1, actor.getMotiveUpdates().size());
    }

    @Test
    public void updatingActorUpdatesAllMotives() {
        Motive m1 = arbitraryMotive();
        Motive m2 = arbitraryMotive();
        Motive m3 = arbitraryMotive();
        MotiveUpdate u1 = arbitraryMotiveUpdate();
        MotiveUpdate u2 = arbitraryMotiveUpdate();
        MotiveUpdate u3 = arbitraryMotiveUpdate();
        actor.registerMotiveUpdate(m1, u1);
        actor.registerMotiveUpdate(m2, u2);
        actor.registerMotiveUpdate(m3, u3);
        actor.update();

        assertEquals(3, actor.getMotiveUpdates().size());
        verify(u1).update(m1, actor);
        verify(u2).update(m2, actor);
        verify(u3).update(m3, actor);
    }

    @Test
    public void canChooseToEatIfHungry() {
        makeActorHungry(5);
        MotivatedAction eat = new MotivatedAction("eat").motive(hunger, -1);
        Action rest = new MotivatedAction("rest").motive(hunger, 0);
        Action starve = new MotivatedAction("fast").motive(hunger, 1);
        actor.addAction(eat);
        actor.addAction(rest);
        actor.addAction(starve);
        assertActionChain(eat, eat, eat, eat, eat, null, null, null);
        assertEquals(0, actor.getMotive(hunger), EPSILON);
    }

    @Test
    public void canNotPerformInvalidActions() {
        makeActorHungry(5);
        MotivatedAction eat = new MotivatedAction("eat").motive(hunger, -1);
        actor.addAction(eat);
        assertActionChain(eat, eat);
        eat.setValidityCheck((actor) -> false);
        assertActionChain(null, null);
        eat.setValidityCheck((actor) -> true);
        assertActionChain(eat, eat);

    }

    @Test
    public void willAlternateMotivesAsOthersAreSatisfied() {
        makeActorHungry(5.9);
        makeActorBored(5);
        MotivatedAction eat = new MotivatedAction("eat").motive(hunger, -1);
        Action rest = new MotivatedAction("rest").motive(boredom, -1);
        actor.addAction(eat);
        actor.addAction(rest);
        assertActionChain(eat, rest, eat, rest, eat, rest, eat, rest, eat, rest, eat, null, null);
    }

    @Test
    public void rationalActorsStillAlternatesBetweenEqualUtilities() {
        actor.registerDesire(hunger, 0, 0, 0);
        Action eatGrass = new MotivatedAction("eat grass").motive(hunger, -1);
        Action eatLeaves = new MotivatedAction("eat leaves").motive(hunger, -1);

        actor.addAction(eatGrass);
        actor.addAction(eatLeaves);
        actor.addMotive(hunger, 100);

        for (int i = 0; i < 100; i++) {
            processTurn(actor);
            actor.addMotive(hunger, 1);
        }
        long grassCount = ActionLogger.logger.actorActionCount(actor, "eat grass");
        long leavesCount = ActionLogger.logger.actorActionCount(actor, "eat leaves");
        System.out.println("Farmer at grass " + grassCount + " times, and leaves " + leavesCount + " times");
        long diff = Math.abs(grassCount - leavesCount);
        assertTrue("Diff is " + diff + ", expected < 25", diff < 25);
    }

    private void makeActorBored(double amount) {
        actor.addMotive(boredom, amount);
        actor.registerDesire(boredom, 0, 0, 0);
    }

    @Test
    public void canAssignMoreWeightToSomeMotives() {
        makeActorHungry(50);
        makeActorBored(50);
        actor.setMotiveWeight(hunger, 0.75);
        MotivatedAction eat = new MotivatedAction("eat").motive(hunger, -5);
        Action rest = new MotivatedAction("rest").motive(boredom, -5);
        actor.addAction(eat);
        actor.addAction(rest);
        assertActionChain(rest, rest, rest, rest, rest, eat, rest);
    }

    @Test
    public void motiveWeightEncouragesClosestToIdeal() {
        makeActorHungry(50);
        actor.setMotiveWeight(hunger, 5);
        MotivatedAction eatGrass = new MotivatedAction("eat grass").motive(hunger, -5);
        MotivatedAction eatSteak = new MotivatedAction("eat steak").motive(hunger, -50);
        actor.addAction(eatGrass);
        actor.addAction(eatSteak);
        assertActionChain(eatSteak, null);
    }

    @Test
    public void canReceiveItems() {
        Item item = mock(Item.class);
        actor.addItem(item);
        assertEquals(1, actor.getItems().size());
    }

    @Test
    public void canRemoveItems() {
        Item item = mock(Item.class);
        actor.addItem(item);
        actor.removeItem(item);
        assertEquals(0, actor.getItems().size());
    }

    @Test
    public void addingMultipleIdenticalItemsWithChargesCombinesTheCharge() {
        int charge1 = randomInt(50);
        int charge2 = randomInt(50);
        Item item1 = itemWithCharges("match", charge1);
        Item item2 = itemWithCharges("match", charge2);
        actor.addItem(item1);
        actor.addItem(item2);
        assertEquals("Expected " + charge1 + "+" + charge2 + " charges", charge1 + charge2, item1.getUses());
        assertEquals(1, actor.getItems().size());
    }


    @Test
    public void addingMultipleIdenticalItemsWithInfiniteChargesDoesNothing() {
        Item item1 = itemWithCharges("match", Item.INFINITE_USES);
        Item item2 = itemWithCharges("match", Item.INFINITE_USES);
        actor.addItem(item1);
        assertEquals("Expected infinite charges", Item.INFINITE_USES, item1.getUses());
        assertEquals(1, actor.getItems().size());
        actor.addItem(item2);
        assertEquals("Expected infinite charges", Item.INFINITE_USES, item1.getUses());
        assertEquals(1, actor.getItems().size());
    }

    @Test
    public void addingItemWithInfiniteChargesToNonInfiniteChargesMakesOriginalInfinite() {
        Item item1 = itemWithCharges("match", 1);
        Item item2 = itemWithCharges("match", Item.INFINITE_USES);
        actor.addItem(item1);
        assertEquals("Expected 1 charge", 1, item1.getUses());
        assertEquals(1, actor.getItems().size());
        actor.addItem(item2);
        assertEquals("Expected infinite charges", Item.INFINITE_USES, item1.getUses());
        assertEquals(1, actor.getItems().size());
    }

    @Test
    public void addingItemWithNonInfiniteChargesToInfiniteChargesDoesNothing() {
        Item item1 = itemWithCharges("match", Item.INFINITE_USES);
        Item item2 = itemWithCharges("match", 1);
        actor.addItem(item1);
        assertEquals("Expected 1 charge", Item.INFINITE_USES, item1.getUses());
        assertEquals(1, actor.getItems().size());
        actor.addItem(item2);
        assertEquals("Expected infinite charges", Item.INFINITE_USES, item1.getUses());
        assertEquals(1, actor.getItems().size());
    }

    @Test
    public void canConsiderActionsFromItems() {
        Item item = makeFood(1, 1);
        actor.addItem(item);
        assertEquals(1, actor.getItems().size());
        makeActorHungry(50);
        assertEquals("Expected actor to consider eating the food", "eat", actor.considerNextAction().getName());
    }

    @Test
    public void canPerformActionsFromItems() {
        Item item = makeFood(1, 1);
        actor.addItem(item);
        assertEquals(1, actor.getItems().size());
        makeActorHungry(50);
        actor.considerNextAction();
        assertEquals("Expected actor to actually eat the food", item.getActions(actor).iterator().next(), actor.performNextAction());
    }

    @Test
    public void usingAnItemActionUsesUpTheItem() {
        Item item = makeFood(5, 1);
        actor.addItem(item);
        assertEquals(1, actor.getItems().size());
        makeActorHungry(50);
        actor.considerNextAction();
        assertEquals("food uses left before eat", 5, item.getUses());
        actor.performNextAction();
        assertEquals("food uses left after", 4, item.getUses());
    }

    @Test
    public void runningOutOfUsesForItemRemovesItFromActor() {
        Item item = makeFood(1, 1);
        actor.addItem(item);
        assertEquals(1, actor.getItems().size());
        makeActorHungry(50);

        actor.considerNextAction();
        actor.performNextAction();
        assertEquals(0, actor.getItems().size());
    }

    private Item makeFood(int charges, int satiation) {
        Item retval = itemWithCharges("food", charges);
        retval.addAction(new MotivatedAction("eat").motive(hunger, -satiation));
        return retval;
    }

    private void makeActorHungry(double amount) {
        actor.addMotive(hunger, amount);
        actor.registerDesire(hunger, 0, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void motiveWeightNotNegative() {
        actor.setMotiveWeight(hunger, -0.000001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void motiveWeightMotiveMustBeNonNull() {
        actor.setMotiveWeight(null, 1);
    }

    @Test
    public void unregisteredMotiveWeightIsOne() {
        assertEquals(1, actor.getMotiveWeight(hunger), EPSILON);
    }

    @Test
    public void canHaveHungerAutoIncrease() {
        actor.registerMotiveUpdate(hunger, ConstantMotiveUpdate.increaseByOne);
        assertEquals(0, actor.getMotive(hunger), EPSILON);
        actor.update();
        assertEquals(1, actor.getMotive(hunger), EPSILON);
        actor.update();
        assertEquals(2, actor.getMotive(hunger), EPSILON);
    }

    @Test
    public void canAddToMotives() {
        actor.addMotive(hunger, 1);
        assertEquals(1, actor.getMotive(hunger), EPSILON);
        actor.addMotive(hunger, 3);
        assertEquals(4, actor.getMotive(hunger), EPSILON);
    }

    @Test
    public void canAddNegativeToMotives() {
        actor.addMotive(hunger, 1);
        assertEquals(1, actor.getMotive(hunger), EPSILON);
        actor.addMotive(hunger, -3);
        assertEquals(-2, actor.getMotive(hunger), EPSILON);
    }

    @Test
    public void motiveAddRespectsCeiling() {
        int ceiling = 10;
        int moreThanHalfCeiling = ceiling / 2 + 1;
        hunger.max(ceiling);
        actor.addMotive(hunger, moreThanHalfCeiling);
        assertEquals(moreThanHalfCeiling, actor.getMotive(hunger), EPSILON);
        actor.addMotive(hunger, moreThanHalfCeiling);
        assertEquals(ceiling, actor.getMotive(hunger), EPSILON);
        actor.addMotive(hunger, moreThanHalfCeiling);
        assertEquals(ceiling, actor.getMotive(hunger), EPSILON);
    }

    @Test
    public void motiveSubtractRespectsFloor() {
        int floor = -10;
        int moreThanHalfFloor = floor / 2 - 1;
        hunger.min(floor);
        actor.addMotive(hunger, moreThanHalfFloor);
        assertEquals(moreThanHalfFloor, actor.getMotive(hunger), EPSILON);
        actor.addMotive(hunger, moreThanHalfFloor);
        assertEquals(floor, actor.getMotive(hunger), EPSILON);
        actor.addMotive(hunger, moreThanHalfFloor);
        assertEquals(floor, actor.getMotive(hunger), EPSILON);
    }

    private void assertActionPerformed(Action action) {
        if (action == null) {
            assertNull("action considered", actor.considerNextAction());
            assertNull("action performed", actor.performNextAction());
        } else {
            assertEquals("action considered", getActionName(action), actor.considerNextAction().getName());
            assertEquals("action performed", getActionName(action), actor.performNextAction().getName());
        }
    }

    @Test
    public void canPerformTimeConsumingTasks() {
        makeActorHungry(1);
        MotivatedAction eat = new MotivatedAction("eat").motive(hunger, -1);
        eat.setCompletionTime(3); // 0: start eating, 1: continue eating, 2: eat
        actor.addAction(eat);
        assertActionPerformed(eat);
        assertEquals("starting to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("starting to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("continuing to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("continuing to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("finish eating - # eats", 1, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("finish eating - hunger", 0, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(null);
    }

    @Test
    public void timeConsumingTaskInterruptedIfActionNotOwnedAnyMore() {
        makeActorHungry(1);
        MotivatedAction eat = new MotivatedAction("eat").motive(hunger, -1);
        eat.setCompletionTime(3); // 0: start eating, 1: continue eating, 2: eat
        actor.addAction(eat);
        assertActionPerformed(eat);
        assertEquals("starting to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("starting to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("continuing to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("continuing to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        actor.removeAction(eat);
        assertActionPerformed(null);
        assertEquals("interrupted eating - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("interrupted eating - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(null);
    }

    @Test
    public void timeConsumingTaskInterruptedIfItemNotOwnedAnyMore() {
        makeActorHungry(1);
        Item food = makeFood(1, 1);
        Action eat = food.getAction("eat");
        eat.setCompletionTime(3); // 0: start eating, 1: continue eating, 2: eat
        actor.addItem(food);
        assertActionPerformed(eat);
        assertEquals("starting to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("starting to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("continuing to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("continuing to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        actor.removeItem(food);
        assertActionPerformed(null);
        assertEquals("interrupted eating - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("interrupted eating - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(null);
    }

    @Test
    public void timeConsumingTaskInterruptedIfActionNotValidAnyMore() {
        makeActorHungry(1);
        MotivatedAction eat = new MotivatedAction("eat").motive(hunger, -1);
        eat.setCompletionTime(3); // 0: start eating, 1: continue eating, 2: eat
        actor.addAction(eat);
        assertActionPerformed(eat);
        assertEquals("starting to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("starting to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("continuing to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("continuing to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        eat.setValidityCheck((actor) -> false);
        assertActionPerformed(null);
        assertEquals("interrupted eating - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("interrupted eating - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(null);
    }

    @Test
    public void multipleTimeConsumingTasksInARowWork() {
        makeActorHungry(2);
        MotivatedAction eat = new MotivatedAction("eat").motive(hunger, -1);
        eat.setCompletionTime(3); // 0: start eating, 1: continue eating, 2: eat
        actor.addAction(eat);
        assertActionPerformed(eat);
        assertEquals("starting to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("starting to eat - hunger", 2, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("continuing to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("continuing to eat - hunger", 2, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("finish eating - # eats", 1, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("finish eating - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("starting to eat 2 - # eats", 1, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("starting to eat 2 - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("continuing to eat 2 - # eats", 1, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("continuing to eat 2 - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("finish eating 2 - # eats", 2, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("finish eating 2 - hunger", 0, actor.getMotive(hunger), EPSILON);
    }


    @Test
    public void multipleTimeConsumingTasksInARowWorkAfterInterruption() {
        makeActorHungry(1);
        MotivatedAction eat = new MotivatedAction("eat").motive(hunger, -1);
        eat.setCompletionTime(3); // 0: start eating, 1: continue eating, 2: eat
        actor.addAction(eat);
        assertActionPerformed(eat);
        assertEquals("starting to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("starting to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("continuing to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("continuing to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        eat.setValidityCheck((actor) -> false);
        assertActionPerformed(null);
        assertEquals("interrupted eating - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("interrupted eating - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(null);

        eat.setValidityCheck(null);
        assertActionPerformed(eat);
        assertEquals("starting to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("starting to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("continuing to eat - # eats", 0, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("continuing to eat - hunger", 1, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(eat);
        assertEquals("finish eating - # eats", 1, ActionLogger.logger.actorActionCount(actor, "eat"));
        assertEquals("finish eating - hunger", 0, actor.getMotive(hunger), EPSILON);
        assertActionPerformed(null);
    }

    @Test
    public void timeConsumingTasksLessDesirableThanEquivalentInstantTasks() {
        makeActorHungry(100);
        MotivatedAction slowEat = new MotivatedAction("eat").motive(hunger, -1);
        slowEat.setCompletionTime(3); // 0: start eating, 1: continue eating, 2: eat
        MotivatedAction instantEat = new MotivatedAction("eat fast").motive(hunger, -1);
        instantEat.setCompletionTime(1);
        actor.addAction(slowEat);
        actor.addAction(instantEat);

        for (int i = 0; i < 100; i++) {
            assertEquals("attempt #" + i + ": expected to always use the faster option, but didn't", instantEat, processTurn(actor));
        }
        assertEquals("Should have eaten 100 times", 0, actor.getMotive(hunger), EPSILON);
    }

    @Test
    public void timeConsumingTasksLessDesirableThanEquivalentFasterTasks() {
        makeActorHungry(100);
        MotivatedAction slowEat = new MotivatedAction("eat").motive(hunger, -1);
        slowEat.setCompletionTime(3); // 0: start eating, 1: continue eating, 2: eat
        MotivatedAction lessSlowEat = new MotivatedAction("eat slightly faster").motive(hunger, -1);
        lessSlowEat.setCompletionTime(2);
        actor.addAction(slowEat);
        actor.addAction(lessSlowEat);

        for (int i = 0; i < 100; i++) {
            assertEquals("attempt #" + i + ": expected to always use the faster option, but didn't", lessSlowEat, processTurn(actor));
        }
        assertEquals("Should have eaten 50 times", 50, actor.getMotive(hunger), EPSILON);
    }

    @Test
    public void higherHappinessPerTimeUnitChosenWithPerfectForesight() {
        makeActorHungry(100);
        MotivatedAction feast = new MotivatedAction("feast").motive(hunger, -15);
        feast.setCompletionTime(10); // 0: start eating, 1: continue eating, 2: eat
        MotivatedAction snack = new MotivatedAction("snack").motive(hunger, -1);
        actor.addAction(snack);
        actor.addAction(feast);
        assertEquals(feast, processTurn(actor));
    }

    @Test
    public void timeConsumingTaskDesirabilityDecaysWithPessimisticForesight() {
        makeActorHungry(100);
        actor.setForesight(1.06);
        MotivatedAction feast = new MotivatedAction("feast").motive(hunger, -11);
        feast.setCompletionTime(10); // 0: start eating, 1: continue eating, 2: eat
        MotivatedAction snack = new MotivatedAction("snack").motive(hunger, -1);
        actor.addAction(snack);
        actor.addAction(feast);
        assertEquals(snack, processTurn(actor));
    }

    @Test
    public void lowerHappinessPerTimeUnitChosenWithOptimisticForesight() {
        makeActorHungry(100);
        actor.setForesight(0.92);
        MotivatedAction feast = new MotivatedAction("feast").motive(hunger, -9);
        feast.setCompletionTime(10); // 0: start eating, 1: continue eating, 2: eat
        MotivatedAction snack = new MotivatedAction("snack").motive(hunger, -1);
        actor.addAction(snack);
        actor.addAction(feast);
        assertEquals(feast, processTurn(actor));
    }

    @Test
    public void findMostOptimisticHalfQualityChoice() {
        makeActorHungry(100);
        MotivatedAction feast = new MotivatedAction("feast").motive(hunger, -10);
        feast.setCompletionTime(20);
        MotivatedAction snack = new MotivatedAction("snack").motive(hunger, -1);
        actor.addAction(snack);
        actor.addAction(feast);

        double foresight = 1;
        actor.setForesight(foresight);
        while (foresight > 0 && processTurn(actor) == snack) {
            makeActorHungry(100);
            foresight -= 0.01;
            actor.setForesight(foresight);
        }
        System.out.println("### Snack to feast hunger/time ratio: " + (snack.getExpectedMotiveFulfillment(actor).get(hunger) / snack.getCompletionTime()) + " vs " + (feast.getExpectedMotiveFulfillment(actor).get(hunger) / feast.getCompletionTime()) +
                "; choosing 'feast' if our foresight is at " + foresight + " or lower");
    }

    @Test
    public void findMostPessimisticDoubleQualityChoice() {
        makeActorHungry(100);
        MotivatedAction feast = new MotivatedAction("feast").motive(hunger, -10);
        feast.setCompletionTime(5);
        MotivatedAction snack = new MotivatedAction("snack").motive(hunger, -1);
        actor.addAction(snack);
        actor.addAction(feast);

        double foresight = 1;
        actor.setForesight(foresight);
        while (foresight < 3 && processTurn(actor) == feast) {
            makeActorHungry(100);
            foresight += 0.01;
            actor.setForesight(foresight);
        }
        System.out.println("### Snack to feast hunger/time ratio: " + (snack.getExpectedMotiveFulfillment(actor).get(hunger) / snack.getCompletionTime()) + " vs " + (feast.getExpectedMotiveFulfillment(actor).get(hunger) / feast.getCompletionTime()) +
                "; choosing 'feast' if our foresight is at " + foresight + " or higher");
    }


    private void assertActionChain(Action... chain) {
        String[] considered = new String[chain.length];
        String[] performed = new String[chain.length];
        int i = 0;
        String fmt = "%3s %15s %15s %15s\n";
        System.out.printf(fmt, "", "Expected", "Considered", "Performed");
        for (Action expected : chain) {
            considered[i] = getActionName(actor.considerNextAction());
            performed[i] = getActionName(actor.performNextAction());
            String chainName = getActionName(chain[i]);
            String consideredName = considered[i];
            String performedName = performed[i];
            System.out.printf(fmt, i, chainName, consideredName, performedName);
            i++;
        }
        for (int j = 0; j < chain.length; j++) {
            String chainName = getActionName(chain[j]);
            String consideredName = considered[j];
            String performedName = performed[j];
            assertEquals("Action chain failed on #" + j + ", didn't consider the expected action", chainName, consideredName);
            assertEquals("Action chain failed on #" + j + ", didn't perform the expected action", chainName, performedName);
        }
    }

    private int randomInt(int max) {
        return (int) (Math.random() * max);
    }

    private Item itemWithCharges(String name, int charges) {
        Item i = new Item(name);
        i.setUses(charges);
        return i;
    }

    private String getActionName(Action action) {
        if (action == null) return "(null)";
        if (action.getName() == null) return "(null)";
        return action.getName();
    }

    private MotiveUpdate arbitraryMotiveUpdate() {
        return mock(MotiveUpdate.class);
    }

    private Motive arbitraryMotive() {
        return mock(Motive.class);
    }
}