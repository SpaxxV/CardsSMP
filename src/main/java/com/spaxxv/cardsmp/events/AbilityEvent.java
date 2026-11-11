package com.spaxxv.cardsmp.events;

import com.spaxxv.cardsmp.abilities.Ability;
import com.spaxxv.cardsmp.data.DataMap;
import org.bukkit.entity.Player;

public class AbilityEvent {
    public enum Type {
        ACTIVATE, TRIGGER, TICK, END, END_DISCONNECT
    }
    public Player player;
    public Ability ability;
    public DataMap data;
    public Type eventType;
    public int remainingTime;
    private boolean cancel;
    private boolean cancelCooldown;
    public AbilityEvent(Player p, Ability a, DataMap d, int r, Type type) {
        player = p;
        ability = a;
        data = d;
        remainingTime = r;
        eventType = type;
        cancel = false;
        cancelCooldown = false;
    }
    public void cancelCooldown() {
        cancelCooldown = true;
    }
    public void cancelAbility() {
        cancel = true;
    }
    public boolean isCancelled() {
        return cancel;
    }
    public boolean cooldownCancelled() {return cancelCooldown;}
}
