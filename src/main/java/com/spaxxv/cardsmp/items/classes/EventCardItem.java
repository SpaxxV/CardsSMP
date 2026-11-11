package com.spaxxv.cardsmp.items.classes;

import com.spaxxv.cardsmp.cards.CardInstance;
import com.spaxxv.cardsmp.cards.CardManager;
import com.spaxxv.cardsmp.cards.EventCard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.tag.DamageTypeTags;

public class EventCardItem extends BasicItem {
    private final EventCard cardType;
    public EventCardItem(NamespacedKey id, EventCard card) {
        super(id, Material.PAPER);
        cardType = card;
        setMaxStack(1);
        setItemName(cardType.getName() + " Card");
        setRarity(ItemRarity.EPIC);
    }
    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            event.setCancelled(true);
            boolean success = CardManager.equipCard(event.getPlayer(), new CardInstance(cardType, 0));
            if (success) {
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) event.getItem().subtract();
            } else {
                event.getPlayer().sendMessage(Component.text("You already have an event card equipped!").color(NamedTextColor.RED));
            }
        }
    }
    @Override
    protected ItemMeta createItemMeta(ItemMeta meta) {
        super.createItemMeta(meta);
        meta.setDamageResistant(DamageTypeTags.IS_FIRE);
        return meta;
    }
}
