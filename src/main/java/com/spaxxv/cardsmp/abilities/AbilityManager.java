package com.spaxxv.cardsmp.abilities;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.cards.CardInstance;
import com.spaxxv.cardsmp.cards.CardManager;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.events.AbilityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AbilityManager {
    private static final int[] tearStates = new int[] {0, 0, 1, 2, 2, 3};
    private static final NamespacedKey cooldownP = CardSMP.instance.getResourceKey("passiveCooldown");
    private static final NamespacedKey cooldown1 = CardSMP.instance.getResourceKey("abilityCooldown1");
    private static final NamespacedKey cooldown2 = CardSMP.instance.getResourceKey("abilityCooldown2");
    private static final NamespacedKey eventCooldownP = CardSMP.instance.getResourceKey("eventPassiveCooldown");
    private static final NamespacedKey eventCooldown = CardSMP.instance.getResourceKey("eventCooldown");
    private static NamespacedKey cooldownKey(AbilitySlot s) {
        switch (s) {
            case PASSIVE -> {
                return cooldownP;
            }
            case ACTIVE1 -> {
                return cooldown1;
            }
            case ACTIVE2 -> {
                return cooldown2;
            }
            case EVENTPASSIVE -> {
                return eventCooldownP;
            }
            case EVENT -> {
                return eventCooldown;
            }
        }
        return cooldown1;
    }
    private static final Map<String, DataInt> guiTimings = new HashMap<>();
    private static final Map<String, Map<AbilitySlot, AbilityInstance>> abilityMap = new HashMap<>();
    private static final ArrayList<AbilityInstance> activeAbilities = new ArrayList<>();
    private static DataInt getGuiTime(String playerUUID) {
        if (guiTimings.containsKey(playerUUID)) {
            return guiTimings.get(playerUUID);
        } else {
            DataInt guiTime = new DataInt();
            guiTimings.put(playerUUID, guiTime);
            return guiTime;
        }
    }
    private static Map<AbilitySlot, AbilityInstance> playerAbilityData(String playerUUID) {
        if (abilityMap.containsKey(playerUUID)) {
            return abilityMap.get(playerUUID);
        } else {
            Map<AbilitySlot, AbilityInstance> data = new HashMap<>();
            abilityMap.put(playerUUID, data);
            return data;
        }
    }
    private static void endAbilityForced(AbilityInstance ability) {
        String playerUUID = ability.getPlayer().getPlayerProfile().getId().toString();
        Map<AbilitySlot, AbilityInstance> abilityData = playerAbilityData(playerUUID);
        if (abilityData.containsKey(ability.getAbility().slot)) {
            ability.getAbility().onEnd(new AbilityEvent(ability.getPlayer(), ability.getAbility(), ability.data, ability.remainingTicks, AbilityEvent.Type.END_DISCONNECT));
            ability.getPlayer().setCooldown(cooldownKey(ability.getAbility().slot), ability.getCooldown());
            abilityData.remove(ability.getAbility().slot);
        }
    }
    private static void endAbilityTick(AbilityInstance ability) {
        String playerUUID = ability.getPlayer().getPlayerProfile().getId().toString();
        Map<AbilitySlot, AbilityInstance> abilityData = playerAbilityData(playerUUID);
        if (abilityData.containsKey(ability.getAbility().slot)) {
            ability.getAbility().onEnd(new AbilityEvent(ability.getPlayer(), ability.getAbility(), ability.data, ability.remainingTicks, AbilityEvent.Type.END));
            ability.getPlayer().setCooldown(cooldownKey(ability.getAbility().slot), ability.getCooldown());
            abilityData.remove(ability.getAbility().slot);
        }
    }
    private static void endAbility(Map<AbilitySlot, AbilityInstance> abilityData, AbilitySlot slot, boolean disconnect) {
        if (abilityData.containsKey(slot)) {
            AbilityInstance ability = abilityData.get(slot);
            ability.getAbility().onEnd(new AbilityEvent(ability.getPlayer(), ability.getAbility(), ability.data, ability.remainingTicks, disconnect ? AbilityEvent.Type.END_DISCONNECT : AbilityEvent.Type.END));
            ability.getPlayer().setCooldown(cooldownKey(slot), ability.getCooldown());
            activeAbilities.remove(ability);
            abilityData.remove(slot);
        }
    }
    private static void endAbility(Map<AbilitySlot, AbilityInstance> abilityData, AbilitySlot slot) {
        endAbility(abilityData, slot, false);
    }
    public static void endAbility(Player player, AbilitySlot slot) {
        String playerUUID = player.getPlayerProfile().getId().toString();
        Map<AbilitySlot, AbilityInstance> abilityData = playerAbilityData(playerUUID);
        endAbility(abilityData, slot);
    }
    public static void endAbility(AbilityInstance ability) {
        endAbility(ability.getPlayer(), ability.getAbility().slot);
    }
    public static void endPlayerAbilities(Player player, boolean disconnect) {
        String playerUUID = player.getPlayerProfile().getId().toString();
        endPlayerAbilities(player, new AbilitySlot[]{
                AbilitySlot.PASSIVE,
                AbilitySlot.ACTIVE1,
                AbilitySlot.ACTIVE2,
                AbilitySlot.EVENTPASSIVE,
                AbilitySlot.EVENT
        }, disconnect);
        if (disconnect) abilityMap.remove(playerUUID);
    }
    public static void endPlayerAbilities(Player player, AbilitySlot[] slots, boolean disconnect) {
        String playerUUID = player.getPlayerProfile().getId().toString();
        Map<AbilitySlot, AbilityInstance> abilityData = playerAbilityData(playerUUID);
        for (AbilitySlot slot : slots) endAbility(abilityData, slot, disconnect);
    }
    public static void endPlayerAbilities(Player player, AbilitySlot[] slots) {
        endPlayerAbilities(player, slots, false);
    }
    public static void endAllAbilities() {
        Iterator<AbilityInstance> iterator = activeAbilities.iterator();
        while (iterator.hasNext()) {
            AbilityInstance ability = iterator.next();
            endAbilityForced(ability);
            iterator.remove();
        }
    }
    public static boolean activateAbility(AbilityInstance ability) {
        String playerUUID = ability.getPlayer().getPlayerProfile().getId().toString();
        Map<AbilitySlot, AbilityInstance> abilityData = playerAbilityData(playerUUID);
        if (!(abilityData.containsKey(ability.getAbility().slot))) {
            activeAbilities.add(ability);
            abilityData.put(ability.getAbility().slot, ability);
            AbilityEvent event = new AbilityEvent(ability.getPlayer(), ability.getAbility(), ability.data, ability.remainingTicks, AbilityEvent.Type.ACTIVATE);
            ability.getAbility().onActivate(event);
            ability.remainingTicks = event.remainingTime;
            if (event.cooldownCancelled()) ability.cancelCooldown();
            if (event.isCancelled()) endAbility(ability);
            return true;
        } else {
            return false;
        }
    }
    public static void callTrigger(Player player, AbilitySlot slot) {
        if (isActive(player, slot)) {
            AbilityInstance ability = getAbilityInSlot(player, slot);
            AbilityEvent event = new AbilityEvent(ability.getPlayer(), ability.getAbility(), ability.data, ability.remainingTicks, AbilityEvent.Type.TRIGGER);
            ability.getAbility().onTrigger(event);
            ability.remainingTicks = event.remainingTime;
            if (event.cooldownCancelled()) ability.cancelCooldown();
            if (event.isCancelled()) endAbility(ability);
        }
    }
    public static AbilityInstance getAbilityInstance(Player player, Ability ability) {
        if (isActive(player, ability)) {
            return getAbilityInSlot(player, ability.slot);
        }
        return null;
    }
    public static AbilityInstance getAbilityInSlot(Player player, AbilitySlot slot) {
        String playerUUID = player.getPlayerProfile().getId().toString();
        Map<AbilitySlot, AbilityInstance> abilityData = playerAbilityData(playerUUID);
        if (abilityData.containsKey(slot)) {
            return abilityData.get(slot);
        }
        return null;
    }
    public static void tickAbilities() {
        for (AbilityInstance ability : activeAbilities) {
            if (ability.remainingTicks == 0 || ability.markedForRemoval()) {
                ability.markForRemoval();
                continue;
            }
            AbilityEvent event = new AbilityEvent(ability.getPlayer(), ability.getAbility(), ability.data, ability.remainingTicks, AbilityEvent.Type.TICK);
            ability.getAbility().onActiveTick(event);
            ability.remainingTicks = event.remainingTime;
            if (event.cooldownCancelled()) ability.cancelCooldown();
            if (event.isCancelled()) {
                ability.markForRemoval();
                continue;
            }
            if (ability.remainingTicks != -1) ability.remainingTicks--;
        }
        Iterator<AbilityInstance> iterator = activeAbilities.iterator();
        while (iterator.hasNext()) {
            AbilityInstance ability = iterator.next();
            if (ability.markedForRemoval()) {
                endAbilityTick(ability);
                iterator.remove();
            }
        }
    }
    private static boolean abilityToolbar(Player player) {
        DataInt guiTime = getGuiTime(player.getPlayerProfile().getId().toString());
        if (isActive(player, AbilitySlot.ACTIVE1) || isActive(player, AbilitySlot.ACTIVE2) || isActive(player, AbilitySlot.EVENT) ||
                onCooldown(player, AbilitySlot.ACTIVE1) || onCooldown(player, AbilitySlot.ACTIVE2) || onCooldown(player, AbilitySlot.EVENT)) {
            guiTime.value = 600;
            return true;
        } else {
            if (guiTime.value > 0) guiTime.value--;
            return (guiTime.value != 0);
        }
    }
    private static Component toolbarForAbility(Player player, Ability ability, CardInstance cardInstance) {
        TextComponent.Builder builder = Component.text();
        if (isActive(player, ability)) {
            builder.append(Component.text("Active").color(NamedTextColor.YELLOW));
        } else if (onCooldown(player, ability.slot)) {
            builder.append(Component.text(String.format("%.0f", Math.ceil(getCooldown(player, ability.slot) / 20f)) + "s").color(NamedTextColor.RED));
        } else if (cardInstance.tearLevel != 5) {
            builder.append(Component.text("Ready").color(NamedTextColor.GREEN));
        } else {
            builder.append(Component.text("Disabled").color(NamedTextColor.GRAY));
        }
        return builder.build();
    }
    public static Component generateToolbar(Player player) {
        TextComponent.Builder builder = Component.text();
        boolean abilityStyle = abilityToolbar(player);
        boolean flag = false;
        if (CardManager.hasCard(player)) {
            if (abilityStyle) {
                flag = true;
                CardInstance cardInstance = CardManager.getCard(player);
                Ability a1 = cardInstance.card.ability1();
                Ability a2 = cardInstance.card.ability2();
                builder.append(toolbarForAbility(player, a1, cardInstance));
                builder.append(Component.text(" | "));
                builder.append(toolbarForAbility(player, a2, cardInstance));
                if (CardManager.hasEventCard(player)) builder.append(Component.text(" | "));
            } else {
                flag = true;
                CardInstance cardInstance = CardManager.getCard(player);
                builder.append(Component.text(cardInstance.card.iconCharFor(tearStates[cardInstance.tearLevel])));
                if (CardManager.hasEventCard(player)) builder.append(Component.text(" "));
            }
        }
        if (CardManager.hasEventCard(player)) {
            if (abilityStyle) {
                flag = true;
                CardInstance eventCardInstance = CardManager.getEventCard(player);
                Ability ae = eventCardInstance.card.ability1();
                builder.append(toolbarForAbility(player, ae, eventCardInstance));
            } else {
                flag = true;
                CardInstance eventCardInstance = CardManager.getEventCard(player);
                builder.append(Component.text(eventCardInstance.card.iconCharFor(0)));
            }
        }
        if (flag) return builder.build(); else return null;
    }
    public static boolean isActive(Player player, Ability ability) {
        AbilityInstance abilityInstance = getAbilityInSlot(player, ability.slot);
        if (abilityInstance == null) return false; else return (abilityInstance.getAbility().getId() == ability.getId());
    }
    public static boolean isActive(Player player, AbilitySlot slot) {
        String playerUUID = player.getPlayerProfile().getId().toString();
        Map<AbilitySlot, AbilityInstance> abilityData = playerAbilityData(playerUUID);
        return abilityData.containsKey(slot);
    }
    public static void clearCooldown(Player player, AbilitySlot slot) {
        player.setCooldown(cooldownKey(slot), 0);
    }
    public static int getCooldown(Player player, AbilitySlot slot) {
        return player.getCooldown(cooldownKey(slot));
    }
    public static boolean onCooldown(Player player, AbilitySlot slot) {
        return getCooldown(player, slot) > 0;
    }
}
