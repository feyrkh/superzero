package com.liquidenthusiasm.webapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.liquidenthusiasm.webapp.model.action.Action;
import com.liquidenthusiasm.webapp.model.action.ActionProvider;
import com.liquidenthusiasm.webapp.model.item.Item;
import com.liquidenthusiasm.webapp.model.motive.Desire;
import com.liquidenthusiasm.webapp.model.motive.Motive;
import com.liquidenthusiasm.webapp.model.motive.MotiveUpdate;

import java.util.*;

public class Actor implements ActionProvider {
    private String name;
    private Map<Motive, Double> motives = new HashMap<>();
    private Map<Motive, MotiveUpdate> motiveUpdates = new HashMap<>();
    private Map<Motive, Desire> desires = new HashMap<>();
    private List<Action> actions = new ArrayList<>();
    @JsonIgnore
    private Map<Motive, Double> motiveWeights = new HashMap<>();
    private Set<Item> items = new HashSet<>();
    private BestActionAccumulator nextAction = new BestActionAccumulator(this);
    private double foresight = 1;

    public Actor(String name) {
        this.name = name;
    }

    public void addMotive(Motive motive, double amount) {
        double newAmt;
        if (!motives.containsKey(motive)) {
            newAmt = amount;
        } else {
            newAmt = motives.get(motive) + amount;
        }
        if (newAmt > motive.getMax()) {
            newAmt = motive.getMax();
        } else if (newAmt < motive.getMin()) {
            newAmt = motive.getMin();
        }
        motives.put(motive, newAmt);
    }

    public Map<Motive, Double> getMotives() {
        return motives;
    }

    public double getMotive(Motive motive) {
        if (!motives.containsKey(motive)) {
            return 0;
        }
        return motives.get(motive);
    }

    public void registerMotiveUpdate(Motive motive, MotiveUpdate motiveUpdate) {
        motiveUpdates.put(motive, motiveUpdate);
    }

    public void update() {
        for (Map.Entry<Motive, MotiveUpdate> entry : motiveUpdates.entrySet()) {
            entry.getValue().update(entry.getKey(), this);
        }
    }

    public Map<Motive, MotiveUpdate> getMotiveUpdates() {
        return motiveUpdates;
    }

    public void registerDesire(Motive motive, int min, int ideal, int max) {
        desires.put(motive, new Desire(motive, min, ideal, max));
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public Action considerNextAction() {
        nextAction.verifyLongActionStillValid();
        nextAction.consider(this);
        for (Item item : items) {
            nextAction.consider(item);
        }

        return nextAction.getAction();
    }

    private double calculateUtility(Action action) {
        double utility = 0d;
        Map<Motive, Double> motiveChanges = action.getExpectedMotiveFulfillment(this);
        double timeFactor = Math.pow((action.getCompletionTime() - 1), foresight) + 1;
        System.out.printf(String.format("Time factor for action %s: %.5f%n", action.getName(), timeFactor));
        for (Map.Entry<Motive, Double> entry : motiveChanges.entrySet()) {
            Motive motive = entry.getKey();
            Double change = entry.getValue();
            Desire desire = desires.get(motive);
            if (desire == null) {
                continue;
            }
            double originalVal = getMotive(motive);
            double newVal = originalVal + change;
            int ideal = desire.getIdeal();

            double origHappiness = -Math.abs(originalVal - ideal) - addWeightToOutOfBoundsDesires(desire, originalVal);
            double newHappiness = -Math.abs(newVal - ideal) - addWeightToOutOfBoundsDesires(desire, newVal);
            double thisMotiveUtility = ((newHappiness - origHappiness) * getMotiveWeight(motive)) / timeFactor;
            System.out.printf("(%15s): %3.0f + %3.0f; utility=%.1f\n", motive.getName(), getMotive(motive), change, thisMotiveUtility);
            utility += thisMotiveUtility;
        }
        return utility;
    }

    private double addWeightToOutOfBoundsDesires(Desire desire, double originalVal) {
        double retval = 0;
        int minDesire = desire.getMin();
        if (originalVal < minDesire) {
            retval = (minDesire - originalVal);
        } else {
            int maxDesire = desire.getMax();
            if (originalVal > maxDesire) {
                retval = (originalVal - maxDesire);
            }
        }
        return Math.pow(retval, 1.5);
    }

    public Action performNextAction() {
        if (nextAction != null) {
            nextAction.perform();
        }
        return nextAction.getAction();
    }

    public String getName() {
        return name;
    }

    public void setMotiveWeight(Motive motive, double weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Motive weights must be non-negative, but got " + weight + " for motive " + motive);
        }
        if (motive == null) {
            throw new IllegalArgumentException("Motive for a motive weight must be non-null");
        }
        motiveWeights.put(motive, weight);
    }

