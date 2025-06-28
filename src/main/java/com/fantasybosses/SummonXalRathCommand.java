package com.fantasybosses;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SummonXalRathCommand implements CommandExecutor {

    private final XalRathBoss xalRathBoss;

    public SummonXalRathCommand(FantasyBosses plugin, XalRathBoss xalRathBoss) {
        this.xalRathBoss = xalRathBoss;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can summon Xal Rath.");
            return true;
        }

        xalRathBoss.spawnBoss(player.getLocation());
        player.sendMessage("💀 Xal Rath descends from the Void...");
        return true;
    }
}
