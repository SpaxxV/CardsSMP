package com.spaxxv.cardsmp.items;

import com.spaxxv.cardsmp.CardSMP;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemRegistry {
    private static final Map<NamespacedKey, Item> items = new HashMap<>();
    public static Item register(Item item) {
        items.put(item.getKey(), item);
        CardSMP.instance.getLogger().info("Registered item " + item.getKey());
        return item;
    }
    public static Item getItem(NamespacedKey key) {
        return items.get(key);
    }
    public static Set<NamespacedKey> getKeys() {
        return items.keySet();
    }
    public static boolean isCustom(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        return dataContainer.has(CardSMP.instance.getResourceKey("item_id"), PersistentDataType.STRING);
    }
    public static NamespacedKey itemIdFrom(ItemStack itemStack) {
        if (isCustom(itemStack)) {
            ItemMeta meta = itemStack.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            return NamespacedKey.fromString(dataContainer.get(CardSMP.instance.getResourceKey("item_id"), PersistentDataType.STRING));
        }
        return null;
    }
    public static Item getItemFrom(ItemStack itemStack) {
        if (isCustom(itemStack)) {
            return getItem(itemIdFrom(itemStack));
        }
        return null;
    }
}
