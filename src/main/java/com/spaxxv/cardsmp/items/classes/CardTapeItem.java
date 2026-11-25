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

public class CardTapeItem extends BasicItem {
    public CardTapeItem(NamespacedKey id) {
        super(id, Material.SLIME_BALL);
        setMaxStack(1);
        setRarity(ItemRarity.RARE);
    }
    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            event.setCancelled(true);
            if (CardManager.hasCard(event.getPlayer())) {
                CardInstance currentCard = CardManager.getCard(event.getPlayer());
                if (currentCard != null && currentCard.tearLevel > 0) {
                    currentCard.tearLevel--;
                    CardManager.equipCard(event.getPlayer(), currentCard, true);
                    event.getPlayer().sendMessage(Component.text("Repaired your card! You are now at Tear " + currentCard.tearLevel).color(NamedTextColor.GREEN));
                } else {
                    event.getPlayer().sendMessage(Component.text("Your card does not need repaired!").color(NamedTextColor.RED));
                }
            } else {
                event.getPlayer().sendMessage(Component.text("You do not have a card to repair!").color(NamedTextColor.RED));
            }
        }
    }
    @Override
    public String getItemName() {
        return "Card Tape";
    }
}
