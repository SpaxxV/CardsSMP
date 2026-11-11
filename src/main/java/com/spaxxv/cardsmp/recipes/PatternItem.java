package com.spaxxv.cardsmp.recipes;

import com.spaxxv.cardsmp.items.Item;
import com.spaxxv.cardsmp.items.ItemRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PatternItem implements Matchable {
    private final Material material;
    private final Item item;
    public PatternItem(ItemStack itemStack) {
        if (ItemRegistry.isCustom(itemStack) && ItemRegistry.getItemFrom(itemStack) != null) {
            item = ItemRegistry.getItemFrom(itemStack);
            material = null;
        } else {
            material = itemStack.getType();
            item = null;
        }
    }
    public PatternItem(Material m) {
        item = null;
        material = m;
    }
    public PatternItem(Item i) {
        material = null;
        item = i;
    }
    public boolean isVanilla() {
        return material != null;
    }
    public boolean isCustomItem() {
        return item != null;
    }
    protected boolean matches(PatternItem patternItem) {
        if (isVanilla() && patternItem.isVanilla()) {
            return material == patternItem.material;
        } else if (isCustomItem() && patternItem.isCustomItem()) {
            return item == patternItem.item;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        if (isVanilla()) return material.toString();
        if (isCustomItem()) return "custom:" + item.getItemName().replaceAll(" ", "_");
        return "PI:null";
    }

    @Override
    public boolean matches(Matchable matchable) {
        if (matchable instanceof PatternItem i) {
            return matches(i);
        }
        return false;
    }
}
