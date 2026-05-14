package com.hungergames.manager;

import com.hungergames.HungerGamesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LootManager {

    private final HungerGamesPlugin plugin;
    private final List<Location> chestLocations = new ArrayList<>();
    private final Random random = new Random();

    private final File dataFile;
    private FileConfiguration dataConfig;

    private List<ItemStack> commonPool = new ArrayList<>();
    private List<ItemStack> uncommonPool = new ArrayList<>();
    private List<ItemStack> rarePool = new ArrayList<>();
    private int commonWeight;
    private int uncommonWeight;
    private int minItems;
    private int maxItems;

    public LootManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "chests.yml");
        reloadLootTable();
        loadChests();
    }

    public void reloadLootTable() {
        FileConfiguration config = plugin.getConfig();
        commonPool   = loadPool(config, "loot.common");
        uncommonPool = loadPool(config, "loot.uncommon");
        rarePool     = loadPool(config, "loot.rare");
        commonWeight   = config.getInt("rarity-weights.common", 60);
        uncommonWeight = config.getInt("rarity-weights.uncommon", 30);
        minItems = config.getInt("items-per-chest.min", 3);
        maxItems = config.getInt("items-per-chest.max", 6);
        plugin.getLogger().info("โหลด loot table: common=" + commonPool.size()
            + " uncommon=" + uncommonPool.size() + " rare=" + rarePool.size());
    }

    private List<ItemStack> loadPool(FileConfiguration config, String path) {
        List<ItemStack> pool = new ArrayList<>();
        for (Map<?, ?> entry : config.getMapList(path)) {
            Object matObj = entry.get("material");
            Object amtObj = entry.get("amount");
            if (matObj == null) continue;
            Material mat = Material.matchMaterial(matObj.toString());
            if (mat == null) {
                plugin.getLogger().warning("ไม่รู้จัก material: " + matObj);
                continue;
            }
            int amount = amtObj instanceof Number n ? n.intValue() : 1;
            pool.add(new ItemStack(mat, amount));
        }
        return pool;
    }

    public boolean registerChest(Location location) {
        for (Location existing : chestLocations) {
            if (existing.getBlockX() == location.getBlockX()
                && existing.getBlockY() == location.getBlockY()
                && existing.getBlockZ() == location.getBlockZ()
                && existing.getWorld().equals(location.getWorld())) {
                return false;
            }
        }
        chestLocations.add(location.clone());
        saveChests();
        plugin.getLogger().info("Registered chest at " + formatLocation(location));
        return true;
    }

    public boolean unregisterChest(Location location) {
        boolean removed = chestLocations.removeIf(loc ->
            loc.getBlockX() == location.getBlockX()
            && loc.getBlockY() == location.getBlockY()
            && loc.getBlockZ() == location.getBlockZ()
            && loc.getWorld().equals(location.getWorld())
        );
        if (removed) saveChests();
        return removed;
    }

    public void fillIfEmpty(Location location) {
        boolean isRegistered = chestLocations.stream().anyMatch(loc ->
            loc.getBlockX() == location.getBlockX()
            && loc.getBlockY() == location.getBlockY()
            && loc.getBlockZ() == location.getBlockZ()
            && loc.getWorld().equals(location.getWorld())
        );
        if (!isRegistered) return;
        if (!(location.getBlock().getState() instanceof Chest chest)) return;
        if (chest.getInventory().isEmpty()) fillChest(location);
    }

    public void fillAllChests() {
        int filled = 0;
        for (Location loc : chestLocations) {
            if (fillChest(loc)) filled++;
        }
        plugin.getLogger().info("Filled " + filled + "/" + chestLocations.size() + " chests");
    }

    private boolean fillChest(Location location) {
        if (!(location.getBlock().getState() instanceof Chest chest)) return false;

        Inventory inv = chest.getInventory();
        inv.clear();

        int itemCount = minItems + random.nextInt(maxItems - minItems + 1);
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) slots.add(i);
        Collections.shuffle(slots, random);

        for (int i = 0; i < itemCount && i < slots.size(); i++) {
            inv.setItem(slots.get(i), randomLoot());
        }
        return true;
    }

    public void clearAllChests() {
        for (Location loc : chestLocations) {
            if (loc.getBlock().getState() instanceof Chest chest) {
                chest.getInventory().clear();
            }
        }
    }

    private void saveChests() {
        dataConfig = new YamlConfiguration();
        for (int i = 0; i < chestLocations.size(); i++) {
            Location loc = chestLocations.get(i);
            String path = "chests." + i;
            dataConfig.set(path + ".world", loc.getWorld().getName());
            dataConfig.set(path + ".x", loc.getBlockX());
            dataConfig.set(path + ".y", loc.getBlockY());
            dataConfig.set(path + ".z", loc.getBlockZ());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("บันทึก chests.yml ไม่ได้: " + e.getMessage());
        }
    }

    private void loadChests() {
        if (!dataFile.exists()) return;

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        var chestsSection = dataConfig.getConfigurationSection("chests");
        if (chestsSection == null) return;

        int loaded = 0;
        for (String key : chestsSection.getKeys(false)) {
            String path = "chests." + key;
            String worldName = dataConfig.getString(path + ".world");
            var world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World '" + worldName + "' ไม่เจอ ข้ามกล่องนี้");
                continue;
            }
            int x = dataConfig.getInt(path + ".x");
            int y = dataConfig.getInt(path + ".y");
            int z = dataConfig.getInt(path + ".z");
            chestLocations.add(new Location(world, x, y, z));
            loaded++;
        }
        plugin.getLogger().info("โหลดกล่อง loot " + loaded + " กล่อง จาก chests.yml");
    }

    private ItemStack randomLoot() {
        int roll = random.nextInt(100);
        List<ItemStack> pool;
        if (roll < commonWeight) {
            pool = commonPool;
        } else if (roll < commonWeight + uncommonWeight) {
            pool = uncommonPool;
        } else {
            pool = rarePool;
        }
        if (pool.isEmpty()) pool = commonPool;
        if (pool.isEmpty()) return new ItemStack(Material.BREAD);
        return pool.get(random.nextInt(pool.size())).clone();
    }

    private String formatLocation(Location loc) {
        return loc.getWorld().getName() + " [" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]";
    }

    public List<Location> getChestLocations() { return Collections.unmodifiableList(chestLocations); }
    public int getChestCount() { return chestLocations.size(); }
}
