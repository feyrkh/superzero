package com.liquidenthusiasm.webapp.model.motive;

import java.lang.reflect.Field;

public class Motive {
    private String name;
    private int min = -100;
    private int max = 100;

    public Motive() {
    }

    public Motive(String name) {
        this.name = name;
    }

    public static final Motive hunger = new Motive();


    public String getName() {
        return name;
    }

    static {
        for (Field field : Motive.class.getDeclaredFields()) {
            if (field.getType().equals(Motive.class)) {
                try {
                    ((Motive) field.get(Motive.class)).name = field.getName();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public Motive min(int i) {
        if (i > max) {
            throw new IllegalArgumentException("May not set 'min' higher than 'max'");
        }
        this.min = i;
        return this;
    }

    public Motive max(int i) {
        if (i < min) {
            throw new IllegalArgumentException("May not set 'max' lower than 'min'");
        }
        this.max = i;
        return this;
    }

    @Override
    public String toString() {
        return "Motive{" +
                "'" + name + '\'' +
                '}';
    }
}
