package com.spaxxv.cardsmp.cards.classes.event;

import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.EventCard;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BloodCard extends EventCard {
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.EVENTPASSIVE);

    private final Ability activeAbility = new Ability()
            .setSlot(AbilitySlot.EVENT)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(getConfigInt("ability-1.duration"))
            .setActivate(event -> event.player.sendPlainMessage("Activated Blood Card event ability!"))
            .setEnd(event -> event.player.sendPlainMessage("Blood Card event ability has ended."));

    @Override
    public String getName() {
        return "Blood";
    }
    @Override
    public String getID() {
        return "blood-card";
    }
    @Override
    public final Ability passive() {
        return passiveAbility;
    }
    @Override
    public final Ability ability1() {
        return activeAbility;
    }
    @Override
    public String iconCharFor(int tearState) {
        return "\uE031";
    }
}
