package com.spaxxv.cardsmp.ritual;

import org.bukkit.inventory.ItemStack;

public class RitualItem {
    private final ItemStack item;
    private final int remove;
    public RitualItem(ItemStack s, int i) {
        item = s;
        remove = i;
    }
    public void doRemove() {
        item.subtract(remove);
    }
}
