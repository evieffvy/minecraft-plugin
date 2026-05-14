package com.hungergames;

import com.hungergames.command.HGCommand;
import com.hungergames.listener.ChestListener;
import com.hungergames.manager.AirdropManager;
import com.hungergames.manager.LootManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class HungerGamesPlugin extends JavaPlugin {

    private LootManager lootManager;
    private AirdropManager airdropManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        lootManager    = new LootManager(this);
        airdropManager = new AirdropManager(this);

        getServer().getPluginManager().registerEvents(new ChestListener(lootManager), this);
        airdropManager.startTimer(getServer().getWorlds().get(0));

        var hgCommand = getCommand("hg");
        if (hgCommand != null) {
            var executor = new HGCommand(this, lootManager, airdropManager);
            hgCommand.setExecutor(executor);
            hgCommand.setTabCompleter(executor);
        }

        getLogger().info("HungerGames enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HungerGames disabled!");
    }

    public LootManager getLootManager() { return lootManager; }
    public AirdropManager getAirdropManager() { return airdropManager; }
}
