package com.fantasybosses;

import org.bukkit.plugin.java.JavaPlugin;

public class FantasyBosses extends JavaPlugin {

    private GoblinBoss goblinBoss;
    private XalRathBoss xalRathBoss;

    @Override
    public void onEnable() {
        goblinBoss = new GoblinBoss(this);
        xalRathBoss = new XalRathBoss(this);

        getServer().getPluginManager().registerEvents(goblinBoss, this);
        getServer().getPluginManager().registerEvents(xalRathBoss, this);

        getCommand("summongoblin").setExecutor(new SummonGoblinCommand(this, goblinBoss));
        getCommand("summonxalrath").setExecutor(new SummonXalRathCommand(this, xalRathBoss));
    }
}
