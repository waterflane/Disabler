# Disabler

A server-side mod that lets server admins block specific mob spawns, biomes, dimensions, and structure generation through a simple config file. No GUI is required, and config changes are picked up on the next world load.

- **Block mob spawns** — remove mobs from biome spawn lists and cancel runtime spawn attempts
- **Block biomes** — strip their mob spawns, carvers, features, and structures from world generation; blocked biomes are replaced with allowed alternatives at runtime
- **Block structures** — prevent entire structure types from generating and strip their mob spawn overrides
- **Block dimensions** - cancel travel before entities enter disabled dimensions and prevent Nether portal creation when the Nether is disabled
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
.minecraft/config/disabler-server.toml
```

```toml
[spawns]
	#List of mob ids that should never appear in the world.
	#Examples: "minecraft:zombie", "minecraft:creeper"
	blocked_mobs = []

[structures]
	#List of structure ids that should be removed from world generation.
	#Examples: "minecraft:village_plains", "minecraft:mineshaft"
	blocked_structures = []

[biomes]
	#List of biome ids that should be fully removed from world generation.
	#Examples: "minecraft:plains", "minecraft:swamp"
	blocked_biomes = []

[dimensions]
	#List of dimension ids that entities should not be able to enter.
	#Dimension travel is cancelled before the entity changes level.
	#Examples: "minecraft:the_nether", "minecraft:the_end"
	blocked_dimensions = []

[biome_exceptions]
	#List of biome ids that should NOT be included in the replacement pool.
	#These biomes have special generation requirements (e.g., mushroom biome on islands).
	#They will be kept as fallback if no other allowed biomes exist.
	#By default, minecraft:mushroom_fields is excluded.
	#Examples: "minecraft:mushroom_fields", "minecraft:deep_dark"
	exceptions = ["minecraft:mushroom_fields"]

```

### Key Points

- **Biome Exceptions**: By default, `minecraft:mushroom_fields` is listed in `biome_exceptions`. This prevents it from being used as a replacement for blocked biomes, since mushroom biomes have special generation requirements (e.g., spawning only on islands). You can remove it from this list if you want mushroom biomes to be used as replacements.
- **All lists can be empty**: Leave any list empty (`[]`) to disable that feature entirely.
- **Config is written to disk**: When the mod generates the config file for the first time, it includes the default `minecraft:mushroom_fields` in the exceptions list. Edit the file to customize this behavior.

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

### Dimension Blocking Implementation

Blocked dimensions are handled before the entity changes level:

1. **Dimension Travel Cancellation** (`DimensionTravelBlocker`):
   - `EntityTravelToDimensionEvent` is cancelled when the target dimension is in `blocked_dimensions`
   - The entity stays in its current dimension; it is not teleported to spawn as a fallback
   - Portal cooldown is applied and the current portal process is cleared to avoid rapid repeat attempts
   - Players receive a short action bar message, throttled so it does not spam

2. **Nether Portal Creation Prevention**:
   - If `minecraft:the_nether` is blocked, `PortalSpawnEvent` is cancelled
   - This prevents newly lit Nether portals from forming while still keeping the general travel blocker for existing portals and modded travel paths

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
