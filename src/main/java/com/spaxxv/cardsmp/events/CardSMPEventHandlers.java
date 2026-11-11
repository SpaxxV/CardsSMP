package com.spaxxv.cardsmp.events;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.CardSMPStartAnimation;
import com.spaxxv.cardsmp.abilities.AbilityInstance;
import com.spaxxv.cardsmp.abilities.AbilityManager;
import com.spaxxv.cardsmp.cards.Card;
import com.spaxxv.cardsmp.cards.CardInstance;
import com.spaxxv.cardsmp.cards.CardManager;
import com.spaxxv.cardsmp.cards.Cards;
import com.spaxxv.cardsmp.cards.classes.BoostCard;
import com.spaxxv.cardsmp.data.values.DataBoolean;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.data.values.DataString;
import com.spaxxv.cardsmp.data.values.DataSubMap;
import com.spaxxv.cardsmp.items.Item;
import com.spaxxv.cardsmp.items.ItemRegistry;
import com.spaxxv.cardsmp.items.Items;
import com.spaxxv.cardsmp.items.classes.EventCardItem;
import com.spaxxv.cardsmp.recipes.Matchable;
import com.spaxxv.cardsmp.recipes.RecipeManager;
import com.spaxxv.cardsmp.ritual.Ritual;
import com.spaxxv.cardsmp.ritual.RitualItem;
import com.spaxxv.cardsmp.ritual.RitualManager;
import com.spaxxv.cardsmp.trust.TrustedPlayer;
import com.spaxxv.cardsmp.util.CustomInventory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Random;

