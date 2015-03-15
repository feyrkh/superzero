package com.liquidenthusiasm.webapp.model.motive;

import com.liquidenthusiasm.webapp.model.Actor;

import java.util.Map;

public interface MotiveFulfiller {
    Map<Motive, Double> getExpectedMotiveFulfillment(Actor actor);

    Map<Motive, Double> getActualMotiveFulfillment(Actor actor);
}
