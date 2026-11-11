
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.events.AbilityEvent;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataBoolean;
import com.spaxxv.cardsmp.data.values.DataInt;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Fireworks;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.WindCharge;
import org.bukkit.inventory.ItemStack;

public class SkyCard extends Card {
    private final String[] icons = new String[] {"\uE02D", "\uE02E", "\uE02F", "\uE030"};
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE);

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setAbilityData(new DataMap().set("trigger", new DataBoolean()).set("ticks", new DataInt()))
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(getConfigInt("ability-1.duration"))
            .setActivate(event -> {
                WindCharge windCharge = event.player.getWorld().spawn(event.player.getLocation(), WindCharge.class);
                windCharge.explode();
                event.player.setGliding(true);
            }).setTrigger(event -> {
                DataBoolean triggered = (DataBoolean) event.data.get("trigger");
                if (!triggered.value) {
                    ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
                    firework.setData(DataComponentTypes.FIREWORKS, Fireworks.fireworks().flightDuration(0).build());
                    event.player.setGliding(true);
                    event.player.fireworkBoost(firework);
                    triggered.value = true;
                } else {
                    event.player.sendPlainMessage("You cannot activate this ability's trigger more than once!");
                }
            }).setTick(event -> {
                DataInt ticks = (DataInt) event.data.get("ticks");
                ticks.value++;
                if (((Entity) event.player).isOnGround() && ticks.value > 10) {
                    event.cancelAbility();
                } else {
                    event.player.setGliding(true);
                }
            }).setEnd(event -> event.player.setGliding(false));

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(getConfigInt("ability-2.duration"))
            .setActivate(event -> {
                if (event.player.getFallDistance() > 7) {
                    event.player.setGliding(true);
                } else {
                    event.cancelCooldown();
                    event.cancelAbility();
                }
            }).setTick(event -> {
                if (((Entity) event.player).isOnGround()) {
                    event.cancelAbility();
                } else {
                    event.player.setGliding(true);
                }
            }).setEnd(event -> event.player.setGliding(false)).setTrigger(AbilityEvent::cancelAbility);

    @Override
    public String getName() {
        return "Sky";
    }
    @Override
    public String getID() {
        return "sky-card";
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
