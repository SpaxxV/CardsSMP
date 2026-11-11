
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.data.values.DataSubMap;
import com.spaxxv.cardsmp.events.AbilityEventListener;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.Collection;

public class MedicCard extends Card {
    private final String[] icons = new String[] {"\uE025", "\uE026", "\uE027", "\uE028"};
    private final AbilityEventListener ability1Heal = event -> {
        double heal = getConfigDouble("ability-1.heal");
        Collection<Player> players = event.player.getWorld().getNearbyEntitiesByType(Player.class, event.player.getLocation(), getConfigDouble("ability-1.radius"));
        for (Player player : players) {
            boolean isSelf = player.equals(event.player);
            if (TrustedPlayer.hasTrust(event.player, player) || isSelf) {
                if (isSelf) {
                    event.player.heal(heal / getConfigDouble("passive.multiplier"));
                } else {
                    player.heal(heal);
                }
                CardSMP.instance.setupParticlesForEntity(player, Particle.HEART.builder()).extra(0).count(10).spawn();
            }
        }
    };
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE);

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(getConfigInt("ability-1.duration"))
            .setActivate(ability1Heal)
            .setEnd(ability1Heal);

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(getConfigInt("ability-2.duration"))
            .setAbilityData(new DataMap().set("affected-players", new DataSubMap()).set("player-hits", new DataSubMap()).set("gain", new DataInt()))
            .setEnd(event -> {
                Collection<? extends Player> players = event.player.getServer().getOnlinePlayers();
                DataSubMap affected = (DataSubMap) event.data.get("affected-players");
                for (Player player : players) {
                    String uuid = player.getPlayerProfile().getId().toString();
                    if (affected.hasKey(uuid)) {
                        if (player.getAttribute(Attribute.MAX_HEALTH) == null) player.registerAttribute(Attribute.MAX_HEALTH);
                        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                        if (maxHealth.getModifier(CardSMP.instance.getResourceKey("medic-stolen")) != null) {
                            maxHealth.removeModifier(CardSMP.instance.getResourceKey("medic-stolen"));
                        }
                    }
                }
                if (event.player.getAttribute(Attribute.MAX_HEALTH) == null) event.player.registerAttribute(Attribute.MAX_HEALTH);
                AttributeInstance maxHealth = event.player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth.getModifier(CardSMP.instance.getResourceKey("medic-gained")) != null) {
                    maxHealth.removeModifier(CardSMP.instance.getResourceKey("medic-gained"));
                }
                if (event.player.getHealth() > maxHealth.getValue()) event.player.setHealth(maxHealth.getValue());
            });

    @Override
    public String getName() {
        return "Medic";
    }
    @Override
    public String getID() {
        return "medic-card";
    }
    @Override
    public final Ability passive() {
        return passiveAbility;
    }
    @Override
    public final Ability ability1() {
        return activeAbility1;
    }
    @Override
    public final Ability ability2() {
        return activeAbility2;
    }
    @Override
    public String iconCharFor(int tearState) {
        return icons[tearState];
    }
}
