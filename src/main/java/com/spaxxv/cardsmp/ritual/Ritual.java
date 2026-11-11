package com.spaxxv.cardsmp.ritual;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class Ritual {
    public Player player;
    public int remainingTime;
    public BossBar bar;
    public boolean endNextTick;
    public int repairAmount;
    public Ritual(Player p, int rep) {
        player = p;
        remainingTime = RitualManager.getDuration();
        bar = BossBar.bossBar(Component.text("Ritual"), 0, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        endNextTick = false;
        repairAmount = rep;
    }
}
