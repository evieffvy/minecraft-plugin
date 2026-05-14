# HungerGames — LootChest & Airdrop Plugin

Minecraft plugin สำหรับ Paper/Purpur 1.21 ระบบจัดการกล่อง loot และ airdrop สำหรับโหมด Hunger Games

## Features

- **Loot Chest** — register chest block ในแผนที่ให้สุ่ม item อัตโนมัติเมื่อเปิด แบ่ง rarity เป็น Common / Uncommon / Rare
- **Airdrop** — drop กล่อง airdrop ลงพื้นที่สุ่มรอบ spawn ตาม interval ที่กำหนด หรือสั่ง manual ได้ทันที
- ปรับแต่ง loot table ได้ทั้งหมดผ่าน `config.yml` โดยไม่ต้อง restart server

## Requirements

- Java 21+
- Paper หรือ Purpur 1.21

## Installation

1. Build plugin ด้วย Gradle:
   ```bash
   ./gradlew jar
   ```
2. คัดลอก `build/libs/LootChest+Airdrop.jar` ไปวางใน `plugins/` ของ server
3. Start/Restart server
4. แก้ไข `plugins/HungerGames/config.yml` ตามต้องการ

## Commands

| Command | Permission | คำอธิบาย |
|---|---|---|
| `/hg listchests` | ทุกคน | ดูรายการกล่อง loot ทั้งหมด |
| `/hg addchest` | `hungergames.admin` | Register chest ที่มองอยู่เป็น loot chest |
| `/hg removechest` | `hungergames.admin` | ลบ loot chest ที่มองอยู่ |
| `/hg airdrop` | `hungergames.admin` | Trigger airdrop ทันที |
| `/hg reload` | `hungergames.admin` | Reload config.yml โดยไม่ restart |

> Permission `hungergames.admin` จะให้อัตโนมัติกับผู้เล่นที่เป็น OP

## Configuration

```yaml
airdrop:
  interval-seconds: 300   # ทุก 5 นาที (0 = ปิด auto)
  radius: 500             # สุ่มใน radius นี้จาก spawn
  items-count: 5          # จำนวน item ใน airdrop chest

rarity-weights:
  common: 60    # %
  uncommon: 30  # %
  # rare = ที่เหลือ (10%)

items-per-chest:
  min: 3
  max: 6
```

แก้ไข loot ได้ในหัวข้อ `loot.common`, `loot.uncommon`, `loot.rare` และ `airdrop.loot`

## Build from Source

```bash
git clone https://github.com/evieffvy/minecraft-plugin.git
cd minecraft-plugin
./gradlew jar
```

ทดสอบ local ด้วย Paper server:

```bash
./gradlew runServer
```
