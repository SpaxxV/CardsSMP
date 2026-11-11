package com.spaxxv.cardsmp.cards;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;

public abstract class Card {
    public abstract String getName();
    public abstract String getID();
    public abstract Ability passive();
    public abstract Ability ability1();
    public abstract Ability ability2();
    public abstract String iconCharFor(int tearState);
    protected String getConfigLoc(String path) {
        return "cardsmp.cards." + getID() + "." + path;
    }
    public int getConfigInt(String path) {
        return CardSMP.config.getInt(getConfigLoc(path));
    }
    public double getConfigDouble(String path) {
        return CardSMP.config.getDouble(getConfigLoc(path));
    }
    public boolean getConfigBool(String path) {
        return CardSMP.config.getBoolean(getConfigLoc(path));
    }
}
