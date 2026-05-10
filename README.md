# Disabler

A server-side mod that lets server admins block specific mob spawns, biomes, dimensions, and structure generation through a simple config file. No GUI is required, and config changes are picked up on the next world load.

- **Block mob spawns** — remove mobs from biome spawn lists and cancel runtime spawn attempts
- **Block biomes** — strip their mob spawns, carvers, features, and structures from world generation; blocked biomes are replaced with allowed alternatives at runtime
- **Block structures** — prevent entire structure types from generating and strip their mob spawn overrides
- Config-driven: plain text TOML file, no commands or GUI needed
- Server-side only (no client install required)

## Requirements

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x |

## Configuration

The config file is located at:
```
<world>/serverconfig/disabler-server.toml
```

```toml
[spawns]
	#List of mob ids that should never appear in the world.
	#Examples: minecraft:zombie, minecraft:creeper
	blocked_mobs = ["minecraft:zombie"]

[structures]
	#List of structure ids that should be removed from world generation.
	#Examples: minecraft:village_plains, minecraft:mineshaft
	blocked_structures = ["minecraft:village_plains"]

[biomes]
	#List of biome ids that should be fully removed from world generation.
	#Examples: minecraft:plains, minecraft:swamp
	blocked_biomes = ["minecraft:plains"]

```

Leave a list empty (`[]`) to disable that feature entirely.

This configuration will:
- Block spawning of zombies(removed from spawn lists, cancelled at runtime)
- Replace plains with allowed alternatives during world generation
- Prevent village_plains from generating
- Filter all structures to only generate in non-blocked biomes

### Biome Blocking Implementation

When a biome is blocked via the config, the following happens:

1. **World Generation Phase** (`ConfigDrivenBiomeModifier`):
   - All mob spawn entries are **cleared** from the blocked biome's spawn settings
   - Carvers, features, and structures associated with that biome are removed
   - The biome is excluded from structure generation filters

2. **Runtime Biome Replacement** (`MultiNoiseBiomeSourceMixin`):
   - During world generation, when a blocked biome would be selected via `getNoiseBiome()`, it's intercepted
   - The biome is replaced with an **allowed alternative** from the same biome parameter list
   - Allowed biomes are collected once and cached in memory for performance
   - If no allowed biomes exist, the blocked biome is kept as fallback (prevents empty world generation)

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
