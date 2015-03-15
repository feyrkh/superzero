package com.liquidenthusiasm.webapp.model.sandbox;

import com.liquidenthusiasm.webapp.model.Actor;
import com.liquidenthusiasm.webapp.model.action.ActionLogger;
import com.liquidenthusiasm.webapp.model.action.ItemProvidingAction;
import com.liquidenthusiasm.webapp.model.item.Item;
import com.liquidenthusiasm.webapp.model.motive.Motive;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FarmerJoe extends BaseSandbox {
    private Actor farmer;
    private Item grass, corn, steak, hoe;
    private ItemProvidingAction gatherGrass;
    private ItemProvidingAction buyHoe;
    private ItemProvidingAction farmWithHoe;

    @Before
    public void setup() {
        farmer = createPerson("Farmer Joe");

        grass = createFood("grass", 1, 1);
        corn = createFood("corn", 1, 5);
        steak = createFood("steak", 1, 20);
        hoe = createItem("hoe");
        farmWithHoe = new ItemProvidingAction("farms corn with").providesItem(corn);
        hoe.addAction(farmWithHoe);
        gatherGrass = new ItemProvidingAction("gathers grass").providesItem(grass);
        buyHoe = new ItemProvidingAction("buys a hoe").providesItem(hoe);
        assertNull("Expected logger to have been cleared", ActionLogger.logger.actionsBy(farmer));
    }

    @Test
    public void hungryFarmerEatsGrassRatherThanStarve() {
        farmer.addItem(steak);
        farmer.addAction(gatherGrass);
        farmer.addMotive(hunger, 21);

        assertEquals("farmer item count before eat", 1, farmer.getItems().size());
        assertEquals(steak.getAction("eats"), processTurn(farmer));
        assertEquals("farmer item count after eat", 0, farmer.getItems().size());

        // People get +1 hunger per turn, so the farmer will never get full if it takes 2 turns to gather and eat grass
        for (int i = 0; i < 100; i++) {
            assertEquals("turn #" + i + " with no grass", gatherGrass, processTurn(farmer));
            assertEquals("turn #" + i + " with grass", grass.getAction("eats"), processTurn(farmer));
        }
    }

    @Test
    public void hungryFarmerBuysAHoeAndFarmsRatherThanEatGrass() {
        farmer.addAction(gatherGrass);
        farmer.addAction(buyHoe);
        farmer.addMotive(hunger, 21);

        assertEquals(buyHoe, processTurn(farmer));
        assertEquals(farmWithHoe, processTurn(farmer));
        assertEquals(corn.getAction("eats"), processTurn(farmer));
        assertEquals(farmWithHoe, processTurn(farmer));
        assertEquals(corn.getAction("eats"), processTurn(farmer));
    }

    @Test
    public void healthConsciousFarmerAvoidsJunkFood() {
        Motive sugar = new Motive("sugar");
        farmer.registerDesire(sugar, 0, 0, 0);
        farmer.setMotiveWeight(sugar, 2);
        Item pie = createFancyFood("pie", 10, 11, (eatAction) -> eatAction.motive(sugar, 10));
        Item steak = createFancyFood("steak", 10, 10, (eatAction) -> eatAction.motive(sugar, 0));
        farmer.addItem(pie);
        farmer.addItem(steak);
        farmer.addMotive(hunger, 100);

        for (int i = 0; i < 10; i++) {
            assertEquals(steak.getAction("eats"), processTurn(farmer));
        }

    }
}
