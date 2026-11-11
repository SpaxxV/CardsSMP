
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataString;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.UUID;

public class BountyCard extends Card {
    private final String[] icons = new String[] {"\uE009", "\uE00A", "\uE00B", "\uE00C"};
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE);

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(getConfigInt("ability-1.duration"))
            .setActivate(event -> {
                Collection<Player> players = event.player.getWorld().getNearbyEntitiesByType(Player.class, event.player.getLocation(), getConfigDouble("ability-1.radius"));
                for (Player player : players) {
                    if (!TrustedPlayer.hasTrust(event.player, player) && !player.equals(event.player)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, getConfigInt("ability-1.duration"), 0));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, getConfigInt("ability-1.duration"), 0));
                        CardSMP.instance.setupParticlesForEntity(event.player, Particle.END_ROD.builder()).extra(0).count(10).spawn();
                    }
                }
            });

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(20)
            .setAbilityData(new DataMap().set("target", new DataString()))
            .setActivate(event -> {
                ItemStack item = event.player.getInventory().getItemInMainHand();
                if (item.getType() == Material.PLAYER_HEAD) {
                    if (item.hasData(DataComponentTypes.PROFILE)) {
                        ResolvableProfile profile = item.getData(DataComponentTypes.PROFILE);
                        if (profile.uuid() != null) {
                            event.data.set("target", new DataString(profile.uuid().toString()));
                            Player player = CardSMP.instance.getServer().getPlayer(profile.uuid());
                            World world = event.player.getWorld();
                            if (player != null && player.isOnline() && player.getWorld().equals(world)) {
                                if (player.equals(event.player)) {
                                    event.player.sendMessage(Component.text("You cannot track yourself.").color(NamedTextColor.RED));
                                    event.cancelCooldown();
                                    event.cancelAbility();
                                    return;
                                }
                                Location centralLocation = event.player.getEyeLocation().clone();
                                Location targetLocation = player.getLocation().clone();
                                if (!TrustedPlayer.hasTrust(event.player, player) && centralLocation.distance(targetLocation) < getConfigInt("ability-2.warn-distance")) {
                                    event.player.sendMessage(Component.text("Your bounty is near...").color(NamedTextColor.GREEN));
                                    player.sendMessage(Component.text("You are being hunted.").color(NamedTextColor.RED));
                                }
                                Vector vector = targetLocation.subtract(centralLocation).toVector().normalize().multiply(0.25);
                                for (int i = 0; i < 20; i++) {
                                    centralLocation.add(vector);
                                    Particle.END_ROD.builder().location(centralLocation).extra(0).count(1).spawn();
                                }
                                return;
                            }
                        }
                    }
                    event.player.sendMessage(Component.text("Failed to track player. They must be online and in the same dimension.").color(NamedTextColor.RED));
                    event.cancelCooldown();
                    event.cancelAbility();
                    return;
                }
                event.player.sendMessage(Component.text("Failed to track player. You must hold a player head in your main hand.").color(NamedTextColor.RED));
                event.cancelCooldown();
                event.cancelAbility();
            })
            .setTick(event -> {
                Player player = CardSMP.instance.getServer().getPlayer(UUID.fromString(((DataString) event.data.get("target")).value));
                World world = event.player.getWorld();
                if (player != null && player.isOnline() && player.getWorld().equals(world)) {
                    Location centralLocation = event.player.getEyeLocation().clone();
                    Location targetLocation = player.getLocation().clone();

                    Vector vector = targetLocation.subtract(centralLocation).toVector().normalize().multiply(0.25);
                    for (int i = 0; i < 20; i++) {
                        centralLocation.add(vector);
                        Particle.END_ROD.builder().location(centralLocation).extra(0).count(1).spawn();
                    }
                }
            });

    @Override
    public String getName() {
        return "Bounty";
    }
    @Override
    public String getID() {
        return "bounty-card";
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
