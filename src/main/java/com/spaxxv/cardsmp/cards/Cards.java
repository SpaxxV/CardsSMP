package com.spaxxv.cardsmp.cards;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.cards.classes.*;
import com.spaxxv.cardsmp.cards.classes.event.BloodCard;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Cards {
    public static Card OCEAN;
    public static Card SKY;
    public static Card BREEZE;
    public static Card GHOST;
    public static Card BOUNTY;
    public static Card CONDUCTOR;
    public static Card FREEZE;
    public static Card ALCHEMIST;
    public static Card INVIS;
    public static Card MEDIC;
    public static Card INFERNO;
    public static Card BOOST;
    public static EventCard BLOOD;

    public static final Map<String, Card> cards = new HashMap<>();
    public static final Map<String, EventCard> eventCards = new HashMap<>();
    public static Card getCard(String id) {
        return cards.get(id);
    }
    public static Card getRandomCard(Random rng) {
        Set<String> keySet = cards.keySet();
        String[] keys = keySet.toArray(new String[0]);
        return getCard(keys[rng.nextInt(keySet.size())]);
    }
    public static String[] getCardIds() {
        return cards.keySet().toArray(new String[0]);
    }
    public static Card registerCard(Card card) {
        cards.put(card.getID(), card);
        CardSMP.instance.getLogger().info("Registered card " + card.getID());
        return card;
    }
    public static EventCard getEventCard(String id) {
        return eventCards.get(id);
    }
    public static EventCard registerEventCard(EventCard card) {
        eventCards.put(card.getID(), card);
        CardSMP.instance.getLogger().info("Registered event card " + card.getID());
        return card;
    }
    public static void registerCards() {
        OCEAN = registerCard(new OceanCard());
        SKY = registerCard(new SkyCard());
        BREEZE = registerCard(new BreezeCard());
        GHOST = registerCard(new GhostCard());
        BOUNTY = registerCard(new BountyCard());
        CONDUCTOR = registerCard(new ConductorCard());
        FREEZE = registerCard(new FreezeCard());
        ALCHEMIST = registerCard(new AlchemistCard());
        INVIS = registerCard(new InvisCard());
        MEDIC = registerCard(new MedicCard());
        INFERNO = registerCard(new InfernoCard());
        BOOST = registerCard(new BoostCard());

        BLOOD = registerEventCard(new BloodCard());
    }

}
