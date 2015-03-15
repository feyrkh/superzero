package com.liquidenthusiasm.webapp.model.action;

import com.liquidenthusiasm.webapp.model.Actor;
import com.liquidenthusiasm.webapp.model.item.Item;
import com.liquidenthusiasm.webapp.model.motive.Motive;

import java.util.HashMap;
import java.util.Map;

public class ItemProvidingAction extends MotivatedAction {
    public static final double PREVIOUSLY_OWNED_ITEM_ADJUSTMENT = 0.5d;
    private Item providedItem;

    public ItemProvidingAction(String name) {
        super(name);
    }

    public ItemProvidingAction(String name, ActionValidityCheck validityCheck) {
        super(name, validityCheck);
    }

    public ItemProvidingAction providesItem(Item providedItem) {
        this.providedItem = providedItem;
        return this;
    }

    public Item getProvidedItem() {
        return providedItem;
    }

    @Override
    public void perform(Actor actor, ActionProvider provider) {
        actor.addItem(providedItem.copy());
        super.perform(actor, provider);
    }

    @Override
    public boolean isValid(Actor actor) {
        if (getProvidedItem().getUses() == Item.INFINITE_USES && actor.getItems().contains(getProvidedItem())) {
            return false;
        }
        return super.isValid(actor);
    }

    @Override
    public Map<Motive, Double> getExpectedMotiveFulfillment(Actor actor) {
        if (actor.getItems().contains(providedItem)) {
            return getAlreadyOwnedExpectedMotiveFulfillment(actor);
        }
        return providedItem.getExpectedMotiveFulfillment(actor);
    }

    private Map<Motive, Double> getAlreadyOwnedExpectedMotiveFulfillment(Actor actor) {
        HashMap<Motive, Double> retval = new HashMap<>(providedItem.getExpectedMotiveFulfillment(actor));
        for (Map.Entry<Motive, Double> entry : retval.entrySet()) {
            entry.setValue(entry.getValue() * PREVIOUSLY_OWNED_ITEM_ADJUSTMENT);
        }
        return retval;
    }

    @Override
    protected void logAction(Actor actor, ActionProvider provider) {
        if (providedItem != null) {
            ActionLogger.logger.log(actor, this, provider, String.format("; gets %s", getProvidedItem().getName()));
        } else {
            super.logAction(actor, provider);
        }
    }

}
