
package com.spaxxv.cardsmp.cards.classes;

import com.destroystokyo.paper.ParticleBuilder;
import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataBoolean;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

public class BreezeCard extends Card {
    private final String[] icons = new String[] {"\uE00D", "\uE00E", "\uE00F", "\uE010"};
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setAbilityData(new DataMap().set("can-use", new DataBoolean(true)))
            .setSlot(AbilitySlot.PASSIVE)
            .setTick(event -> {
                DataBoolean canUse = (DataBoolean) event.data.get("can-use");
                if (!canUse.value && ((Entity) event.player).isOnGround()) {
                    canUse.value = true;
                }
            });

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setActivate(event -> {
                float yaw = (float) Math.toRadians(event.player.getYaw());
                float pitch = (float) Math.toRadians(event.player.getPitch());
                Vector facing = new Vector(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
                event.player.setVelocity(facing.multiply(getConfigDouble("ability-1.speed")));
                CardSMP.instance.setupParticlesForEntity(event.player, Particle.POOF.builder()).extra(0).count(10).spawn();
            });

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(getConfigInt("ability-2.duration"))
            .setActivate(event -> {
                float yaw = (float) Math.toRadians(event.player.getYaw());
                float pitch = (float) Math.toRadians(event.player.getPitch());
                Vector facing = new Vector(-Math.sin(yaw) * Math.cos(pitch), 1, Math.cos(yaw) * Math.cos(pitch));
                event.player.setVelocity(facing.multiply(getConfigDouble("ability-2.speed")));
            })
            .setTick(event -> {
                if (event.remainingTime % 2 == 0) {
                    CardSMP.instance.setupParticlesForEntity(event.player, Particle.POOF.builder()).extra(0).count(1).spawn();
                }
                if (event.remainingTime + 2 < getConfigInt("ability-2.duration") && ((Entity) event.player).isOnGround() ) {
                    Location centralLocation = event.player.getLocation().clone();
                    Collection<Player> players = event.player.getWorld().getNearbyEntitiesByType(Player.class, centralLocation, getConfigDouble("ability-2.radius"));
                    for (Player player : players) {
                        if (!TrustedPlayer.hasTrust(event.player, player) && !player.equals(event.player)) {
                            Vector kbVector = player.getLocation().clone().subtract(centralLocation).toVector().setY(0).normalize().setY(0.5).multiply(getConfigDouble("ability-2.knockback"));
                            player.setVelocity(kbVector);
                        }
                    }
                    event.cancelAbility();
                }
            });


    @Override
    public String getName() {
        return "Breeze";
    }
    @Override
    public String getID() {
        return "breeze-card";
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
