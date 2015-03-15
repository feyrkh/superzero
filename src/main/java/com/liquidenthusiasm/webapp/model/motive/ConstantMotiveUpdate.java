package com.liquidenthusiasm.webapp.model.motive;

import com.liquidenthusiasm.webapp.model.Actor;

public class ConstantMotiveUpdate implements MotiveUpdate {
    private final double amount;

    public static final ConstantMotiveUpdate increaseByOne = new ConstantMotiveUpdate(1);
    public static final ConstantMotiveUpdate decreaseByOne = new ConstantMotiveUpdate(-1);

    public ConstantMotiveUpdate(double amount) {
        this.amount = amount;
    }

    @Override
    public void update(Motive motive, Actor actor) {
        actor.addMotive(motive, amount);
    }
}
