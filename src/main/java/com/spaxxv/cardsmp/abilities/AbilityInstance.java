package com.spaxxv.cardsmp.abilities;

import com.spaxxv.cardsmp.data.DataMap;
import org.bukkit.entity.Player;

public class AbilityInstance {
    private final Ability ability;
    private final Player player;
    private boolean remove;
    private boolean doCooldown;
    public DataMap data;
    public int remainingTicks;
    public AbilityInstance(Ability a, Player p) {
        ability = a;
        player = p;
        remove = false;
        doCooldown = true;
        if (ability.abilityData != null) data = ability.abilityData.clone(); else data = null;
        if (ability.passive) remainingTicks = -1; else remainingTicks = ability.duration;
    }
    public int getCooldown() {
        return doCooldown ? ability.cooldown : 0;
    }
    public Ability getAbility() {
        return ability;
    }
    public Player getPlayer() {
        return player;
    }
    public void markForRemoval() {
        remove = true;
    }
    public void cancelCooldown() {
        doCooldown = false;
    }
    public boolean markedForRemoval() {
        return remove;
    }
}
