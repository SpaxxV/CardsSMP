package com.spaxxv.cardsmp.events;

import com.spaxxv.cardsmp.recipes.Recipe;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PreCraftEvent {
    private final Player player;
    private final Recipe recipe;
    private ItemStack result;
    private boolean cancelled;
    public PreCraftEvent(Player p, Recipe r) {
        player = p;
        recipe = r;
        result = recipe.getResult();
        cancelled = false;
    }
    public Player getPlayer() {
        return player;
    }
    public Recipe getRecipe() {
        return recipe;
    }
    public boolean isCancelled() {
        return cancelled;
    }
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    public void setResult(ItemStack newResult) {
        result = newResult;
    }
    public ItemStack getResult() {
        return result;
    }
}
