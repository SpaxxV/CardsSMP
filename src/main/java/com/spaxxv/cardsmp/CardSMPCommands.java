package com.spaxxv.cardsmp;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.spaxxv.cardsmp.abilities.*;
import com.spaxxv.cardsmp.cards.CardInstance;
import com.spaxxv.cardsmp.cards.CardManager;
import com.spaxxv.cardsmp.cards.Cards;
import com.spaxxv.cardsmp.data.values.DataBoolean;
import com.spaxxv.cardsmp.items.Item;
import com.spaxxv.cardsmp.items.ItemRegistry;
import com.spaxxv.cardsmp.ritual.RitualManager;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class CardSMPCommands {
    public static LiteralCommandNode<CommandSourceStack> mainCommand;
    public static LiteralCommandNode<CommandSourceStack> activateCommand;
    public static LiteralCommandNode<CommandSourceStack> trustCommand;
    public static LiteralCommandNode<CommandSourceStack> boostCommand;

    public static void buildCommands() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("cards").requires(source -> source.getSender().hasPermission("permission.cardssmp.admin"));
        root.then(Commands.literal("setcard")
                .then(Commands.argument("target", ArgumentTypes.player())
                .then(Commands.argument("cardid", StringArgumentType.word())
                        .suggests(CardSMPCommands::cardSuggestions)
                .then(Commands.argument("tear", IntegerArgumentType.integer(0, 5))
                .executes(context -> {
                    PlayerSelectorArgumentResolver resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = resolver.resolve(context.getSource()).getFirst();

                    String cardID = StringArgumentType.getString(context, "cardid");
                    int tearLevel = IntegerArgumentType.getInteger(context, "tear");

                    if (Cards.cards.containsKey(cardID)) {
                        CardManager.equipCard(target, new CardInstance(Cards.getCard(cardID), tearLevel), true);
                        context.getSource().getSender().sendPlainMessage("Successfully set card!");
                    } else {
                        context.getSource().getSender().sendPlainMessage("Card id does not exist.");
                    }

                    return Command.SINGLE_SUCCESS;
                })))));
        root.then(Commands.literal("seteventcard")
                .then(Commands.argument("target", ArgumentTypes.player())
                .then(Commands.argument("eventcardid", StringArgumentType.word())
                    .suggests(CardSMPCommands::eventCardSuggestions)
                .executes(context -> {
                    PlayerSelectorArgumentResolver resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = resolver.resolve(context.getSource()).getFirst();

                    String cardID = StringArgumentType.getString(context, "eventcardid");

                    if (Cards.eventCards.containsKey(cardID)) {
                        CardManager.equipCard(target, new CardInstance(Cards.getEventCard(cardID), 0), true);
                        context.getSource().getSender().sendPlainMessage("Successfully set event card!");
                    } else {
                        context.getSource().getSender().sendPlainMessage("Event card id does not exist.");
                    }

                    return Command.SINGLE_SUCCESS;
                }))));
        root.then(Commands.literal("removecard")
                .then(Commands.argument("target", ArgumentTypes.player())
                .executes(context -> {
                    PlayerSelectorArgumentResolver resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = resolver.resolve(context.getSource()).getFirst();

                    if (CardManager.removeCardData(target)) {
                        context.getSource().getSender().sendPlainMessage("Successfully removed card!");
                    } else {
                        context.getSource().getSender().sendPlainMessage("Player does not have a card.");
                    }

                    return Command.SINGLE_SUCCESS;
                })));
        root.then(Commands.literal("removeeventcard")
                .then(Commands.argument("target", ArgumentTypes.player())
                .executes(context -> {
                    PlayerSelectorArgumentResolver resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = resolver.resolve(context.getSource()).getFirst();

                    if (CardManager.removeEventCardData(target)) {
                        context.getSource().getSender().sendPlainMessage("Successfully removed event card!");
                    } else {
                        context.getSource().getSender().sendPlainMessage("Player does not have an event card.");
                    }

                    return Command.SINGLE_SUCCESS;
                })));
        root.then(Commands.literal("clearcooldowns")
                .then(Commands.argument("target", ArgumentTypes.player())
                .executes(context -> {
                    PlayerSelectorArgumentResolver resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = resolver.resolve(context.getSource()).getFirst();

                    AbilityManager.clearCooldown(target, AbilitySlot.PASSIVE);
                    AbilityManager.clearCooldown(target, AbilitySlot.ACTIVE1);
                    AbilityManager.clearCooldown(target, AbilitySlot.ACTIVE2);
                    AbilityManager.clearCooldown(target, AbilitySlot.EVENTPASSIVE);
                    AbilityManager.clearCooldown(target, AbilitySlot.EVENT);

                    return Command.SINGLE_SUCCESS;
                })));
        root.then(Commands.literal("giveitem")
                .then(Commands.argument("target", ArgumentTypes.player())
                .then(Commands.argument("itemid", ArgumentTypes.namespacedKey())
                    .suggests(CardSMPCommands::itemSuggestions)
                .executes(context -> {
                    PlayerSelectorArgumentResolver resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = resolver.resolve(context.getSource()).getFirst();

                    Component component = target.displayName();
                    String name = PlainTextComponentSerializer.plainText().serialize(component);

                    NamespacedKey key = context.getArgument("itemid", NamespacedKey.class);

                    Item item = ItemRegistry.getItem(key);
                    if (item != null) {
                        target.give(item.createItemStack());
                        context.getSource().getSender().sendPlainMessage("Gave " + name + " 1x " + key.toString());
                    } else {
                        context.getSource().getSender().sendPlainMessage("CardsSMP Item \"" + key.toString() + "\" does not exist.");
                    }

                    return Command.SINGLE_SUCCESS;
                }))));
        root.then(Commands.literal("info")
                .then(Commands.argument("target", ArgumentTypes.player())
                .executes(context -> {
                    PlayerSelectorArgumentResolver resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = resolver.resolve(context.getSource()).getFirst();

                    Component component = target.displayName();
                    String name = PlainTextComponentSerializer.plainText().serialize(component);
                    context.getSource().getSender().sendPlainMessage("Cards info on " + name + ":");

                    if (CardManager.hasCard(target)) {
                        CardInstance cardInstance = CardManager.getCard(target);
                        assert cardInstance != null;
                        context.getSource().getSender().sendPlainMessage("Card: " + cardInstance.card.getName() + " (" + cardInstance.card.getID() + "), tear level " + cardInstance.tearLevel);
                    }

                    if (CardManager.hasEventCard(target)) {
                        CardInstance cardInstance = CardManager.getEventCard(target);
                        assert cardInstance != null;
                        context.getSource().getSender().sendPlainMessage("Event Card: " + cardInstance.card.getName() + " (" + cardInstance.card.getID() + ")");
                    }

                    dumpAbilityData(context, target, AbilitySlot.PASSIVE);
                    dumpAbilityData(context, target, AbilitySlot.ACTIVE1);
                    dumpAbilityData(context, target, AbilitySlot.ACTIVE2);
                    dumpAbilityData(context, target, AbilitySlot.EVENT);
                    dumpAbilityData(context, target, AbilitySlot.EVENTPASSIVE);

                    context.getSource().getSender().sendPlainMessage("TrustedPlayer data: " + TrustedPlayer.getTrustData(target).toString());

                    return Command.SINGLE_SUCCESS;
                })));
        root.then(Commands.literal("dumpplayerdata")
                .then(Commands.argument("target", ArgumentTypes.player())
                .executes(context -> {
                    PlayerSelectorArgumentResolver resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                    Player target = resolver.resolve(context.getSource()).getFirst();

                    Component component = target.displayName();
                    String name = PlainTextComponentSerializer.plainText().serialize(component);
                    context.getSource().getSender().sendPlainMessage("Persistent data dump on " + name + ":");

                    context.getSource().getSender().sendPlainMessage(CardSMP.getPlayerData(target).toString());

                    return Command.SINGLE_SUCCESS;
                })));
        root.then(Commands.literal("dumpserverdata")
                 .executes(context -> {
                     context.getSource().getSender().sendPlainMessage("Full plugin persistent data dump:");

                     context.getSource().getSender().sendPlainMessage(CardSMP.pluginData.toString());

                     return Command.SINGLE_SUCCESS;
                 }));
        root.then(Commands.literal("runtexturetest")
                .executes(context -> {
                    context.getSource().getSender().sendPlainMessage("Result of \\uE001:");

                    context.getSource().getSender().sendPlainMessage("\n\n\n\n\uE001");

                    return Command.SINGLE_SUCCESS;
                }));
        root.then(Commands.literal("setrituallocation")
                .executes(context -> {
                    RitualManager.setLocation(context.getSource().getLocation().toBlockLocation());

                    context.getSource().getSender().sendPlainMessage("Set ritual site location to your current position.");

                    return Command.SINGLE_SUCCESS;
                }));
        root.then(Commands.literal("serverstart")
                .executes(context -> {
                    if (!((DataBoolean) CardSMP.pluginData.get("server-start")).value) {
                        CardSMPStartAnimation.tickCount = 0;
                        CardSMPStartAnimation.active = true;
                    }

                    return Command.SINGLE_SUCCESS;
                }));
        root.then(Commands.literal("resetstate")
                .executes(context -> {
                    CardSMP.pluginData.set("server-start", new DataBoolean());

                    context.getSource().getSender().sendPlainMessage("Reset start of plugin-data.server-start");

                    return Command.SINGLE_SUCCESS;
                }));
        mainCommand = root.build();
        LiteralArgumentBuilder<CommandSourceStack> activate = Commands.literal("activate");
        activate.then(Commands.literal("ability1").executes(context -> {
            Entity executor = context.getSource().getExecutor();
            if (executor instanceof Player player) {
                CardInstance cardInstance = CardManager.getCard(player);
                if (cardInstance != null) {
                    Ability ability = cardInstance.card.ability1();
                    activateAbility(context, player, cardInstance, ability);
                } else {
                    context.getSource().getSender().sendPlainMessage("You do not have a card!");
                }
            }
            return Command.SINGLE_SUCCESS;
        }));
        activate.then(Commands.literal("ability2").executes(context -> {
            Entity executor = context.getSource().getExecutor();
            if (executor instanceof Player player) {
                CardInstance cardInstance = CardManager.getCard(player);
                if (cardInstance != null) {
                    Ability ability = cardInstance.card.ability2();
                    activateAbility(context, player, cardInstance, ability);
                } else {
                    context.getSource().getSender().sendPlainMessage("You do not have a card!");
                }
            }
            return Command.SINGLE_SUCCESS;
        }));
        activate.then(Commands.literal("event").executes(context -> {
            Entity executor = context.getSource().getExecutor();
            if (executor instanceof Player player) {
                CardInstance cardInstance = CardManager.getEventCard(player);
                if (cardInstance != null) {
                    Ability ability = cardInstance.card.ability1();
                    activateAbility(context, player, cardInstance, ability);
                } else {
                    context.getSource().getSender().sendPlainMessage("You do not have an event card!");
                }
            }
            return Command.SINGLE_SUCCESS;
        }));
        activateCommand = activate.build();
        LiteralArgumentBuilder<CommandSourceStack> trust = Commands.literal("trust");
        trust.then(Commands.literal("add")
                .then(Commands.argument("target", ArgumentTypes.player())
                .executes(context -> {
                    Entity executor = context.getSource().getExecutor();
                    if (executor instanceof Player player) {
                        PlayerSelectorArgumentResolver resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                        Player target = resolver.resolve(context.getSource()).getFirst();
                        Component component = target.displayName();
                        String name = PlainTextComponentSerializer.plainText().serialize(component);
                        if (!TrustedPlayer.hasTrust(player, target)) {
                            TrustedPlayer.addTrust(player, target);
                            context.getSource().getSender().sendPlainMessage("Added " + name + " to your trusted player list.");
                        } else {
                            context.getSource().getSender().sendPlainMessage(name + " is already on your trusted player list.");
                        }
                    }

                    return Command.SINGLE_SUCCESS;
                })));
        trust.then(Commands.literal("remove")
                .then(Commands.argument("target", ArgumentTypes.player())
                .executes(context -> {
                    Entity executor = context.getSource().getExecutor();
                    if (executor instanceof Player player) {
                        PlayerSelectorArgumentResolver resolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
                        Player target = resolver.resolve(context.getSource()).getFirst();
                        Component component = target.displayName();
                        String name = PlainTextComponentSerializer.plainText().serialize(component);
                        if (TrustedPlayer.hasTrust(player, target)) {
                            TrustedPlayer.removeTrust(player, target);
                            context.getSource().getSender().sendPlainMessage("Removed " + name + " from your trusted player list.");
                        } else {
                            context.getSource().getSender().sendPlainMessage(name + " is not on your trusted player list.");
                        }
                    }

                    return Command.SINGLE_SUCCESS;
                })));
        trustCommand = trust.build();
        LiteralArgumentBuilder<CommandSourceStack> boost = Commands.literal("boostinventory");
        boost.executes(context -> {
            Entity executor = context.getSource().getExecutor();
            if (executor instanceof Player player) {
                if (AbilityManager.isActive(player, Cards.BOOST.passive())) {
                    AbilityManager.callTrigger(player, Cards.BOOST.passive().slot);
                }
            }
            return Command.SINGLE_SUCCESS;
        });
        boostCommand = boost.build();
    }

    private static void activateAbility(CommandContext<CommandSourceStack> context, Player player, CardInstance cardInstance, Ability ability) {
        if (!AbilityManager.isActive(player, ability.slot)) {
            if (!AbilityManager.onCooldown(player, ability.slot)) {
                if (cardInstance.tearLevel != 5) {
                    AbilityManager.activateAbility(new CardAbilityInstance(ability, player, cardInstance));
                } else {
                    context.getSource().getSender().sendPlainMessage("Your card is too torn to use it's abilities.");
                }
            } else {
                context.getSource().getSender().sendPlainMessage("Ability is currently on cooldown.");
            }
        } else {
            Ability abilityType = AbilityManager.getAbilityInSlot(player, ability.slot).getAbility();
            if (abilityType.hasTrigger()) {
                AbilityManager.callTrigger(player, ability.slot);
            } else {
                context.getSource().getSender().sendPlainMessage("Ability is currently active.");
            }
        }
    }

    private static void dumpAbilityData(CommandContext<CommandSourceStack> context, Player player, AbilitySlot slot) {
        if (AbilityManager.isActive(player, slot)) {
            AbilityInstance ability = AbilityManager.getAbilityInSlot(player, slot);
            context.getSource().getSender().sendPlainMessage("Ability in slot " + slot + ": " + ability.getAbility().getId() + " r" + ability.remainingTicks + "t");
        }
        if (AbilityManager.onCooldown(player, slot)) {
            int cooldown = AbilityManager.getCooldown(player, slot);
            context.getSource().getSender().sendPlainMessage("Ability slot " + slot + " c" + cooldown + "t");
        }
    }

    private static CompletableFuture<Suggestions> cardSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        for (String id : Cards.cards.keySet()) {
            builder.suggest(id);
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> eventCardSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        for (String id : Cards.eventCards.keySet()) {
            builder.suggest(id);
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> itemSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        for (NamespacedKey id : ItemRegistry.getKeys()) {
            builder.suggest(id.toString());
        }
        return builder.buildFuture();
    }
}
