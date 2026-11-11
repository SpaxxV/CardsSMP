
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.events.AbilityEventListener;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;

public class InfernoCard extends Card {
    private final String[] icons = new String[] {"\uE01D", "\uE01E", "\uE01F", "\uE020"};
    private final AbilityEventListener shootFireball = event -> {
        DataInt fireballs = (DataInt) event.data.get("fireballs");
        fireballs.value--;
        float yaw = (float) Math.toRadians(event.player.getYaw());
        float pitch = (float) Math.toRadians(event.player.getPitch());
        Vector facing = new Vector(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
        Fireball fireball = event.player.launchProjectile(Fireball.class, facing.multiply(getConfigDouble("ability-2.speed")));
        fireball.setYield(getConfigInt("ability-2.explosion-power"));
        fireball.setIsIncendiary(getConfigBool("ability-2.incendiary"));
        if (fireballs.value == 0) event.cancelAbility();
    };
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE)
            .setTick(event -> event.player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 10, 0, true)));

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(0)
            .setActivate(event -> {
                Collection<Player> players = event.player.getWorld().getNearbyEntitiesByType(Player.class, event.player.getLocation(), getConfigDouble("ability-1.radius"));
                for (Player player : players) {
                    if (!TrustedPlayer.hasTrust(event.player, player) && !player.equals(event.player)) {
                        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
                    }
                }
            });

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(getConfigInt("ability-2.duration"))
            .setAbilityData(new DataMap().set("fireballs", new DataInt(getConfigInt("ability-2.fireballs"))))
            .setActivate(shootFireball)
            .setTrigger(shootFireball);

    @Override
    public String getName() {
        return "Inferno";
    }
    @Override
    public String getID() {
        return "inferno-card";
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
