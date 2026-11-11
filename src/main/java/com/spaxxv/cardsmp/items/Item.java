package com.spaxxv.cardsmp.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class Item {
    public abstract void onInteract(PlayerInteractEvent event);
    public abstract NamespacedKey getKey();
    public abstract String getItemName();
    public abstract ItemStack createItemStack();
}
