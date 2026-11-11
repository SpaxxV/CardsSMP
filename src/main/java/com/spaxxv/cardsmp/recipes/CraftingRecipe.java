package com.spaxxv.cardsmp.recipes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.events.PreCraftEvent;
import com.spaxxv.cardsmp.events.PreCraftEventListener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class CraftingRecipe extends Recipe {
    private final Pattern recipe;
    private final ItemStack result;
    private PreCraftEventListener onPreCraft;
    public CraftingRecipe(Pattern pattern, ItemStack item) {
        recipe = pattern;
        result = item;
        onPreCraft = null;
    }
    public CraftingRecipe setPreCraftEvent(PreCraftEventListener listener) {
        onPreCraft = listener;
        return this;
    }
    @Override
    public InventoryType getInventoryType() {
        return InventoryType.CRAFTING;
    }

    @Override
    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(Pattern pattern) {
        boolean result = recipe.matches(pattern);
        return result;
    }

    @Override
    public void firePreCraftEvent(PreCraftEvent event) {
        if (onPreCraft != null) onPreCraft.onFire(event);
    }
}
