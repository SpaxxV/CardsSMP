package com.spaxxv.cardsmp;

import com.destroystokyo.paper.ParticleBuilder;
import com.spaxxv.cardsmp.abilities.AbilityManager;
import com.spaxxv.cardsmp.cards.Cards;
import com.spaxxv.cardsmp.data.DataMap;
import com.spaxxv.cardsmp.data.DataUtils;
import com.spaxxv.cardsmp.data.values.DataBoolean;
import com.spaxxv.cardsmp.data.values.DataInt;
import com.spaxxv.cardsmp.data.values.DataLong;
import com.spaxxv.cardsmp.data.values.DataSubMap;
import com.spaxxv.cardsmp.events.CardSMPEventHandlers;
import com.spaxxv.cardsmp.items.Items;
import com.spaxxv.cardsmp.recipes.RecipeManager;
import com.spaxxv.cardsmp.ritual.RitualManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.util.Random;

public final class CardSMP extends JavaPlugin {
    public static CardSMP instance;
    public static FileConfiguration config;
    public static DataMap pluginData;
    public static DataSubMap getPlayerData(Player player) {
        String uuid = player.getPlayerProfile().getId().toString();
        DataSubMap playerDataMap = (DataSubMap) CardSMP.pluginData.get("player-data");
        if (playerDataMap.hasKey(uuid)) {
            return (DataSubMap) playerDataMap.get(uuid);
        } else {
            DataSubMap playerData = new DataSubMap();
            playerDataMap.set(uuid, playerData);
            return playerData;
        }
    }
    public static boolean incrementHits(Player player, DataSubMap map, int target) {
        String uuid = player.getPlayerProfile().getId().toString();
        if (!map.hasKey(uuid)) {
            map.set(uuid, new DataInt());
        }
        DataInt playerHits = (DataInt) map.get(uuid);
        playerHits.value++;
        boolean targetHit = (playerHits.value == target);
        if (targetHit) playerHits.value = 0;
        return targetHit;
    }

    public NamespacedKey getResourceKey(String key) {
        return new NamespacedKey(this, key);
    }

    public void savePluginData() {
        RitualManager.saveData();
        File dataFile = new File(getDataFolder(), "cardsmp.data");
        try {
            DataUtils.writeMapToFile(pluginData, dataFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPluginData() {
        File dataFile = new File(getDataFolder(), "cardsmp.data");
        if (dataFile.exists()) {
            try {
                pluginData = DataUtils.readMapFromFile(dataFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Random getRng() {
        if (!pluginData.hasKey("rng-seed")) {
            pluginData.set("rng-seed", new DataLong(getServer().getWorlds().getFirst().getSeed()));
        }
        Random rng = new Random(((DataLong) pluginData.get("rng-seed")).value);
        pluginData.set("rng-seed", new DataLong(rng.nextLong()));
        return rng;
    }
    public ParticleBuilder setupParticlesForEntity(Entity entity, ParticleBuilder builder) {
        BoundingBox bb = entity.getBoundingBox();
        Location loc = entity.getLocation().clone();
        loc.add(0, bb.getHeight() / 2, 0);
        builder.location(loc);
        builder.offset(bb.getWidthX() / 2, bb.getHeight() / 2, bb.getWidthZ() / 2);
        return builder;
    }
    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        DataUtils.registerPrimitiveTypes();

        pluginData = new DataMap();
        loadPluginData();
        if (!pluginData.hasKey("player-data")) {
            pluginData.set("player-data", new DataSubMap());
        }
        if (!pluginData.hasKey("server-start")) {
            pluginData.set("server-start", new DataBoolean());
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        CardSMP.config = getConfig();

        Cards.registerCards();

        Items.registerAll();

        RecipeManager.registerRecipes();

        RitualManager.loadData();

        CardSMPCommands.buildCommands();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(CardSMPCommands.mainCommand);
            event.registrar().register(CardSMPCommands.activateCommand);
            event.registrar().register(CardSMPCommands.trustCommand);
            event.registrar().register(CardSMPCommands.boostCommand);
        });

        getServer().getPluginManager().registerEvents(new CardSMPEventHandlers(), this);
    }

    @Override
    public void onDisable() {
        AbilityManager.endAllAbilities();
        savePluginData();
    }
}