public class CardSMPEventHandlers implements Listener {

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        CardSMP.instance.savePluginData();
    }

    @EventHandler
    public void onTick(ServerTickStartEvent event) {
        if (CardSMPStartAnimation.active) CardSMPStartAnimation.tick();
        RitualManager.tick();
        AbilityManager.tickAbilities();
        Collection<? extends Player> players = CardSMP.instance.getServer().getOnlinePlayers();
        for (Player player : players) {
            Component toolbar = AbilityManager.generateToolbar(player);
            if (toolbar != null) {
                player.sendActionBar(toolbar);
            }
            PersistentDataContainer playerPDC = player.getPersistentDataContainer();
            NamespacedKey frozenKey = CardSMP.instance.getResourceKey("frozen-movement");
            if (playerPDC.has(frozenKey)) {
                int frozenTick = playerPDC.get(frozenKey, PersistentDataType.INTEGER);
                if (frozenTick == 0) {
                    playerPDC.remove(frozenKey);
                } else {
                    playerPDC.set(frozenKey, PersistentDataType.INTEGER, playerPDC.get(frozenKey, PersistentDataType.INTEGER) - 1);
                }
            }
            BoostCard boost = (BoostCard) Cards.BOOST;
            if (playerPDC.has(boost.boostTickKey)) {
                int boostTick = playerPDC.get(boost.boostTickKey, PersistentDataType.INTEGER);
                if (boostTick == 0) {
                    playerPDC.remove(boost.boostTickKey);
                    playerPDC.remove(boost.damageBoostKey);
                } else {
                    playerPDC.set(boost.boostTickKey, PersistentDataType.INTEGER, playerPDC.get(boost.boostTickKey, PersistentDataType.INTEGER) - 1);
                }
            }
        }
        for (World world : CardSMP.instance.getServer().getWorlds()) {
            for (org.bukkit.entity.Item itemEntity : world.getEntitiesByClass(org.bukkit.entity.Item.class)) {
                Item item = ItemRegistry.getItemFrom(itemEntity.getItemStack());
                if (item instanceof EventCardItem && itemEntity.getLocation().getY() < (world.getMinHeight() + (world.getVoidDamageMinBuildHeightOffset() / 2f))) {
                    itemEntity.setVelocity(new Vector(0, 0, 0));
                    itemEntity.teleport(new Location(CardSMP.instance.getServer().getWorld(NamespacedKey.minecraft("the_end")), 100, 50, 0));
                }
            }
        }
    }

    @EventHandler
    public void onPreCraft(PrepareItemCraftEvent event) {
        boolean result = RecipeManager.handleRecipeEvent(event);
        if (!result && event.getInventory().getResult() != null) {
            for (ItemStack itemStack : event.getInventory().getMatrix()) {
                if (itemStack != null && ItemRegistry.isCustom(itemStack)) {
                    event.getInventory().setResult(null);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        RecipeManager.handleCraftEvent(event);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        if (itemStack != null) {
            Item item = ItemRegistry.getItemFrom(itemStack);
            if (item != null) {
                item.onInteract(event);
            }
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null) {
                Location blockLocation = block.getLocation();
                Location ritualLocation = RitualManager.getLocation();
                if (blockLocation.getWorld() == ritualLocation.getWorld()) {
                    if (
                            blockLocation.getBlockX() == ritualLocation.getBlockX() &&
                            blockLocation.getBlockY() == ritualLocation.getBlockY() &&
                            blockLocation.getBlockZ() == ritualLocation.getBlockZ()
                    ) {
                        event.setCancelled(true);
                        if (!RitualManager.hasOngoing()) {
                            if (RitualManager.isActive()) {
                                CardInstance card = CardManager.getCard(event.getPlayer());
                                if (card != null && card.tearLevel > 0) {
                                    RitualItem[] ritualItems = new RitualItem[5];
                                    ItemStack[] items = event.getPlayer().getInventory().getContents();
                                    int ri = 0;
                                    boolean netherStar = false;
                                    int heads = 0;
                                    for (ItemStack item : items) {
                                        if (item == null) continue;
                                        if (heads < 4 && item.getType() == Material.PLAYER_HEAD) {
                                            int req = (4 - heads);
                                            if (item.getAmount() >= req) {
                                                heads = 4;
                                                ritualItems[ri] = new RitualItem(item, req);
                                            } else {
                                                heads += item.getAmount();
                                                ritualItems[ri] = new RitualItem(item, item.getAmount());
                                            }
                                            ri++;
                                        }
                                        if (!netherStar && item.getType() == Material.NETHER_STAR) {
                                            netherStar = true;
                                            ritualItems[ri] = new RitualItem(item, 1);
                                            ri++;
                                        }
                                    }
                                    if (netherStar && heads == 4) {
                                        for (int i = 0; i < ri; i++) {
                                            ritualItems[i].doRemove();
                                        }
                                        RitualManager.setRitual(new Ritual(event.getPlayer(), 5));
                                    } else {
                                        RitualItem ritualItem = null;
                                        for (ItemStack item : items) {
                                            if (item != null & ItemRegistry.isCustom(item) && ItemRegistry.getItemFrom(item) == Items.CARD_TAPE) {
                                                ritualItem = new RitualItem(item, 1);
                                                break;
                                            }
                                        }
                                        if (ritualItem != null) {
                                            ritualItem.doRemove();
                                            RitualManager.setRitual(new Ritual(event.getPlayer(), 1));
                                        } else {
                                            event.getPlayer().sendMessage(Component.text("You are missing the required materials to begin a ritual!").color(NamedTextColor.RED));
                                        }
                                    }
                                } else {
                                    event.getPlayer().sendMessage(Component.text("The ritual altar deems you have no need for a ritual!").color(NamedTextColor.RED));
                                }
                            } else {
                                event.getPlayer().sendMessage(Component.text("The ritual site is not active!").color(NamedTextColor.RED));
                            }
                        } else {
                            event.getPlayer().sendMessage(Component.text("You cannot start a ritual while one is in progress!").color(NamedTextColor.RED));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Item itemEntity) {
            Item item = ItemRegistry.getItemFrom(itemEntity.getItemStack());
            if (item instanceof EventCardItem) event.setCancelled(true);
        }
        if (event.getEntity() instanceof Player player) {
            if (event.getDamageSource().getDamageType() == DamageType.FALL || event.getDamageSource().getDamageType() == DamageType.ENDER_PEARL) {
                if (AbilityManager.isActive(player, Cards.SKY.passive())) event.setCancelled(true);
                if (AbilityManager.isActive(player, Cards.BREEZE.ability2())) event.setCancelled(true);
                if (AbilityManager.isActive(player, Cards.CONDUCTOR.ability2())) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityAttacked(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getDamager() instanceof Player attacker) {
                if (!TrustedPlayer.hasTrust(attacker, player)) {
                    if (AbilityManager.isActive(attacker, Cards.OCEAN.ability1())) {
                        AbilityInstance ability = AbilityManager.getAbilityInstance(attacker, Cards.OCEAN.ability1());
                        String uuid = player.getPlayerProfile().getId().toString();

                        DataSubMap abilityAttackData = (DataSubMap) ability.data.get("player-hits");
                        DataSubMap abilityAffectedData = (DataSubMap) ability.data.get("affected-players");

                        if (CardSMP.incrementHits(player, abilityAttackData, Cards.OCEAN.getConfigInt("ability-1.hit-amount"))) {
                            abilityAffectedData.set(uuid, new DataInt());
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Cards.OCEAN.getConfigInt("ability-1.effect-duration"), 0));
                        }
                    }
                    if (AbilityManager.isActive(attacker, Cards.CONDUCTOR.ability1())) {
                        AbilityInstance ability = AbilityManager.getAbilityInstance(attacker, Cards.CONDUCTOR.ability1());

                        DataSubMap abilityAttackData = (DataSubMap) ability.data.get("player-hits");

                        if (CardSMP.incrementHits(player, abilityAttackData, Cards.CONDUCTOR.getConfigInt("ability-1.hit-amount"))) {
                            event.setDamage(event.getDamage() + Cards.CONDUCTOR.getConfigDouble("ability-1.damage"));
                            player.getWorld().strikeLightningEffect(player.getLocation());
                        }
                    }
                    if (AbilityManager.isActive(attacker, Cards.CONDUCTOR.ability2()) && event.getDamageSource().getDamageType() == DamageType.TRIDENT) {
                        AbilityInstance ability = AbilityManager.getAbilityInstance(attacker, Cards.CONDUCTOR.ability2());
                        DataInt stage = (DataInt) ability.data.get("stage");
                        if (stage.value == 1) {
                            stage.value = 2;
                            event.setDamage(Cards.CONDUCTOR.getConfigDouble("ability-2.damage"));
                            ability.markForRemoval();
                        }
                    }
                    if (AbilityManager.isActive(attacker, Cards.FREEZE.ability1())) {
                        AbilityInstance ability = AbilityManager.getAbilityInstance(attacker, Cards.FREEZE.ability1());

                        DataSubMap abilityAttackData = (DataSubMap) ability.data.get("player-hits");

                        if (CardSMP.incrementHits(player, abilityAttackData, Cards.FREEZE.getConfigInt("ability-1.hit-amount"))) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Cards.FREEZE.getConfigInt("ability-1.effect-duration"), 1));
                        }
                    }
                    if (AbilityManager.isActive(attacker, Cards.FREEZE.ability2())) {
                        AbilityInstance ability = AbilityManager.getAbilityInstance(attacker, Cards.FREEZE.ability2());
                        DataString uuidString = (DataString) ability.data.get("targeted-player");
                        if (uuidString.value.isEmpty()) {
                            uuidString.value = player.getPlayerProfile().getId().toString();
                            ability.remainingTicks = Cards.FREEZE.getConfigInt("ability-2.effect-duration");
                            player.getPersistentDataContainer().set(CardSMP.instance.getResourceKey("frozen-movement"), PersistentDataType.INTEGER, Cards.FREEZE.getConfigInt("ability-2.effect-duration"));
                        }
                    }
                    if (AbilityManager.isActive(attacker, Cards.MEDIC.ability2())) {
                        AbilityInstance ability = AbilityManager.getAbilityInstance(attacker, Cards.MEDIC.ability2());
                        String uuid = player.getPlayerProfile().getId().toString();

                        DataSubMap abilityAttackData = (DataSubMap) ability.data.get("player-hits");
                        DataSubMap abilityAffectedData = (DataSubMap) ability.data.get("affected-players");
                        DataInt gain = (DataInt) ability.data.get("gain");

                        if (CardSMP.incrementHits(player, abilityAttackData, Cards.MEDIC.getConfigInt("ability-2.hit-amount"))) {
                            DataInt lost = new DataInt();
                            if (!abilityAffectedData.hasKey(uuid)) {
                                abilityAffectedData.set(uuid, lost);
                            } else {
                                lost = (DataInt) abilityAffectedData.get(uuid);
                            }
                            gain.value += 2;
                            lost.value -= 2;
                            if (lost.value < -18) lost.value = -18;
                            if (attacker.getAttribute(Attribute.MAX_HEALTH) == null) attacker.registerAttribute(Attribute.MAX_HEALTH);
                            if (player.getAttribute(Attribute.MAX_HEALTH) == null) player.registerAttribute(Attribute.MAX_HEALTH);
                            AttributeInstance aMaxHealth = attacker.getAttribute(Attribute.MAX_HEALTH);
                            if (aMaxHealth.getModifier(CardSMP.instance.getResourceKey("medic-gained")) != null) {
                                aMaxHealth.removeModifier(CardSMP.instance.getResourceKey("medic-gained"));
                            }
                            aMaxHealth.addModifier(new AttributeModifier(CardSMP.instance.getResourceKey("medic-gained"), gain.value, AttributeModifier.Operation.ADD_NUMBER));
                            attacker.setHealth(attacker.getHealth() + 2);
                            AttributeInstance pMaxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                            if (pMaxHealth.getModifier(CardSMP.instance.getResourceKey("medic-stolen")) != null) {
                                pMaxHealth.removeModifier(CardSMP.instance.getResourceKey("medic-stolen"));
                            }
                            pMaxHealth.addModifier(new AttributeModifier(CardSMP.instance.getResourceKey("medic-stolen"), lost.value, AttributeModifier.Operation.ADD_NUMBER));
                            if (player.getHealth() > pMaxHealth.getValue()) player.setHealth(pMaxHealth.getValue());
                        }
                    }
                    if (AbilityManager.isActive(attacker, Cards.BOOST.ability1())) {
                        if (CardSMP.instance.getRng().nextDouble() > Cards.BOOST.getConfigDouble("ability-1.steal-chance")) {
                            int stealAmount = Cards.BOOST.getConfigInt("ability-1.steal-amount");
                            if (player.getTotalExperience() < stealAmount) stealAmount = player.getTotalExperience();
                            player.setTotalExperience(player.getTotalExperience() - stealAmount);
                            attacker.setTotalExperience(attacker.getTotalExperience() + stealAmount);
                        }
                    }
                }
                if (!TrustedPlayer.hasTrust(player, attacker)) {
                    if (AbilityManager.isActive(player, Cards.GHOST.passive())) {
                        Random rng = CardSMP.instance.getRng();
                        if (rng.nextDouble() < Cards.GHOST.getConfigDouble("passive.chance")) {
                            event.setCancelled(true);
                            attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Cards.GHOST.getConfigInt("passive.effect-duration"), 0));
                        }
                    }
                }
                PersistentDataContainer attackerPDC = attacker.getPersistentDataContainer();
                BoostCard boost = (BoostCard) Cards.BOOST;
                if (attackerPDC.has(boost.damageBoostKey)) {
                    event.setDamage(event.getDamage() + attackerPDC.get(boost.damageBoostKey, PersistentDataType.DOUBLE));
                }
            }
        }
    }

