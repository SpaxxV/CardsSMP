package com.spaxxv.cardsmp;

import com.spaxxv.cardsmp.cards.CardInstance;
import com.spaxxv.cardsmp.cards.CardManager;
import com.spaxxv.cardsmp.cards.Cards;
import com.spaxxv.cardsmp.data.values.DataBoolean;
import com.spaxxv.cardsmp.util.PlayerAnimData;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CardSMPStartAnimation {
    public static Sound click = Sound.sound(NamespacedKey.minecraft("block.note_block.hat"), Sound.Source.MASTER, 1.0f, 1.0f);
    public static Sound pick = Sound.sound(NamespacedKey.minecraft("entity.player.levelup"), Sound.Source.MASTER, 1.0f, 1.0f);
    public static Sound roar = Sound.sound(NamespacedKey.minecraft("entity.ender_dragon.growl"), Sound.Source.MASTER, 1.0f, 1.0f);
    public static int tickCount = 0;
    public static int c = 0;
    public static int t = 3;
    public static int g = 4;
    public static String[] cardIds;
    public static Collection<? extends Player> players;
    public static Map<Player, PlayerAnimData> animDataMap;
    public static boolean active = false;
    public static void tick() {
        tickCount++;
        if (tickCount == 1) {
            CardSMP.pluginData.set("server-start", new DataBoolean(true));
            players = CardSMP.instance.getServer().getOnlinePlayers();
            animDataMap = new HashMap<>();
            cardIds = Cards.getCardIds();
            c = 0;
            t = 3;
            g = 4;
            for (Player player : players) {
                PlayerAnimData animData = new PlayerAnimData();
                animData.cardType = Cards.getCard(cardIds[c]);
                animDataMap.put(player, animData);
            }
        }
        if (tickCount < 60) {
            if (tickCount % 4 == 0) {
                c++;
                if (c == cardIds.length) c = 0;
                for (Player player : players) {
                    PlayerAnimData animData = animDataMap.get(player);
                    animData.cardType = Cards.getCard(cardIds[c]);
                    player.playSound(click, Sound.Emitter.self());
                }
            }
        } else if (tickCount < 178) {
            t++;
            if (t == g) {
                g = (int) (1.5f * g);
                t = 0;
                for (Player player : players) {
                    PlayerAnimData animData = animDataMap.get(player);
                    animData.cardType = Cards.getCard(cardIds[CardSMP.instance.getRng().nextInt(cardIds.length)]);
                    if (tickCount == 177) {
                        player.playSound(pick, Sound.Emitter.self());
                        CardManager.equipCard(player, new CardInstance(animData.cardType, 0), true);
                        player.sendMessage(Component.text("You were assigned the " + animData.cardType.getName() + " Card.").color(NamedTextColor.GREEN));
                    } else {
                        player.playSound(click, Sound.Emitter.self());
                    }
                }
            }
        }
        for (Player player : players) {
            if (tickCount < 238) {
                PlayerAnimData animData = animDataMap.get(player);
                player.showTitle(Title.title(
                        Component.text(animData.cardType.iconCharFor(0)),
                        Component.text(animData.cardType.getName() + " Card"),
                        0, 20, 0
                ));
            } else if (tickCount < 288) {
                player.showTitle(Title.title(
                        Component.text("Cards").color(NamedTextColor.LIGHT_PURPLE).append(
                                Component.text("SMP").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)),
                        Component.text("Will begin in..."),
                        0, 20, 0
                ));
            } else if (tickCount < 388) {
                int num = 5 - ((tickCount - 288) / 20);
                if ((tickCount - 288) % 20 == 0) player.playSound(click);
                player.showTitle(Title.title(
                        Component.text(num).color(NamedTextColor.RED).decorate(TextDecoration.BOLD),
                        Component.text(""),
                        0, 20, 0
                ));
            } else if (tickCount == 388) {
                player.resetTitle();
                player.playSound(roar, Sound.Emitter.self());
            }
        }
        if (tickCount == 389) active = false;
    }
}
