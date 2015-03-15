package com.liquidenthusiasm.webapp.model.action;

import com.liquidenthusiasm.webapp.model.Actor;
import com.liquidenthusiasm.webapp.model.motive.Motive;
import com.liquidenthusiasm.webapp.model.motive.MotiveFulfiller;

import java.util.HashMap;
import java.util.Map;

public class MotivatedAction implements Action, MotiveFulfiller {
    private final String name;
    protected Map<Motive, Double> motiveChanges = new HashMap<>();
    private ActionValidityCheck validityCheck = null;
    private int completionTime = 1;

    public MotivatedAction(String name) {
        this.name = name;
    }

    public MotivatedAction(String name, ActionValidityCheck validityCheck) {
        this.name = name;
        this.validityCheck = validityCheck;
    }

    /**
     * When this action is performed, the corresponding motive is changed by this amount
     *
     * @param motive
     * @param change
     */
    public MotivatedAction motive(Motive motive, double change) {
        motiveChanges.put(motive, change);
        return this;
    }

    @Override
    public Map<Motive, Double> getExpectedMotiveFulfillment(Actor actor) {
        return motiveChanges;
    }

    @Override
    public Map<Motive, Double> getActualMotiveFulfillment(Actor actor) {
        return motiveChanges;
    }


    @Override
    public boolean isValid(Actor actor) {
        if (validityCheck != null) {
            return validityCheck.isValid(actor);
        }
        return true;
    }

    @Override
    public void perform(Actor actor, ActionProvider provider) {
        for (Map.Entry<Motive, Double> entry : getActualMotiveFulfillment(actor).entrySet()) {
            actor.addMotive(entry.getKey(), entry.getValue());
        }
        logAction(actor, provider);
    }

    protected void logAction(Actor actor, ActionProvider provider) {
        ActionLogger.logger.log(actor, this, provider);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Action{" +
                "'" + name + '\'' +
                '}';
    }

    public ActionValidityCheck getValidityCheck() {
        return validityCheck;
    }

    public void setValidityCheck(ActionValidityCheck validityCheck) {
        this.validityCheck = validityCheck;
    }

    public void setCompletionTime(int completionTime) {
        if (completionTime < 1) completionTime = 1;
        this.completionTime = completionTime;
    }

    @Override
    public int getCompletionTime() {
        return completionTime;
    }
}
