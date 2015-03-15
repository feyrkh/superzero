package com.liquidenthusiasm.webapp.model.action;

import com.liquidenthusiasm.webapp.model.Actor;
import com.liquidenthusiasm.webapp.model.motive.Motive;

import java.util.Map;

public interface Action {
    Map<Motive, Double> getExpectedMotiveFulfillment(Actor actor);

    Map<Motive, Double> getActualMotiveFulfillment(Actor actor);

    boolean isValid(Actor actor);

    void perform(Actor actor, ActionProvider provider);

    String getName();

    int getCompletionTime();

    void setCompletionTime(int i);
}
