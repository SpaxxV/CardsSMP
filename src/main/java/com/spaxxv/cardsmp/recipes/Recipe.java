package com.spaxxv.cardsmp.recipes;

import com.spaxxv.cardsmp.events.PreCraftEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public abstract class Recipe {
    public abstract InventoryType getInventoryType();
    public abstract ItemStack getResult();
    public abstract boolean matches(Pattern pattern);
    public abstract void firePreCraftEvent(PreCraftEvent event);
}
