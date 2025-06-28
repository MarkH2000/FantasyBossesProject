package com.fantasybosses;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SummonGoblinCommand implements CommandExecutor {
    private final GoblinBoss goblinBoss;

    public SummonGoblinCommand(FantasyBosses plugin, GoblinBoss goblinBoss) {
        this.goblinBoss = goblinBoss;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player player) {
            goblinBoss.spawnBoss(player.getLocation());
            player.sendMessage("🧌 Goblin Boss summoned!");
        }
        return true;
    }
}
