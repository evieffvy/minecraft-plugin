# HungerGames — LootChest & Airdrop Plugin

A Minecraft plugin for Paper/Purpur 1.21 featuring a loot chest system and airdrops designed for Hunger Games servers.

## Features

- **Loot Chests** — Register chest blocks on the map to automatically fill with random items when opened. Items are divided into Common, Uncommon, and Rare rarities.
- **Airdrops** — Drop airdrop crates at a random location around spawn on a set interval, or trigger one manually at any time.
- Fully customizable loot tables via `config.yml` with no server restart required.

## Requirements

- Java 21+
- Paper or Purpur 1.21

## Installation

1. Build the plugin with Gradle:
   ```bash
   ./gradlew jar
   ```
2. Copy `build/libs/LootChest+Airdrop.jar` into your server's `plugins/` folder.
3. Start or restart the server.
4. Edit `plugins/HungerGames/config.yml` as needed.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/hg listchests` | Everyone | List all registered loot chests |
| `/hg addchest` | `hungergames.admin` | Register the chest you are looking at |
| `/hg removechest` | `hungergames.admin` | Unregister the chest you are looking at |
| `/hg airdrop` | `hungergames.admin` | Trigger an airdrop immediately |
| `/hg reload` | `hungergames.admin` | Reload config.yml without restarting |

> `hungergames.admin` is granted automatically to OP players.

## Configuration

```yaml
airdrop:
  interval-seconds: 300   # every 5 minutes (0 = disable auto)
  radius: 500             # random location within this radius from spawn
  items-count: 5          # number of items in each airdrop crate

rarity-weights:
  common: 60    # %
  uncommon: 30  # %
  # rare = remainder (10%)

items-per-chest:
  min: 3
  max: 6
```

Loot tables can be edited under `loot.common`, `loot.uncommon`, `loot.rare`, and `airdrop.loot`.

## Build from Source

```bash
git clone https://github.com/evieffvy/minecraft-plugin.git
cd minecraft-plugin
./gradlew jar
```

To test locally with a Paper server:

```bash
./gradlew runServer
```
