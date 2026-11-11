
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.cards.Cards;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataBoolean;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class GhostCard extends Card {
    private final String[] icons = new String[] {"\uE019", "\uE01A", "\uE01B", "\uE01C"};
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE);

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setActivate(event -> {
                for (PotionEffect effect : event.player.getActivePotionEffects()) {
                    if (effect.getType() == PotionEffectType.INVISIBILITY) {
                        event.player.addPotionEffect(effect.withDuration(effect.getDuration() + Cards.GHOST.getConfigInt("ability-1.increase")));
                        CardSMP.instance.setupParticlesForEntity(event.player, Particle.SMOKE.builder()).extra(0).count(20).spawn();
                        return;
                    }
                }
                event.cancelCooldown();
                event.cancelAbility();
            });

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(getConfigInt("ability-2.duration"))
            .setAbilityData(new DataMap().set("had-flight", new DataBoolean()))
            .setActivate(event -> {
                Collection<Player> players = event.player.getWorld().getNearbyEntitiesByType(Player.class, event.player.getLocation(), getConfigDouble("ability-2.radius"));
                for (Player player : players) {
                    if (!TrustedPlayer.hasTrust(event.player, player) && !player.equals(event.player)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, getConfigInt("ability-2.effect-duration"), 0));
                    }
                }
                if (event.player.getAllowFlight()) {
                    ((DataBoolean) event.data.get("had-flight")).value = true;
                } else {
                    event.player.setAllowFlight(true);
                }
            })
            .setTick(event -> {
                if (event.remainingTime % 2 == 0) {
                    CardSMP.instance.setupParticlesForEntity(event.player, Particle.SMOKE.builder()).extra(0).count(1).spawn();
                }
            })
            .setEnd(event -> {
                if (!((DataBoolean) event.data.get("had-flight")).value) {
                    event.player.setAllowFlight(false);
                }
            });

    @Override
    public String getName() {
        return "Ghost";
    }
    @Override
    public String getID() {
        return "ghost-card";
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
