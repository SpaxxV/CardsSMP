package com.spaxxv.cardsmp.items;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.cards.Cards;
import com.spaxxv.cardsmp.items.classes.BasicItem;
import com.spaxxv.cardsmp.items.classes.CardDeckItem;
import com.spaxxv.cardsmp.items.classes.EventCardItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemRarity;

public class Items {
    public static Item CARD_DECK;
    public static Item CARD_TAPE;
    public static Item BLOOD_CARD;
    public static Item SPADE_SHARD;
    public static Item HEART_SHARD;
    public static Item DIAMOND_SHARD;
    public static Item CLUB_SHARD;
    private static NamespacedKey key(String s) {
        return CardSMP.instance.getResourceKey(s);
    }
    public static void registerAll() {
        CARD_DECK = ItemRegistry.register(new CardDeckItem(key("card_deck")));
        CARD_TAPE = ItemRegistry.register(new BasicItem(key("card_tape"), Material.SLIME_BALL).setItemName("Card Tape").setMaxStack(1).setRarity(ItemRarity.RARE));
        BLOOD_CARD = ItemRegistry.register(new EventCardItem(key("blood_card"), Cards.BLOOD).disableModel());
        SPADE_SHARD = ItemRegistry.register(new BasicItem(key("spade_shard"), Material.IRON_NUGGET).setItemName("Spade Shard").setMaxStack(1).setRarity(ItemRarity.UNCOMMON));
        HEART_SHARD = ItemRegistry.register(new BasicItem(key("heart_shard"), Material.GOLD_NUGGET).setItemName("Heart Shard").setMaxStack(1).setRarity(ItemRarity.UNCOMMON));
        DIAMOND_SHARD = ItemRegistry.register(new BasicItem(key("diamond_shard"), Material.DIAMOND).setItemName("Diamond Shard").setMaxStack(1).setRarity(ItemRarity.UNCOMMON));
        CLUB_SHARD = ItemRegistry.register(new BasicItem(key("club_shard"), Material.LAPIS_LAZULI).setItemName("Club Shard").setMaxStack(1).setRarity(ItemRarity.UNCOMMON));
    }
}
