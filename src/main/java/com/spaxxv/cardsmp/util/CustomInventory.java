package com.spaxxv.cardsmp.util;

import com.spaxxv.cardsmp.CardSMP;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class CustomInventory implements InventoryHolder {
    private final Inventory inventory;
    public CustomInventory(String name, int slots) {
        inventory = CardSMP.instance.getServer().createInventory(this, slots, Component.text(name));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
