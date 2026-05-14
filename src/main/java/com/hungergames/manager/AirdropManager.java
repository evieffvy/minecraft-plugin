package com.hungergames.manager;

import com.hungergames.HungerGamesPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AirdropManager {

    private final HungerGamesPlugin plugin;
    private final Random random = new Random();

    private List<ItemStack> airdropPool = new ArrayList<>();
    private int itemCount;
    private int radius;
    private long intervalTicks;

    private BukkitTask timerTask;
    private final List<Location> spawnedChests = new ArrayList<>();

    public AirdropManager(HungerGamesPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();
        airdropPool   = loadPool(config);
        itemCount     = config.getInt("airdrop.items-count", 5);
        radius        = config.getInt("airdrop.radius", 500);
        intervalTicks = (long) config.getInt("airdrop.interval-seconds", 300) * 20;
    }

    public void startTimer(World world) {
        if (intervalTicks <= 0) return;
        timerTask = plugin.getServer().getScheduler().runTaskTimer(
            plugin, () -> trigger(world), intervalTicks, intervalTicks
        );
    }

    public void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void trigger(World world) {
        Location spawn = world.getSpawnLocation();
        int x = spawn.getBlockX() + random.nextInt(radius * 2 + 1) - radius;
        int z = spawn.getBlockZ() + random.nextInt(radius * 2 + 1) - radius;
        int groundY = world.getHighestBlockYAt(x, z);

        // spawn FallingBlock 60 บล็อคเหนือพื้น
        Location startLoc = new Location(world, x + 0.5, groundY + 60, z + 0.5);

        FallingBlock falling = world.spawnFallingBlock(startLoc, Material.CHEST.createBlockData());
        falling.setDropItem(false);
        falling.setHurtEntities(false);

        Location[] lastLoc = { startLoc.clone() };

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!falling.isValid()) {
                    cancel();
                    Block block = lastLoc[0].getBlock();
                    if (!(block.getState() instanceof Chest)) {
                        block.setType(Material.CHEST);
                    }
                    if (block.getState() instanceof Chest chest) {
                        fillInventory(chest.getInventory());
                        spawnedChests.add(block.getLocation().clone());
                    }
                    Location center = block.getLocation().add(0.5, 0.5, 0.5);
                    world.spawnParticle(Particle.EXPLOSION, center, 3);
                    world.playSound(center, Sound.BLOCK_ANVIL_LAND, 1.5f, 0.8f);
                    return;
                }
                lastLoc[0] = falling.getLocation().clone();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void clearChests() {
        for (Location loc : spawnedChests) {
            Block block = loc.getBlock();
            if (block.getState() instanceof Chest chest) {
                chest.getInventory().clear();
            }
            block.setType(Material.AIR);
        }
        spawnedChests.clear();
    }

    private void fillInventory(Inventory inv) {
        inv.clear();
        List<ItemStack> shuffled = new ArrayList<>(airdropPool);
        Collections.shuffle(shuffled, random);
        for (int i = 0; i < Math.min(itemCount, shuffled.size()); i++) {
            inv.addItem(shuffled.get(i).clone());
        }
    }

    private List<ItemStack> loadPool(FileConfiguration config) {
        List<ItemStack> pool = new ArrayList<>();
        for (Map<?, ?> entry : config.getMapList("airdrop.loot")) {
            Object matObj = entry.get("material");
            Object amtObj = entry.get("amount");
            if (matObj == null) continue;
            Material mat = Material.matchMaterial(matObj.toString());
            if (mat == null) {
                plugin.getLogger().warning("airdrop: ไม่รู้จัก material: " + matObj);
                continue;
            }
            int amount = amtObj instanceof Number n ? n.intValue() : 1;
            pool.add(new ItemStack(mat, amount));
        }
        return pool;
    }
}
