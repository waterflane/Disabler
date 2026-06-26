# Disabler

A server-side mod that lets server admins block specific mob spawns, biomes, dimensions, and structure generation through a simple config file. No GUI is required, and config changes are picked up on the next world load.

- **Block mob spawns** — remove mobs from biome spawn lists and cancel runtime spawn attempts
- **Block biomes** — strip their mob spawns, carvers, features, and structures from world generation; blocked biomes are replaced with allowed alternatives at runtime
- **Block structures** — prevent entire structure types from generating and strip their mob spawn overrides
- **Block items** — remove configured items from generated loot, pickups, player inventories, ender chests, and open containers
- Config-driven: plain JSON file, no commands or GUI needed
- Server-side only (no client install required)

## Requirements

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x |

## Configuration

The config file is located at:
```
.minecraft/config/disabler-server.json
```

```json
{
  "blocked_mobs": [],
  "blocked_structures": [],
  "blocked_biomes": [],
  "blocked_items": [],
  "storage_scan": {
    "enabled": true,
    "interval_ticks": 300,
    "block_entities_per_tick": 512,
    "skipped_namespaces": [
      "lootr"
    ]
  },
  "biome_exceptions": [
    "minecraft:mushroom_fields"
  ]
}
```

### Key Points

- **Biome Exceptions**: By default, `minecraft:mushroom_fields` is listed in `biome_exceptions`. This prevents it from being used as a replacement for blocked biomes, since mushroom biomes have special generation requirements (e.g., spawning only on islands). You can remove it from this list if you want mushroom biomes to be used as replacements.
- **All lists can be empty**: Leave any list empty (`[]`) to disable that feature entirely.
- **Config is written to disk**: When the mod generates `disabler-server.json` for the first time, it includes the default `minecraft:mushroom_fields` in the exceptions list. If an old `disabler-server.toml` exists and JSON does not, known list keys are migrated into the new JSON file.
- **JSON reload timing**: The config is loaded during mod initialization and reloaded when the server/world is about to start.
- **Storage Scan**: `storage_scan.enabled` allows blocked items to be removed from loaded block entity inventories exposed through NeoForge item capabilities. `interval_ticks` defaults to 300 ticks (about 15 seconds), and `block_entities_per_tick` limits how many loaded block entities are processed per tick while a scan is running.
- **Lootr Compatibility**: `storage_scan.skipped_namespaces` contains `lootr` by default, so Lootr chests, barrels, shulker boxes, frames, and similar per-player loot containers are not touched as normal inventories. Lootr-generated per-player inventories are filtered separately when they are created, so items listed in `blocked_items` are removed without replacing Lootr containers with vanilla ones.

### Biome Blocking Implementation

When a biome is blocked via the config, the following happens:

1. **World Generation Phase** (`ConfigDrivenBiomeModifier`):
   - All mob spawn entries are **cleared** from the blocked biome's spawn settings
   - Carvers, features, and structures associated with that biome are removed
   - The biome is excluded from structure generation filters

2. **Runtime Biome Replacement** (`MultiNoiseBiomeSourceMixin`):
   - During world generation, when a blocked biome would be selected via `getNoiseBiome()`, it's intercepted
   - The biome is replaced with an **allowed alternative** from the same biome parameter list
   - Replacement stays inside the current dimension because the candidate pool is collected from that dimension's own `MultiNoiseBiomeSource` (Nether uses only Nether biomes, Overworld uses only Overworld biomes, etc.)
   - **Biome exceptions**: Biomes listed in `biome_exceptions` config section are **excluded from the replacement pool**. By default, `minecraft:mushroom_fields` is included in this list to prevent it from replacing blocked biomes (since it has special island-only generation requirements).
   - Allowed biomes are collected once and cached in memory for performance
   - If no allowed biomes exist (including exceptions), the blocked biome is kept as fallback (prevents empty world generation)

