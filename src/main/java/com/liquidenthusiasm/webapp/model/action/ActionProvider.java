package com.liquidenthusiasm.webapp.model.action;

import com.liquidenthusiasm.webapp.model.Actor;

import java.util.List;

public interface ActionProvider {
    List<Action> getActions(Actor actor);

    void actionPerformed(Actor actor, Action action);

    String getName();
}
