package com.liquidenthusiasm.webapp.model.action;

import com.liquidenthusiasm.webapp.model.Actor;

public interface ActionValidityCheck {
    boolean isValid(Actor actor);
}
