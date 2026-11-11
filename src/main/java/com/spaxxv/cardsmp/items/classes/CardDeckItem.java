package com.spaxxv.cardsmp.items.classes;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.abilities.AbilityManager;
import com.spaxxv.cardsmp.abilities.AbilitySlot;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.cards.CardInstance;
import com.spaxxv.cardsmp.cards.CardManager;
import com.spaxxv.cardsmp.cards.Cards;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.meta.ItemMeta;

public class CardDeckItem extends BasicItem {
    public CardDeckItem(NamespacedKey id) {
        super(id, Material.BOOK);
        setMaxStack(1);
        setRarity(ItemRarity.EPIC);
    }
    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            event.setCancelled(true);
            if (CardManager.hasCard(event.getPlayer())) {
                if (!(AbilityManager.isActive(event.getPlayer(), AbilitySlot.ACTIVE1) || AbilityManager.isActive(event.getPlayer(), AbilitySlot.ACTIVE2))) {
                    if (!(AbilityManager.onCooldown(event.getPlayer(), AbilitySlot.ACTIVE1) || AbilityManager.onCooldown(event.getPlayer(), AbilitySlot.ACTIVE2))) {
                        CardInstance currentCard = CardManager.getCard(event.getPlayer());
                        Card newCard = Cards.getRandomCard(CardSMP.instance.getRng());
                        CardManager.equipCard(event.getPlayer(), new CardInstance(newCard, currentCard.tearLevel), true);
                        event.getPlayer().sendMessage(Component.text("Re-rolled your card! You now have a " + newCard.getName() + " Card."));
                        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) event.getItem().subtract();
                    } else {
                        event.getPlayer().sendMessage(Component.text("You cannot re-roll while you have an ability on cooldown!").color(NamedTextColor.RED));
                    }
                } else {
                    event.getPlayer().sendMessage(Component.text("You cannot re-roll while you have an ability active!").color(NamedTextColor.RED));
                }
            } else {
                event.getPlayer().sendMessage(Component.text("You do not have a card to re-roll!").color(NamedTextColor.RED));
            }
        }
    }
    @Override
    public String getItemName() {
        return "Card Deck";
    }
}
