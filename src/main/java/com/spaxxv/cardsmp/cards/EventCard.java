package com.spaxxv.cardsmp.cards;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import org.bukkit.entity.Player;

public abstract class EventCard extends Card {
    private final Ability event2 = new Ability().setSlot(AbilitySlot.EVENT);
    @Override
    public final Ability ability2() {
        return event2;
    }
    protected String getConfigLoc(String path) {
        return "cardsmp.event-cards." + getID() + "." + path;
    }
}
