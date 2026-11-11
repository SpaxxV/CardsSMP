
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.events.AbilityEvent;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import com.spaxxv.cardsmp.util.CustomInventory;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class BoostCard extends Card {
    private final String[] icons = new String[] {"\uE005", "\uE006", "\uE007", "\uE008"};
    public final NamespacedKey inventoryKey = CardSMP.instance.getResourceKey("boost-inventory");
    public final NamespacedKey boostTickKey = CardSMP.instance.getResourceKey("boost-boosted");
    public final NamespacedKey damageBoostKey = CardSMP.instance.getResourceKey("boost-damage");
    private ItemStack[] getBoostInventory(PersistentDataContainer pdc) {
        if (pdc.has(inventoryKey, PersistentDataType.BYTE_ARRAY)) {
            return ItemStack.deserializeItemsFromBytes(pdc.get(inventoryKey, PersistentDataType.BYTE_ARRAY));
        }
        ItemStack[] items = new ItemStack[9];
        saveToBoostInventory(pdc, items);
        return items;
    }
    public void saveToBoostInventory(PersistentDataContainer pdc, ItemStack[] items) {
        pdc.set(inventoryKey, PersistentDataType.BYTE_ARRAY, ItemStack.serializeItemsAsBytes(items));
    }
    public int calculateSpeed(int level) {
        int min = getConfigInt("ability-2.min-speed");
        int max = getConfigInt("ability-2.max-speed");
        int minL = getConfigInt("ability-2.min-level");
        int maxL = getConfigInt("ability-2.max-level");
        return Math.max(Math.min((int) (((float) (Math.max(Math.min(level, maxL), minL) - minL) / (maxL - minL)) * (max - min)), max), min);
    }
    public double calculateDamage(int level) {
        double min = getConfigDouble("ability-2.min-damage");
        double max = getConfigDouble("ability-2.max-damage");
        int minL = getConfigInt("ability-2.min-level");
        int maxL = getConfigInt("ability-2.max-level");
        return Math.max(Math.min((((float) (Math.max(Math.min(level, maxL), minL) - minL) / (maxL - minL)) * (max - min)), max), min);
    }
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE)
            .setTrigger(event -> {
                ItemStack[] items = getBoostInventory(event.player.getPersistentDataContainer());
                Inventory inventory = new CustomInventory("Boost Pocket", 9).getInventory();
                for (int i = 0; i < items.length; i++) {
                    inventory.setItem(i, items[i]);
                }
                event.player.openInventory(inventory);
            })
            .setEnd(event -> {
                if (event.eventType != AbilityEvent.Type.END_DISCONNECT) {
                    ItemStack[] items = getBoostInventory(event.player.getPersistentDataContainer());
                    for (ItemStack item : items) {
                        if (item == null) continue;
                        event.player.getWorld().dropItem(event.player.getLocation(), item);
                    }
                    ItemStack[] emptyItems = new ItemStack[9];
                    saveToBoostInventory(event.player.getPersistentDataContainer(), emptyItems);
                }
            });

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(getConfigInt("ability-1.duration"))
            .setActivate(event -> {
                event.player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, getConfigInt("ability-1.duration"), getConfigInt("ability-1.effect-level"), true));
                event.player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, getConfigInt("ability-1.duration"), getConfigInt("ability-1.effect-level"), true));
                CardSMP.instance.setupParticlesForEntity(event.player, Particle.TOTEM_OF_UNDYING.builder()).extra(0).count(20).spawn();
            });

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(-1)
            .setAbilityData(new DataMap().set("drain-timer", new DataInt()).set("drained", new DataInt()))
            .setActivate(event -> {
                if (event.player.getLevel() <= getConfigInt("ability-2.min-level")) {
                    event.cancelCooldown();
                    event.cancelAbility();
                }
            })
            .setTick(event -> {
                Collection<Player> players = event.player.getWorld().getNearbyEntitiesByType(Player.class, event.player.getLocation(), getConfigDouble("ability-2.radius"));
                for (Player player : players) {
                    if (TrustedPlayer.hasTrust(event.player, player) || player.equals(event.player)) {
                        PersistentDataContainer pdc = player.getPersistentDataContainer();
                        pdc.set(boostTickKey, PersistentDataType.INTEGER, 5);
                        pdc.set(damageBoostKey, PersistentDataType.DOUBLE, calculateDamage(event.player.getLevel()));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, calculateSpeed(event.player.getLevel()), true));
                    }
                }
                DataInt drainTimer = (DataInt) event.data.get("drain-timer");
                drainTimer.value++;
                if (drainTimer.value == getConfigInt("ability-2.drain-rate")) {
                    drainTimer.value = 0;
                    DataInt drained = (DataInt) event.data.get("drained");
                    drained.value++;
                    event.player.setLevel(event.player.getLevel() - 1);
                    if (drained.value == getConfigInt("ability-2.max-level") || event.player.getLevel() <= getConfigInt("ability-2.min-level")) {
                        event.cancelAbility();
                    }
                }
                CardSMP.instance.setupParticlesForEntity(event.player, Particle.TOTEM_OF_UNDYING.builder()).extra(0).count(2).spawn();
            })
            .setTrigger(event -> event.cancelAbility());

    @Override
    public String getName() {
        return "Boost";
    }
    @Override
    public String getID() {
        return "boost-card";
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
