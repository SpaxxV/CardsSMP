package com.spaxxv.cardsmp.ritual;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.cards.CardInstance;
import com.spaxxv.cardsmp.cards.CardManager;
import com.spaxxv.cardsmp.data.values.DataBoolean;
import com.spaxxv.cardsmp.data.values.DataByteArray;
import com.spaxxv.cardsmp.data.values.DataString;
import com.spaxxv.cardsmp.util.MapBuilder;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

public class RitualManager {
    private static final Map<DayOfWeek, Integer> dayLookup = new MapBuilder<DayOfWeek, Integer>()
            .addElement(DayOfWeek.MONDAY, 0)
            .addElement(DayOfWeek.TUESDAY, 1)
            .addElement(DayOfWeek.WEDNESDAY, 2)
            .addElement(DayOfWeek.THURSDAY, 3)
            .addElement(DayOfWeek.FRIDAY, 4)
            .addElement(DayOfWeek.SATURDAY, 5)
            .addElement(DayOfWeek.SUNDAY, 6)
            .toMap();
    private static List<Integer> days;
    private static long start;
    private static long end;
    private static long timezoneOffset;
    private static int duration;
    private static Location location;
    private static boolean active;
    private static boolean enabled;
    private static Ritual activeRitual;
    private static boolean hasLoaded = false;
    private static byte[] serializeLocation(Location l) {
        if (l == null) l = new Location(CardSMP.instance.getServer().getWorlds().getFirst(), 0, 0, 0);

        byte[] worldBytes = new DataString(location.getWorld().getKey().toString()).serialize();
        ByteBuffer buffer = ByteBuffer.allocate(worldBytes.length + 8 + 8 + 8);

        buffer.put(worldBytes);
        buffer.putDouble(l.getX());
        buffer.putDouble(l.getY());
        buffer.putDouble(l.getZ());

        return buffer.array();
    }
    private static Location deserializeLocation(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        World world = CardSMP.instance.getServer().getWorld(NamespacedKey.fromString(new DataString().loadFromBytes(buffer).value));
        double x = buffer.getDouble();
        double y = buffer.getDouble();
        double z = buffer.getDouble();

        return new Location(world, x, y, z);
    }
    private static boolean canActivate() {
        long offsetTime = System.currentTimeMillis() - timezoneOffset;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(offsetTime));
        LocalDate localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        int dayOfWeek = dayLookup.get(localDate.getDayOfWeek());
        long todayTime = offsetTime % 86400000;
        if (days.contains(dayOfWeek)) {
            return (todayTime > start && todayTime < end) && enabled;
        }
        return false;
    }
    public static void tick() {
        if (!active) {
            if (canActivate()) {
                active = true;
                Collection<? extends Player> players = CardSMP.instance.getServer().getOnlinePlayers();
                for (Player player : players) {
                    player.sendMessage(Component.text("The ritual site is now active.").color(NamedTextColor.GOLD));
                }
            }
        } else {
            // Store these values early to only loop all players once per tick during ritual times instead of multiple.
            Collection<? extends Player> players = CardSMP.instance.getServer().getOnlinePlayers();
            BossBar hideBar = null;
            String name = "";
            boolean success = false;

            if (activeRitual != null) {
                activeRitual.remainingTime -= 1;
                activeRitual.bar.progress(1.0f - ((float) activeRitual.remainingTime / duration));
                if (!activeRitual.player.isOnline()) activeRitual.endNextTick = true;
                if (activeRitual.remainingTime == 0 || activeRitual.endNextTick) {
                    hideBar = activeRitual.bar;
                    name = activeRitual.player.getName();
                    if (activeRitual.remainingTime == 0) {
                        success = true;
                        CardInstance card = CardManager.getCard(activeRitual.player);
                        if (card != null) {
                            card.tearLevel -= activeRitual.repairAmount;
                            if (card.tearLevel < 0) card.tearLevel = 0;
                            CardManager.setCardData(activeRitual.player, card);
                        }
                    }
                    activeRitual = null;
                }
            } else if (!canActivate()) {
                active = false;
            }

            for (Player player : players) {
                if (activeRitual != null && player.getLocation().getWorld() == location.getWorld() && player.getLocation().distance(location) < 200) {
                    player.showBossBar(activeRitual.bar);
                } else if (activeRitual != null) {
                    player.hideBossBar(activeRitual.bar);
                } else if (hideBar != null) {
                    player.hideBossBar(hideBar);
                    if (success) {
                        player.sendMessage(Component.text(name + "'s ritual has succeeded.").color(NamedTextColor.GOLD));
                    } else {
                        player.sendMessage(Component.text(name + "'s ritual has failed.").color(NamedTextColor.GOLD));
                    }
                }
                if (!active) player.sendMessage(Component.text("The ritual site is now disabled.").color(NamedTextColor.GOLD));
            }
        }
    }
    public static void loadData() {
        DataByteArray byteArray = (DataByteArray) CardSMP.pluginData.get("ritual-location");
        if (byteArray == null) {
            setLocation(new Location(CardSMP.instance.getServer().getWorlds().getFirst(), 0, 0, 0));
        } else {
            setLocation(deserializeLocation(byteArray.value));
        }
        DataBoolean isEnabled = (DataBoolean) CardSMP.pluginData.get("ritual-enabled");
        if (isEnabled != null) {
            enabled = isEnabled.value;
        } else {
            enabled = false;
        }
        days = CardSMP.config.getIntegerList("cardsmp.ritual.days");
        start = CardSMP.config.getLong("cardsmp.ritual.start-time");
        end = CardSMP.config.getLong("cardsmp.ritual.end-time");
        timezoneOffset = CardSMP.config.getLong("cardsmp.ritual.timezone-offset");
        duration = CardSMP.config.getInt("cardsmp.ritual.duration");
        active = canActivate();
        hasLoaded = true;
    }
    public static void saveData() {
        if (!hasLoaded) return;
        CardSMP.pluginData.set("ritual-location", new DataByteArray(serializeLocation(location)));
        CardSMP.pluginData.set("ritual-enabled", new DataBoolean(enabled));
    }
    public static void setLocation(Location l) {
        location = l;
        saveData();
    }
    public static void markToEnd() {
        if (hasOngoing()) activeRitual.endNextTick = true;
    }
    public static boolean isActive() {
        return active;
    }
    public static void setEnabled(boolean b) {
        enabled = b;
    }
    public static void setRitual(Ritual r) {
        activeRitual = r;
        Collection<? extends Player> players = CardSMP.instance.getServer().getOnlinePlayers();
        String name = r.player.getName();
        for (Player player : players) {
            player.sendMessage(Component.text(name + " has begun a ritual.").color(NamedTextColor.GOLD));
        }
    }
    public static boolean hasOngoing() {
        return activeRitual != null;
    }
    public static int getDuration() {
        return duration;
    }
    public static Location getLocation() {
        return location;
    }
}
