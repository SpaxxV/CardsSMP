package com.spaxxv.cardsmp.abilities;

import com.spaxxv.cardsmp.cards.CardInstance;
import org.bukkit.entity.Player;

public class CardAbilityInstance extends AbilityInstance {
    private static final int[] tearMultipliers = new int[] {1, 1, 2, 3, 3};
    public CardInstance cardInstance;
    public CardAbilityInstance(Ability a, Player p, CardInstance card) {
        super(a, p);
        cardInstance = card;
    }
    @Override
    public int getCooldown() {
        return super.getCooldown() * tearMultipliers[cardInstance.tearLevel];
    }
}
