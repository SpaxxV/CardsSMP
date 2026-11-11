
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.events.AbilityEventListener;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;

public class InvisCard extends Card {
    private final String[] icons = new String[] {"\uE021", "\uE022", "\uE023", "\uE024"};
    private final AbilityEventListener dashEvent = event -> {
        DataInt remainingDashes = (DataInt) event.data.get("dashes");
        if (remainingDashes.value > 0) {
            remainingDashes.value--;
            event.remainingTime = 200;
            float yaw = (float) Math.toRadians(event.player.getYaw());
            float pitch = (float) Math.toRadians(event.player.getPitch());
            Vector facing = new Vector(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
            event.player.teleport(event.player.getLocation().add(facing.multiply(getConfigDouble("ability-1.distance"))));
            DataInt invisTicks = (DataInt) event.data.get("invis-ticks");
            invisTicks.value = getConfigInt("ability-1.effect-duration");
            Collection<? extends Player> players = event.player.getServer().getOnlinePlayers();
            for (Player player : players) {
                player.hidePlayer(CardSMP.instance, event.player);
            }
            event.player.setVisibleByDefault(false);
        }
    };
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE)
            .setTick(event -> {
                if (event.player.isSneaking()) {
                    event.player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10, 0, false));
                }
            });

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(200)
            .setAbilityData(new DataMap().set("dashes", new DataInt(getConfigInt("ability-1.dashes"))).set("invis-ticks", new DataInt()))
            .setActivate(dashEvent)
            .setTrigger(dashEvent)
            .setTick(event -> {
                DataInt remainingDashes = (DataInt) event.data.get("dashes");
                DataInt invisTicks = (DataInt) event.data.get("invis-ticks");
                if (invisTicks.value > 0) {
                    invisTicks.value--;
                } else if (!event.player.isVisibleByDefault()) {
                    Collection<? extends Player> players = event.player.getServer().getOnlinePlayers();
                    for (Player player : players) {
                        player.showPlayer(CardSMP.instance, event.player);
                    }
                    event.player.setVisibleByDefault(true);
                    if (remainingDashes.value == 0) event.cancelAbility();
                }
            }).setEnd(event -> {
                Collection<? extends Player> players = event.player.getServer().getOnlinePlayers();
                for (Player player : players) {
                    player.showPlayer(CardSMP.instance, event.player);
                }
                event.player.setVisibleByDefault(true);
            });

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(getConfigInt("ability-2.duration"))
            .setActivate(event -> {
                Collection<? extends Player> players = event.player.getServer().getOnlinePlayers();
                for (Player player : players) {
                    player.hidePlayer(CardSMP.instance, event.player);
                }
                event.player.setVisibleByDefault(false);
            })
            .setEnd(event -> {
                Collection<? extends Player> players = event.player.getServer().getOnlinePlayers();
                for (Player player : players) {
                    player.showPlayer(CardSMP.instance, event.player);
                }
                event.player.setVisibleByDefault(true);
            });

    @Override
    public String getName() {
        return "Invisibility";
    }
    @Override
    public String getID() {
        return "invis-card";
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