    public Double getMotiveWeight(Motive motive) {
        Double weight = motiveWeights.get(motive);
        if (weight == null) {
            return 1d;
        }
        return weight;
    }

    public void addItem(Item item) {
        if (items.contains(item)) {
            for (Item item1 : items) {
                if (item1.equals(item)) {

                    if (item.getUses() == Item.INFINITE_USES || item1.getUses() == Item.INFINITE_USES) {
                        item1.setUses(Item.INFINITE_USES);
                    } else {
                        item1.setUses(item1.getUses() + item.getUses());
                    }
                    break;
                }
            }
        } else {
            items.add(item);
        }
    }

    public Set<Item> getItems() {
        return items;
    }

    @Override
    public List<Action> getActions(Actor actor) {
        return actions;
    }

    @Override
    public void actionPerformed(Actor actor, Action action) {
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public void removeAction(Action action) {
        actions.remove(action);
    }

    public void setForesight(double foresight) {
        this.foresight = foresight;
    }

    public double getForesight() {
        return foresight;
    }

    private class BestActionAccumulator implements Action {
        private final Actor owner;
        private Action action;
        private ActionProvider provider;
        private double utility;
        private int timer = 0;

        public BestActionAccumulator(Actor owner) {
            this.owner = owner;
        }

        public void perform() {
            if (timer == 0) {
                if (action != null) {
                    this.timer = this.action.getCompletionTime();
                }
            }
            if (timer > 1) {
                timer--;
                return;
            }
            if (action != null) {
                action.perform(owner, provider);
                if (provider != null) {
                    provider.actionPerformed(owner, action);
                }
            }
            reset();
        }

        public Action getAction() {
            return action;
        }

        private void reset() {
            utility = 0;
            timer = 0;
        }

        public void consider(ActionProvider provider) {
            if (timer > 0) {
                return;
            }
            if (utility == 0) {
                this.action = null;
                this.provider = null;
            }
            for (Action a : provider.getActions(owner)) {
                if (!a.isValid(owner)) continue;
                double curUtil = calculateUtility(a) - Math.random() * 0.01; // The Math.random() is a fudge factor to help alternate between options of equal utility
                if (curUtil > utility) {
                    this.utility = curUtil;
                    this.action = a;
                    this.provider = provider;
                }
            }
        }

        @Override
        public Map<Motive, Double> getExpectedMotiveFulfillment(Actor actor) {
            if (action == null) {
                return Collections.EMPTY_MAP;
            }
            return action.getExpectedMotiveFulfillment(owner);
        }

        @Override
        public Map<Motive, Double> getActualMotiveFulfillment(Actor actor) {
            return getExpectedMotiveFulfillment(actor);
        }

        @Override
        public boolean isValid(Actor actor) {
            if (action == null) {
                return false;
            }
            return action.isValid(actor);
        }

        @Override
        public void perform(Actor actor, ActionProvider provider) {
            if (action == null) {
                return;
            }
            if (!this.owner.equals(actor)) {
                throw new IllegalArgumentException(String.format("An actor (%s) tried to perform an action on behalf of another actor (%s) - that's not legal", this, actor));
            }
            this.perform();
        }

        @Override
        public String getName() {
            if (action == null) {
                return null;
            }
            return action.getName();
        }

        @Override
        public int getCompletionTime() {
            return action.getCompletionTime();
        }

        @Override
        public void setCompletionTime(int i) {
            action.setCompletionTime(i);
        }

        @Override
        public String toString() {
            if (action == null) {
                return "null";
            }
            return action.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BestActionAccumulator)) {
                if (o instanceof Action) {
                    return o.equals(this.action);
                }
                return false;
            }

            BestActionAccumulator that = (BestActionAccumulator) o;

            if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return owner != null ? owner.hashCode() : 0;
        }

        public void verifyLongActionStillValid() {
            if (action == null || timer <= 0) return;
            boolean bad = false;
            bad = bad || !action.isValid(owner);
            if (provider != null && Item.class.isInstance(provider)) {
                bad = bad || !owner.items.contains(provider);
            }
            if (provider != null) {
                bad = bad || !provider.getActions(owner).contains(action);
            }

            if (bad) {
                reset();
            }
        }
    }
}
