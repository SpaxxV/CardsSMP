package com.spaxxv.cardsmp.recipes;

import com.spaxxv.cardsmp.items.Item;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PatternBuilder {
    private final Pattern pattern;
    private int index;
    public PatternBuilder(Pattern p) {
        index = 0;
        pattern = p;
    }
    public PatternBuilder addItem(Matchable item) {
        if (pattern.isCompiled()) return this;
        if (index < pattern.getSize()) {
            try {
                pattern.setItem(index, item);
                index++;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }
    public PatternBuilder addItem(Material m) {
        addItem(new PatternItem(m));
        return this;
    }
    public PatternBuilder addItem(Item m) {
        addItem(new PatternItem(m));
        return this;
    }
    public PatternBuilder addItems(Matchable[] items) {
        for (Matchable item : items) addItem(item);
        return this;
    }
    public PatternBuilder addItems(Material[] items) {
        for (Material item : items) addItem(item);
        return this;
    }
    public PatternBuilder addItems(Item[] items) {
        for (Item item : items) addItem(item);
        return this;
    }
    public PatternBuilder addFromMatrix(ItemStack[] matrix) {
        for (ItemStack itemStack : matrix) {
            if (itemStack != null) {
                addItem(new StrictPatternItem(itemStack.clone()));
            } else {
                addItem((PatternItem) null);
            }
        }
        return this;
    }
    public Pattern build() {
        if (!pattern.isCompiled()) {
            try {
                pattern.compile();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return pattern;
    }
}
