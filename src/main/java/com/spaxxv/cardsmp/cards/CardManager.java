package com.spaxxv.cardsmp.cards;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.AbilityInstance;
import com.spaxxv.cardsmp.abilities.AbilityManager;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.data.values.DataString;
import com.spaxxv.cardsmp.data.values.DataSubMap;
import org.bukkit.entity.Player;

public class CardManager {
    public static void setCardData(Player player, CardInstance cardInstance) {
        DataSubMap playerData = CardSMP.getPlayerData(player);
        playerData.set("card", new DataSubMap()
                .set("id", new DataString(cardInstance.card.getID()))
                .set("tear", new DataInt(cardInstance.tearLevel)));
    }
    public static void setEventCardData(Player player, CardInstance cardInstance) {
        DataSubMap playerData = CardSMP.getPlayerData(player);
        playerData.set("event-card", new DataSubMap()
                .set("id", new DataString(cardInstance.card.getID())));
    }
    public static DataSubMap getCardData(Player player) {
        DataSubMap playerData = CardSMP.getPlayerData(player);
        if (playerData.hasKey("card")) {
            return (DataSubMap) playerData.get("card");
        }
        return null;
    }
    public static DataSubMap getEventCardData(Player player) {
        DataSubMap playerData = CardSMP.getPlayerData(player);
        if (playerData.hasKey("event-card")) {
            return (DataSubMap) playerData.get("event-card");
        }
        return null;
    }
    public static boolean removeCardData(Player player) {
        DataSubMap playerData = CardSMP.getPlayerData(player);
        if (playerData.hasKey("card")) {
            playerData.remove("card");
            AbilityManager.endPlayerAbilities(player, new AbilitySlot[] { AbilitySlot.PASSIVE, AbilitySlot.ACTIVE1, AbilitySlot.ACTIVE2 });
            return true;
        }
        return false;
    }
    public static boolean removeEventCardData(Player player) {
        DataSubMap playerData = CardSMP.getPlayerData(player);
        if (playerData.hasKey("event-card")) {
            playerData.remove("event-card");
            AbilityManager.endPlayerAbilities(player, new AbilitySlot[] { AbilitySlot.EVENTPASSIVE, AbilitySlot.EVENT });
            return true;
        }
        return false;
    }
    public static CardInstance getCard(Player player) {
        DataSubMap cardData = getCardData(player);
        if (cardData != null) {
            DataString cardID = (DataString) cardData.get("id");
            DataInt cardTear = (DataInt) cardData.get("tear");
            return new CardInstance(Cards.getCard(cardID.value), cardTear.value);
        }
        return null;
    }
    public static CardInstance getEventCard(Player player) {
        DataSubMap cardData = getEventCardData(player);
        if (cardData != null) {
            DataString cardID = (DataString) cardData.get("id");
            return new CardInstance(Cards.getEventCard(cardID.value), 0);
        }
        return null;
    }
    public static boolean hasCard(Player player) {
        DataSubMap playerData = CardSMP.getPlayerData(player);
        return playerData.hasKey("card");
    }
    public static boolean hasEventCard(Player player) {
        DataSubMap playerData = CardSMP.getPlayerData(player);
        return playerData.hasKey("event-card");
    }
    public static boolean equipCard(Player player, CardInstance cardInstance) {
        return equipCard(player, cardInstance, false);
    }
    public static boolean equipCard(Player player, CardInstance cardInstance, boolean force) {
        if (cardInstance.card instanceof EventCard) {
            boolean hasEventCard = hasEventCard(player);
            if (hasEventCard && !force) {
                return false;
            } else {
                if (hasEventCard) removeEventCardData(player);
                setEventCardData(player, cardInstance);
                activateEventPassive(player);
                return true;
            }
        } else {
            boolean hasCard = hasCard(player);
            if (hasCard && !force) {
                return false;
            } else {
                if (hasCard) removeCardData(player);
                setCardData(player, cardInstance);
                activatePassive(player);
                return true;
            }
        }
    }
    public static void activatePassive(Player player) {
        CardInstance cardInstance = CardManager.getCard(player);
        if (cardInstance != null && cardInstance.tearLevel != 5) AbilityManager.activateAbility(new AbilityInstance(cardInstance.card.passive(), player));
    }
    public static void activateEventPassive(Player player) {
        CardInstance eventCardInstance = CardManager.getEventCard(player);
        if (eventCardInstance != null) AbilityManager.activateAbility(new AbilityInstance(eventCardInstance.card.passive(), player));
    }
}
