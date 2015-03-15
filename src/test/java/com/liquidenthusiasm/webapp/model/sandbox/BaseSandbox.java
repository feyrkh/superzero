package com.liquidenthusiasm.webapp.model.sandbox;

import com.liquidenthusiasm.webapp.model.Actor;
import com.liquidenthusiasm.webapp.model.action.Action;
import com.liquidenthusiasm.webapp.model.action.ActionLogger;
import com.liquidenthusiasm.webapp.model.action.MotivatedAction;
import com.liquidenthusiasm.webapp.model.item.Item;
import com.liquidenthusiasm.webapp.model.motive.ConstantMotiveUpdate;
import com.liquidenthusiasm.webapp.model.motive.Motive;
import com.liquidenthusiasm.webapp.model.motive.MotiveUpdate;
import org.junit.Before;

import java.util.function.Function;

public class BaseSandbox {
    protected static final double EPSILON = 0.0000001d;
    Motive hunger = new Motive("hunger").min(0).max(100);
    MotiveUpdate hungerUpdate = ConstantMotiveUpdate.increaseByOne;
    Action eat = new MotivatedAction("eat");

    @Before
    public void clearLog() {
        ActionLogger.logger.clear();
    }

    protected Action processTurn(Actor actor) {
        actor.update();
        actor.considerNextAction();
        return actor.performNextAction();
    }

    protected Actor createPerson(String name) {
        Actor actor = new Actor(name);
        actor.registerMotiveUpdate(hunger, hungerUpdate);
        actor.registerDesire(hunger, 0, 0, 0);
        actor.setMotiveWeight(hunger, 2); // Persons who are hungry really want to eat!
        return actor;
    }

    protected Item createItem(String name) {
        Item i = new Item(name);
        i.setUses(Item.INFINITE_USES);
        return i;
    }

    protected Item createItemWithCharges(String name, int charges) {
        Item i = new Item(name);
        i.setUses(charges);
        return i;
    }

    protected Item createFood(String name, int charges, int satiation) {
        Item retval = createItemWithCharges(name, charges);
        retval.addAction(new MotivatedAction("eats").motive(hunger, -satiation));
        return retval;
    }

    protected Item createFancyFood(String name, int charges, int satiation, Function<MotivatedAction, MotivatedAction> fn) {
        Item food = createFood(name, charges, satiation);
        fn.apply((MotivatedAction) food.getAction("eats"));
        return food;
    }

}
