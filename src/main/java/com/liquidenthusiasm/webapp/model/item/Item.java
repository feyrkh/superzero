package com.liquidenthusiasm.webapp.model.item;

import com.liquidenthusiasm.webapp.model.Actor;
import com.liquidenthusiasm.webapp.model.action.Action;
import com.liquidenthusiasm.webapp.model.action.ActionProvider;
import com.liquidenthusiasm.webapp.model.motive.Motive;
import com.liquidenthusiasm.webapp.model.motive.MotiveFulfiller;

import java.util.*;
import java.util.stream.Collectors;

public class Item implements ActionProvider, MotiveFulfiller {
    public static final int INFINITE_USES = -1;

    private final String name;
    private List<Action> actions = new ArrayList<>();
    private int uses = INFINITE_USES;

    public Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }

    @Override
    public List<Action> getActions(Actor actor) {
        return actions.stream().filter((action) -> action.isValid(actor)).collect(Collectors.toList());
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public int getUses() {
        return uses;
    }

    @Override
    public void actionPerformed(Actor actor, Action action) {
        if (uses > 0) {
            subtractUses(actor, 1);
        }
    }

    private void subtractUses(Actor actor, int i) {
        if (uses != INFINITE_USES && uses >= i) {
            uses -= i;
            if (actor != null && uses == 0) {
                actor.removeItem(this);
            }
        }
    }

    public boolean isUsedUp() {
        return uses == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (!name.equals(item.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public Item copy() {
        if (this.uses == INFINITE_USES) {
            return this;
        }
        Item retval = new Item(this.getName());
        retval.actions = this.actions;
        retval.uses = this.uses;
        return retval;
    }

    public Action getAction(String actionName) {
        for (Action action : actions) {
            if (Objects.equals(action.getName(), actionName)) {
                return action;
            }
        }
        return null;
    }

    @Override
    public Map<Motive, Double> getExpectedMotiveFulfillment(Actor actor) {
        if (actions.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        return actions.get(0).getExpectedMotiveFulfillment(actor);
    }

    @Override
    public Map<Motive, Double> getActualMotiveFulfillment(Actor actor) {
        return Collections.EMPTY_MAP;
    }
}
