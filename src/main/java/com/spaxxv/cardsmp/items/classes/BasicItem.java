package com.spaxxv.cardsmp.items.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.items.Item;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class BasicItem extends Item {
    private final NamespacedKey key;
    private final Material vanillaItem;
    private String itemName;
    private ItemRarity rarity;
    private boolean disableModel;
    public int maxStackSize;
    public BasicItem(NamespacedKey id, Material material) {
        key = id;
        vanillaItem = material;
        maxStackSize = 64;
        itemName = key.value();
        rarity = ItemRarity.COMMON;
        disableModel = false;
    }
    @Override
    public void onInteract(PlayerInteractEvent event) {
        // Do nothing. Only for TAS
    }
    @Override
    public NamespacedKey getKey() {
        return key;
    }
    public BasicItem setRarity(ItemRarity r) {
        rarity = r;
        return this;
    }
    public BasicItem setItemName(String s) {
        itemName = s;
        return this;
    }
    public BasicItem disableModel() {
        disableModel = true;
        return this;
    }
    @Override
    public String getItemName() {
        return itemName;
    }
    public BasicItem setMaxStack(int i) {
        maxStackSize = i;
        return this;
    }
    protected ItemMeta createItemMeta(ItemMeta meta) {
        meta.setMaxStackSize(maxStackSize);
        meta.itemName(Component.text(getItemName()));
        meta.setRarity(rarity);
        if (!disableModel) {
            CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
            modelData.setStrings(List.of(key.value()));
            meta.setCustomModelDataComponent(modelData);
        }
        return meta;
    }
    @Override
    public ItemStack createItemStack() {
        ItemStack itemStack = new ItemStack(vanillaItem);

        ItemMeta meta = createItemMeta(itemStack.getItemMeta());

        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        dataContainer.set(CardSMP.instance.getResourceKey("item_id"), PersistentDataType.STRING, key.toString());

        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