//    @EventHandler
//    public void onEntityEffect(EntityPotionEffectEvent event) {
//        if (event.getEntity() instanceof Player player) {
//            if (event.getAction() == EntityPotionEffectEvent.Action.ADDED || (event.getAction() == EntityPotionEffectEvent.Action.CHANGED && event.isOverride())) {
//                if (event.getModifiedType() == PotionEffectType.INVISIBILITY) {
//                    if (AbilityManager.isActive(player, Cards.GHOST.passive())) {
//                        if (!player.getPersistentDataContainer().has(CardSMP.instance.getResourceKey("ghost_increase_bypass")) || Boolean.FALSE.equals(player.getPersistentDataContainer().get(CardSMP.instance.getResourceKey("ghost_increase_bypass"), PersistentDataType.BOOLEAN))) {
//                            event.setCancelled(true);
//                            player.getPersistentDataContainer().set(CardSMP.instance.getResourceKey("ghost_increase_bypass"), PersistentDataType.BOOLEAN, true);
//                            PotionEffect effect = event.getNewEffect();
//                            player.addPotionEffect(effect.withDuration(effect.getDuration() + Cards.GHOST.getConfigInt("passive.increase")));
//                        } else {
//                            player.getPersistentDataContainer().set(CardSMP.instance.getResourceKey("ghost_increase_bypass"), PersistentDataType.BOOLEAN, false);
//                        }
//                    }
//                }
//            }
//        }
//    }

    @EventHandler
    public void onEntityHeal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (AbilityManager.isActive(player, Cards.MEDIC.passive())) {
                event.setAmount(event.getAmount() * Cards.MEDIC.getConfigDouble("passive.multiplier"));
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        Item item = ItemRegistry.getItemFrom(event.getEntity().getItemStack());

        if (item instanceof EventCardItem) {
            event.getEntity().setUnlimitedLifetime(true);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            if (!((Entity) event.getPlayer()).isOnGround()) {
                if (AbilityManager.isActive(event.getPlayer(), Cards.BREEZE.passive())) {
                    AbilityInstance abilityInstance = AbilityManager.getAbilityInstance(event.getPlayer(), Cards.BREEZE.passive());
                    DataBoolean canUse = (DataBoolean) abilityInstance.data.get("can-use");
                    if (canUse.value) {
                        canUse.value = false;
                        Vector current = event.getPlayer().getVelocity();
                        double speed = Cards.BREEZE.getConfigDouble("passive.speed-multiplier");
                        event.getPlayer().setVelocity(new Vector(current.getX() * speed, Cards.BREEZE.getConfigDouble("passive.jump-velocity"), current.getZ() * speed));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            if (event.getInventory().getHolder() != null && event.getInventory().getHolder().getClass() == CustomInventory.class) {
                if (AbilityManager.isActive(player, Cards.BOOST.passive())) {
                    BoostCard boostCard = (BoostCard) Cards.BOOST;
                    boostCard.saveToBoostInventory(player.getPersistentDataContainer(), event.getInventory().getContents());
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer().getPersistentDataContainer().has(CardSMP.instance.getResourceKey("frozen-movement"))) event.setCancelled(true);
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        if (event.getPlayer().getAttribute(Attribute.MAX_HEALTH) == null) event.getPlayer().registerAttribute(Attribute.MAX_HEALTH);
        AttributeInstance maxHealth = event.getPlayer().getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth.getModifier(CardSMP.instance.getResourceKey("medic-stolen")) != null) {
            maxHealth.removeModifier(CardSMP.instance.getResourceKey("medic-stolen"));
        }
        AbilityManager.endPlayerAbilities(event.getPlayer(), true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Entity attacker;
        if ((attacker = event.getDamageSource().getCausingEntity()) != null) {
            if (attacker instanceof Player attackingPlayer) {
                if (AbilityManager.isActive(attackingPlayer, Cards.BOUNTY.passive()) || CardSMP.instance.getRng().nextDouble() > CardSMP.config.getDouble("cardsmp.head-drop-chance")) {
                    Location deathLocation = event.getPlayer().getLocation();
                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
                    playerHead.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(event.getPlayer().getPlayerProfile()));
                    deathLocation.getWorld().dropItem(deathLocation, playerHead);
                }
                CardInstance cardInstance = CardManager.getCard(event.getPlayer());
                if (cardInstance != null && cardInstance.tearLevel < 5) {
                    cardInstance.tearLevel++;
                    CardManager.setCardData(event.getPlayer(), cardInstance);
                }
            }
        }
        InventoryHolder holder = event.getPlayer().getOpenInventory().getTopInventory().getHolder();
        if (holder != null && holder.getClass() == CustomInventory.class) {
            event.getPlayer().closeInventory(InventoryCloseEvent.Reason.DEATH);
        }
        AbilityManager.endPlayerAbilities(event.getPlayer(), false);
    }

    @EventHandler
    public void onRespawn(PlayerPostRespawnEvent event) {
        CardManager.activatePassive(event.getPlayer());
        CardManager.activateEventPassive(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (((DataBoolean) CardSMP.pluginData.get("server-start")).value) {
            if (!CardManager.hasCard(event.getPlayer())) {
                Card type = Cards.getRandomCard(CardSMP.instance.getRng());
                CardManager.equipCard(event.getPlayer(), new CardInstance(type, 0), true);
                event.getPlayer().sendMessage(Component.text("You were assigned the " + type.getName() + " Card.").color(NamedTextColor.GREEN));
            }
        }
        CardManager.activatePassive(event.getPlayer());
        CardManager.activateEventPassive(event.getPlayer());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Location breakLocation = event.getBlock().getLocation();
        Location ritualLocation = RitualManager.getLocation();
        if (breakLocation.getWorld() == ritualLocation.getWorld()) {
            if (
                    breakLocation.getBlockX() == ritualLocation.getBlockX() &&
                    breakLocation.getBlockY() == ritualLocation.getBlockY() &&
                    breakLocation.getBlockZ() == ritualLocation.getBlockZ()
            ) {
                event.setCancelled(true);
                if (RitualManager.hasOngoing()) {
                    RitualManager.markToEnd();
                }
            }
        }
    }
}
