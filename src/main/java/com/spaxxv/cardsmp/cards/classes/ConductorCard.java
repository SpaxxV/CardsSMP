
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataBoolean;
import com.spaxxv.cardsmp.data.values.DataFloat;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.data.values.DataSubMap;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;

public class ConductorCard extends Card {
    private final String[] icons = new String[] {"\uE011", "\uE012", "\uE013", "\uE014"};
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE)
            .setAbilityData(new DataMap().set("raining", new DataBoolean()))
            .setTick(event -> {
                if (event.player.getWorld().isClearWeather()) {
                    DataBoolean raining = (DataBoolean) event.data.get("raining");
                    if (raining.value) {
                        event.player.removePotionEffect(PotionEffectType.SPEED);
                        raining.value = false;
                    }
                    event.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 0, true));
                } else {
                    ((DataBoolean) event.data.get("raining")).value = true;
                    event.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 1, true));
                }
            });

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(getConfigInt("ability-1.duration"))
            .setAbilityData(new DataMap().set("player-hits", new DataSubMap()))
            .setTick(event -> {
                if (event.remainingTime % 2 == 0) {
                    CardSMP.instance.setupParticlesForEntity(event.player, Particle.ELECTRIC_SPARK.builder()).extra(0).count(1).spawn();
                }
            });

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(getConfigInt("ability-2.duration"))
            .setAbilityData(new DataMap().set("stage", new DataInt()).set("launch-vector", new DataSubMap()))
            .setActivate(event -> {
                if (event.player.getPitch() < -45) {
                    float yaw = (float) Math.toRadians(event.player.getYaw());
                    float pitch = (float) Math.toRadians(event.player.getPitch());
                    DataSubMap launchVec = (DataSubMap) event.data.get("launch-vector");
                    launchVec.set("x", new DataFloat((float) (-Math.sin(yaw) * Math.cos(pitch)))).set("y", new DataFloat((float) (-Math.sin(pitch)))).set("z", new DataFloat((float) (Math.cos(yaw) * Math.cos(pitch))));
                    ItemStack trident = new ItemStack(Material.TRIDENT);
                    trident.addEnchantment(Enchantment.RIPTIDE, 2);
                    event.player.startRiptideAttack(getConfigInt("ability-2.riptide-duration"), 2, trident);
                } else {
                    event.cancelAbility();
                    event.cancelCooldown();
                }
            })
            .setTrigger(event -> {
                DataInt stage = (DataInt) event.data.get("stage");
                if (stage.value == 0 && event.player.getPitch() > 45) {
                    stage.value = 1;
                    event.remainingTime = (getConfigInt("ability-2.riptide-duration") * 3);
                    float yaw = (float) Math.toRadians(event.player.getYaw());
                    float pitch = (float) Math.toRadians(event.player.getPitch());
                    DataSubMap launchVec = (DataSubMap) event.data.get("launch-vector");
                    launchVec.set("x", new DataFloat((float) (-Math.sin(yaw) * Math.cos(pitch) * 2))).set("y", new DataFloat((float) (-Math.sin(pitch) * 2))).set("z", new DataFloat((float) (Math.cos(yaw) * Math.cos(pitch) * 2)));
                    ItemStack trident = new ItemStack(Material.TRIDENT);
                    trident.addEnchantment(Enchantment.RIPTIDE, 2);
                    event.player.startRiptideAttack((getConfigInt("ability-2.riptide-duration") * 2), 2, trident);
                }
            })
            .setTick(event -> {
                DataInt stage = (DataInt) event.data.get("stage");
                if (event.remainingTime + 2 < getConfigInt("ability-2.duration") && ((Entity) event.player).isOnGround()) {
                    if (stage.value == 0 || !event.player.isRiptiding()) {
                        event.cancelAbility();
                    } else {
                        stage.value = 2;
                        event.cancelAbility();
                    }
                }
                if (event.player.isRiptiding()) {
                    DataSubMap launchVec = (DataSubMap) event.data.get("launch-vector");
                    Vector vector = new Vector(((DataFloat) launchVec.get("x")).value, ((DataFloat) launchVec.get("y")).value, ((DataFloat) launchVec.get("z")).value).multiply(getConfigDouble("ability-2.speed"));
                    event.player.setVelocity(vector);
                }
                if (event.remainingTime % 2 == 0) {
                    CardSMP.instance.setupParticlesForEntity(event.player, Particle.ELECTRIC_SPARK.builder()).extra(0).count(1).spawn();
                }
            })
            .setEnd(event -> {
                DataInt stage = (DataInt) event.data.get("stage");
                if (stage.value == 2) {
                    Location endLocation = event.player.getLocation();
                    Collection<Player> players = event.player.getWorld().getNearbyEntitiesByType(Player.class, endLocation, getConfigDouble("ability-2.radius"));
                    for (Player player : players) {
                        if (!TrustedPlayer.hasTrust(event.player, player) && !player.equals(event.player)) {
                            player.damage(getConfigDouble("ability-2.aoe-damage"), DamageSource.builder(DamageType.LIGHTNING_BOLT).withDirectEntity(event.player).build());
                        }
                    }
                    for (int i = 0; i < getConfigInt("ability-2.bolts"); i++) {
                        float angle = (float) Math.toRadians(360.0f / i);
                        Vector edgePoint = new Vector(-Math.sin(angle), 0, Math.cos(angle)).multiply(getConfigDouble("ability-2.radius"));
                        Location boltLocation = endLocation.clone().add(edgePoint).toHighestLocation().toCenterLocation();
                        boltLocation.setY(boltLocation.getBlockY());
                        boltLocation.getWorld().strikeLightningEffect(boltLocation);
                    }
                }
                event.player.setRiptiding(false);
            });

    @Override
    public String getName() {
        return "Conductor";
    }
    @Override
    public String getID() {
        return "conductor-card";
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
