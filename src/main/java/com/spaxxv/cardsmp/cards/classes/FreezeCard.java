
package com.spaxxv.cardsmp.cards.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.data.values.DataString;
import com.spaxxv.cardsmp.data.values.DataSubMap;
import com.spaxxv.cardsmp.util.SetBuilder;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class FreezeCard extends Card {
    private final String[] icons = new String[] {"\uE015", "\uE016", "\uE017", "\uE018"};
    private final Set<Material> coldBlocks = new SetBuilder<Material>().addElements(Material.SNOW_BLOCK, Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE, Material.FROSTED_ICE, Material.SNOW, Material.POWDER_SNOW).toSet();
    private final Ability passiveAbility = new Ability()
            .setPassive(true)
            .setSlot(AbilitySlot.PASSIVE)
            .setTick(event -> {
                Block block = event.player.getLocation().subtract(0, 0.5, 0).getBlock();
                if (coldBlocks.contains(block.getType())) {
                    event.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 2, true));
                }
            });

    private final Ability activeAbility1 = new Ability()
            .setSlot(AbilitySlot.ACTIVE1)
            .setCooldown(getConfigInt("ability-1.cooldown"))
            .setDuration(getConfigInt("ability-1.duration"))
            .setAbilityData(new DataMap().set("player-hits", new DataSubMap()));

    private final Ability activeAbility2 = new Ability()
            .setSlot(AbilitySlot.ACTIVE2)
            .setCooldown(getConfigInt("ability-2.cooldown"))
            .setDuration(getConfigInt("ability-2.duration"))
            .setAbilityData(new DataMap().set("targeted-player", new DataString()).set("damage-tick", new DataInt()))
            .setTick(event -> {
                DataString uuidString = (DataString) event.data.get("targeted-player");
                if (!uuidString.value.isEmpty()) {
                    Player player = CardSMP.instance.getServer().getPlayer(UUID.fromString(uuidString.value));
                    if (player != null) {
                        CardSMP.instance.setupParticlesForEntity(player, Particle.SNOWFLAKE.builder()).extra(0).count(1).spawn();
                        player.setFreezeTicks(player.getFreezeTicks() + 2);
                        DataInt tick = (DataInt) event.data.get("damage-tick");
                        tick.value++;
                        if (tick.value % 20 == 0) {
                            player.damage(1, DamageSource.builder(DamageType.FREEZE).withDirectEntity(event.player).build());
                        }
                    }
                }
            });

    @Override
    public String getName() {
        return "Freeze";
    }
    @Override
    public String getID() {
        return "freeze-card";
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
