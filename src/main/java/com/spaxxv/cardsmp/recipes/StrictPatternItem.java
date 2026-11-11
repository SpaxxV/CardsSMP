package com.spaxxv.cardsmp.recipes;

import com.spaxxv.cardsmp.util.SetBuilder;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class StrictPatternItem extends PatternItem {
    private static final Set<DataComponentType> defaultIgnore = new SetBuilder<DataComponentType>().addElements(DataComponentTypes.ENCHANTMENTS, DataComponentTypes.CUSTOM_NAME, DataComponentTypes.DAMAGE).toSet();
    private final ItemStack itemStack;
    private final Set<DataComponentType> ignoreTypes;
    public StrictPatternItem(ItemStack item) {
        super(item);
        itemStack = item;
        ignoreTypes = defaultIgnore;
    }
    public StrictPatternItem(ItemStack item, Set<DataComponentType> ignore) {
        super(item);
        itemStack = item;
        ignoreTypes = ignore;
    }
    @Override
    public boolean matches(PatternItem patternItem) {
        if (patternItem instanceof StrictPatternItem strictPatternItem) {
            return itemStack.matchesWithoutData(strictPatternItem.itemStack, ignoreTypes, true);
        } else {
            return super.matches(patternItem);
        }
    }
}
