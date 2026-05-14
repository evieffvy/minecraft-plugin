package com.hungergames.command;

import com.hungergames.HungerGamesPlugin;
import com.hungergames.manager.AirdropManager;
import com.hungergames.manager.LootManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class HGCommand implements CommandExecutor, TabCompleter {

    private final HungerGamesPlugin plugin;
    private final LootManager lootManager;
    private final AirdropManager airdropManager;

    public HGCommand(HungerGamesPlugin plugin, LootManager lootManager, AirdropManager airdropManager) {
        this.plugin = plugin;
        this.lootManager = lootManager;
        this.airdropManager = airdropManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "addchest"    -> handleAddChest(sender);
            case "removechest" -> handleRemoveChest(sender);
            case "listchests"  -> handleListChests(sender);
            case "airdrop"     -> handleAirdrop(sender);
            case "reload"      -> handleReload(sender);
            default -> { sendHelp(sender); yield true; }
        };
    }

    private boolean handleAddChest(CommandSender sender) {
        if (!(sender instanceof Player player)) return true;
        if (!sender.hasPermission("hungergames.admin")) {
            sender.sendMessage(Component.text("ไม่มีสิทธิ์!", NamedTextColor.RED));
            return true;
        }
        var target = player.getTargetBlockExact(5);
        if (target == null || !(target.getState() instanceof org.bukkit.block.Chest)) {
            player.sendMessage(Component.text("มองไปที่กล่อง chest ก่อน (ระยะ 5 block)", NamedTextColor.RED));
            return true;
        }
        boolean added = lootManager.registerChest(target.getLocation());
        if (added) {
            player.sendMessage(Component.text("เพิ่มกล่อง loot แล้ว! รวม " + lootManager.getChestCount() + " กล่อง", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("กล่องนี้ register ไปแล้ว!", NamedTextColor.YELLOW));
        }
        return true;
    }

    private boolean handleRemoveChest(CommandSender sender) {
        if (!(sender instanceof Player player)) return true;
        if (!sender.hasPermission("hungergames.admin")) {
            sender.sendMessage(Component.text("ไม่มีสิทธิ์!", NamedTextColor.RED));
            return true;
        }
        var target = player.getTargetBlockExact(5);
        if (target == null || !(target.getState() instanceof org.bukkit.block.Chest)) {
            player.sendMessage(Component.text("มองไปที่กล่อง chest ก่อน (ระยะ 5 block)", NamedTextColor.RED));
            return true;
        }
        boolean removed = lootManager.unregisterChest(target.getLocation());
        if (removed) {
            player.sendMessage(Component.text("ลบกล่อง loot แล้ว! เหลือ " + lootManager.getChestCount() + " กล่อง", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("กล่องนี้ไม่ได้ register ไว้", NamedTextColor.YELLOW));
        }
        return true;
    }

    private boolean handleListChests(CommandSender sender) {
        int count = lootManager.getChestCount();
        sender.sendMessage(Component.text("กล่อง loot ทั้งหมด: " + count + " กล่อง", NamedTextColor.GOLD));
        lootManager.getChestLocations().forEach(loc ->
            sender.sendMessage(Component.text(
                "  • " + loc.getWorld().getName() + " [" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]",
                NamedTextColor.GRAY
            ))
        );
        return true;
    }

    private boolean handleAirdrop(CommandSender sender) {
        if (!sender.hasPermission("hungergames.admin")) {
            sender.sendMessage(Component.text("ไม่มีสิทธิ์!", NamedTextColor.RED));
            return true;
        }
        airdropManager.trigger(plugin.getServer().getWorlds().get(0));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("hungergames.admin")) {
            sender.sendMessage(Component.text("ไม่มีสิทธิ์!", NamedTextColor.RED));
            return true;
        }
        plugin.reloadConfig();
        lootManager.reloadLootTable();
        airdropManager.reload();
        sender.sendMessage(Component.text("โหลด config.yml ใหม่แล้ว!", NamedTextColor.GREEN));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== HungerGames ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/hg listchests       - ดูรายการกล่อง loot", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("--- Admin ---", NamedTextColor.RED));
        sender.sendMessage(Component.text("/hg addchest    - register กล่อง loot (มองไปที่กล่อง)", NamedTextColor.RED));
        sender.sendMessage(Component.text("/hg removechest - ลบกล่อง loot (มองไปที่กล่อง)", NamedTextColor.RED));
        sender.sendMessage(Component.text("/hg airdrop     - drop กล่อง airdrop ทันที", NamedTextColor.RED));
        sender.sendMessage(Component.text("/hg reload      - โหลด config.yml ใหม่", NamedTextColor.RED));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("addchest", "removechest", "listchests", "airdrop", "reload");
        }
        return List.of();
    }
}
