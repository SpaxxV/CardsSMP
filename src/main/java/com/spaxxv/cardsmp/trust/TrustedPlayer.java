package com.spaxxv.cardsmp.trust;

import com.spaxxv.cardsmp.CardSMP;
import com.spaxxv.cardsmp.data.values.DataBoolean;
import com.spaxxv.cardsmp.data.values.DataSubMap;
import org.bukkit.entity.Player;

public class TrustedPlayer {
    public static DataSubMap getTrustData(Player player) {
        DataSubMap playerData = CardSMP.getPlayerData(player);
        if (playerData.hasKey("trusted-players")) {
            return (DataSubMap) playerData.get("trusted-players");
        } else {
            DataSubMap trustData = new DataSubMap();
            playerData.set("trusted-players", trustData);
            return trustData;
        }
    }
    public static void addTrust(Player truster, Player trustee) {
        DataSubMap data = getTrustData(truster);
        String uuid = trustee.getPlayerProfile().getId().toString();
        data.set(uuid, new DataBoolean(true));
    }
    public static void removeTrust(Player truster, Player trustee) {
        DataSubMap data = getTrustData(truster);
        String uuid = trustee.getPlayerProfile().getId().toString();
        data.remove(uuid);
    }
    public static boolean hasTrust(Player truster, Player trustee) {
        DataSubMap data = getTrustData(truster);
        String uuid = trustee.getPlayerProfile().getId().toString();
        return data.hasKey(uuid);
    }
}
