package com.spaxxv.cardsmp.abilities;

import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.events.AbilityEvent;
import com.spaxxv.cardsmp.events.AbilityEventListener;

public class Ability {
    private static int currentID = 0;
    private final int id;
    public boolean passive;
    public int duration;
    public int cooldown;
    public AbilitySlot slot;
    public DataMap abilityData;
    private AbilityEventListener tickEvent;
    private AbilityEventListener activateEvent;
    private AbilityEventListener triggerEvent;
    private AbilityEventListener endEvent;
    public Ability() {
        passive = false;
        duration = 0;
        cooldown = 0;
        slot = AbilitySlot.ACTIVE1;
        abilityData = null;
        id = currentID;
        currentID++;
    }
    public void onActiveTick(AbilityEvent event) {
        if (tickEvent != null) tickEvent.onFire(event);
    }
    public void onActivate(AbilityEvent event) {
        if (activateEvent != null) activateEvent.onFire(event);
    }
    public void onTrigger(AbilityEvent event) {
        if (triggerEvent != null) triggerEvent.onFire(event);
    }
    public void onEnd(AbilityEvent event) {
        if (endEvent != null) endEvent.onFire(event);
    }
    public boolean hasTrigger() {
        return triggerEvent != null;
    }
    public Ability setPassive(boolean val) {
        passive = val;
        return this;
    }
    public Ability setDuration(int val) {
        duration = val;
        return this;
    }
    public Ability setCooldown(int val) {
        cooldown = val;
        return this;
    }
    public Ability setSlot(AbilitySlot s) {
        slot = s;
        return this;
    }
    public Ability setAbilityData(DataMap m) {
        abilityData = m;
        return this;
    }
    public Ability setTick(AbilityEventListener event) {
        tickEvent = event;
        return this;
    }
    public Ability setActivate(AbilityEventListener event) {
        activateEvent = event;
        return this;
    }
    public Ability setTrigger(AbilityEventListener event) {
        triggerEvent = event;
        return this;
    }
    public Ability setEnd(AbilityEventListener event) {
        endEvent = event;
        return this;
    }
    public int getId() {
        return id;
    }
}
