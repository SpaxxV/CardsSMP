
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.data.values.DataSubMap;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class OceanCard extends Card {
    private final String[] icons = new String[] {"\uE029", "\uE02A", "\uE02B", "\uE02C"};
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE)
            .setTick(event -> event.player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 10, 0, true)));

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setAbilityData(new DataMap().set("affected-players", new DataSubMap()).set("player-hits", new DataSubMap()))
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(getConfigInt("ability-1.duration"))
            .setTick(event -> {
                if (event.remainingTime % 4 == 0) {
                    CardSMP.instance.setupParticlesForEntity(event.player, Particle.SPLASH.builder()).extra(0).count(1).spawn();
                }
                Collection<? extends Player> players = event.player.getServer().getOnlinePlayers();
                DataSubMap affected = (DataSubMap) event.data.get("affected-players");
                for (Player player : players) {
                    String uuid = player.getPlayerProfile().getId().toString();
                    if (affected.hasKey(uuid)) {
                        DataInt tick = (DataInt) affected.get(uuid);
                        tick.value++;
                        if (tick.value % 20 == 0) {
                            player.damage(1, DamageSource.builder(DamageType.DROWN).withDirectEntity(event.player).build());
                        }
                        if (tick.value == getConfigInt("ability-1.effect-duration")) affected.remove(uuid);
                    }
                }
            });

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setAbilityData(new DataMap().set("affected-players", new DataSubMap()))
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(getConfigInt("ability-2.duration"))
            .setActivate(event -> {
                Collection<Player> players = event.player.getWorld().getNearbyEntitiesByType(Player.class, event.player.getLocation(), getConfigDouble("ability-2.radius"));
                DataSubMap affected = (DataSubMap) event.data.get("affected-players");
                for (Player player : players) {
                    if (!TrustedPlayer.hasTrust(event.player, player) && !player.equals(event.player)) {
                        affected.set(player.getPlayerProfile().getId().toString(), new DataInt());
                        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, getConfigInt("ability-2.duration"), 2));
                        CardSMP.instance.setupParticlesForEntity(player, Particle.BUBBLE.builder()).extra(0).count(10).spawn();
                    }
                }
                CardSMP.instance.setupParticlesForEntity(event.player, Particle.SPLASH.builder()).extra(0).count(20).spawn();
            })
            .setTick(event -> {
                Collection<? extends Player> players = event.player.getServer().getOnlinePlayers();
                DataSubMap affected = (DataSubMap) event.data.get("affected-players");
                for (Player player : players) {
                    String uuid = player.getPlayerProfile().getId().toString();
                    if (affected.hasKey(uuid)) {
                        DataInt tick = (DataInt) affected.get(uuid);
                        tick.value++;
                        if (tick.value % 20 == 0) {
                            player.damage(1, DamageSource.builder(DamageType.DROWN).withDirectEntity(event.player).build());
                        }
                    }
                }
            });

    @Override
    public String getName() {
        return "Ocean";
    }
    @Override
    public String getID() {
        return "ocean-card";
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
