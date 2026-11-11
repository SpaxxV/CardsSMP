
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilityManager;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.cards.Cards;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;

import java.util.Collection;

public class AlchemistCard extends Card {
    private final String[] icons = new String[] {"\uE001", "\uE002", "\uE003", "\uE004"};
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE);

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(0)
            .setActivate(event -> {
                Collection<Player> players = event.player.getWorld().getNearbyEntitiesByType(Player.class, event.player.getLocation(), getConfigDouble("ability-1.radius"));
                for (Player player : players) {
                    if (TrustedPlayer.hasTrust(event.player, player) || player.equals(event.player)) {
                        for (PotionEffect effect : player.getActivePotionEffects()) {
                            if (effect.getType().getCategory() == PotionEffectTypeCategory.BENEFICIAL || effect.getType() == PotionEffectType.WEAVING) {
//                                if (effect.getType() == PotionEffectType.INVISIBILITY && AbilityManager.isActive(player, Cards.GHOST.passive())) {
//                                    player.getPersistentDataContainer().set(CardSMP.instance.getResourceKey("ghost_increase_bypass"), PersistentDataType.BOOLEAN, true);
//                                }
                                player.addPotionEffect(effect.withDuration(effect.getDuration() + getConfigInt("ability-1.increase")));
                            }
                        }
                        CardSMP.instance.setupParticlesForEntity(player, Particle.GLOW.builder()).extra(0).count(10).spawn();
                    }
                }
            });

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(getConfigInt("ability-2.duration"))
            .setActivate(event -> {
                for (PotionEffect effect : event.player.getActivePotionEffects()) {
                    if (effect.getType().getCategory() == PotionEffectTypeCategory.BENEFICIAL) {
                        event.player.addPotionEffect(effect.withAmplifier(effect.getAmplifier() + 1));
                    }
                }
                CardSMP.instance.setupParticlesForEntity(event.player, Particle.GLOW.builder()).extra(0).count(10).spawn();
            })
            .setEnd(event -> {
                for (PotionEffect effect : event.player.getActivePotionEffects()) {
                    if (effect.getType().getCategory() == PotionEffectTypeCategory.BENEFICIAL) {
                        event.player.removePotionEffect(effect.getType());
                        event.player.addPotionEffect(effect.withAmplifier(effect.getAmplifier() - 1));
                    }
                }
            });

    @Override
    public String getName() {
        return "Alchemist";
    }
    @Override
    public String getID() {
        return "alchemist-card";
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