3. **Structure Filtering** (`ConfigDrivenStructureModifier`):
   - Blocked structures don't generate at all
   - Structures are filtered to only generate in **non-blocked biomes**
   - Mob spawn overrides are removed from blocked structures
   - If a structure can only generate in blocked biomes, it's completely disabled

### Mob Blocking Implementation

Blocked mobs are handled at two runtime points:

1. **Spawn List Cleanup** (via `BiomeModifier`):
   - Blocked mobs are removed from all biome spawn lists during world generation
   - This prevents them from being scheduled for spawning

2. **Runtime Spawn Cancellation** (`MobSpawnBlocker`):
   - **`FinalizeSpawnEvent`**: When a mob spawn attempt is finalized, if the mob type is blocked, the spawn is cancelled
   - **`EntityJoinLevelEvent`**: If a blocked mob somehow spawns (e.g., from NBT data, commands, or creative mode), it's immediately cancelled on server-side only
   - Client-side spawns and disk-loaded entities are not affected

### Item Blocking Implementation

Blocked items are handled without scanning every player every tick:

1. **Loot Table Filtering** (`ConfigDrivenLootModifier`):
   - A global NeoForge loot modifier removes blocked items from generated loot lists
   - This applies to vanilla, modded, and datapack loot tables
   - Lootr per-player inventories are also cleaned at creation time through optional Lootr compatibility mixins

2. **Runtime Item Prevention** (`ItemBlocker`):
   - Ground item entities with blocked items are cancelled before joining the level
   - Blocked item pickups are denied before the stack enters the player inventory
   - Tossed blocked items are removed instead of staying in the world

3. **Inventory Cleanup** (`BlockedItemCleaner`):
   - Player inventories, armor, offhand, ender chests, carried cursor stacks, and open containers are cleaned on login, respawn, dimension change, container open/close, crafting, and smelting
   - A lightweight safety sweep runs once every 100 server ticks (5 seconds), not every tick

4. **Storage Inventory Cleanup** (`StorageInventoryScanner`):
   - When enabled, loaded block entity inventories are checked through NeoForge `ItemHandler` capabilities
   - This covers many technical and magic mod storages without hardcoding mod ids
   - Scans are interval-based and processed in batches to avoid one large server tick spike
   - Namespaces listed in `storage_scan.skipped_namespaces` are skipped before any inventory/capability access

### Structure Blocking Implementation

Blocked structures are handled as follows:

1. **Structure Generation Prevention**:
   - Blocked structures are filtered out during world generation
   - Their `StructureSettings` are cleared (no biomes, no spawn overrides)

2. **Biome Filtering for Structures**:
   - Structures are checked against blocked biome list
   - If a structure can only generate in blocked biomes, it's completely disabled
   - Otherwise, it's allowed to generate in non-blocked biomes only

3. **Mob Spawn Override Removal**:
   - Mob spawn overrides for blocked structures are removed
   - This prevents blocked mobs from spawning in structures even if the structure itself isn't blocked

### Finding resource IDs

- **Mobs**: Use `/summon <tab>` in-game or check the [Minecraft Wiki – Entities](https://minecraft.wiki/w/Entity#List_of_entities).
- **Biomes**: Use `/locate biome <tab>` in-game or inspect the biome ids in a datapack / registry dump.
- **Dimensions**: Vanilla examples are `minecraft:overworld`, `minecraft:the_nether`, and `minecraft:the_end`; modded dimensions use their own namespace and path.
- **Structures**: Use `/locate structure <tab>` in-game or check the [Minecraft Wiki – Generated structures](https://minecraft.wiki/w/Generated_structures).
- **Items**: Use `/give <player> <tab>` in-game or inspect item ids in JEI/EMI/registry dumps.

## Building from Source

Requirements: JDK 21, Git

```bash
git clone https://github.com/<your-username>/Disabler.git
cd Disabler
./gradlew build
```

The compiled jar will be in `build/libs/`.

## License

MIT — see [LICENSE](LICENSE).
