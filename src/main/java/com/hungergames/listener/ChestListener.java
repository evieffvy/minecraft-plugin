package com.hungergames.listener;

import com.hungergames.manager.LootManager;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class ChestListener implements Listener {

    private final LootManager lootManager;

    public ChestListener(LootManager lootManager) {
        this.lootManager = lootManager;
    }

    @EventHandler
    public void onOpenChest(InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof Chest chest)) return;
        lootManager.fillIfEmpty(chest.getLocation());
    }
}
