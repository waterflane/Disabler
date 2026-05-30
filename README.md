# Disabler

Disabler is a server-side Minecraft mod for blocking mob spawns, structures, biomes, and dimensions through one simple TOML config.

Version 1.2 is organized as a multi-loader project for Minecraft 1.21.1, 1.20.1:

| Loader | Status |
|--------|--------|
| NeoForge | Native artifact |
| Forge | Native artifact |
| Fabric | Native artifact, Fabric API required |

Architectury Loom is used only for the build/toolchain layout. Architectury API is not required at runtime.

## Features

- Block mob spawns by id.
- Block structure generation by id.
- Replace blocked biomes during world generation.
- Keep blocked biomes out of structure biome filters and spawn-list paths where the loader allows it.
- Block dimension travel before the entity changes level.
- Prevent new Nether portals when `minecraft:the_nether` is blocked.
- Keep the same config file on all loaders: `.minecraft/config/disabler-server.toml`.
- Include Alex's Caves compatibility for rarity-based cave biome selection and direct biome rewrite paths.

## Configuration

The config file is located at:

```text
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

`minecraft:mushroom_fields` is excluded from biome replacement candidates by default because it has special generation requirements. Empty lists are valid and disable that feature.

## Implementation Notes

- Common code owns config parsing, immutable runtime snapshots, biome replacement, and shared Minecraft/Alex's Caves mixins.
- NeoForge and Forge use loader-native events plus biome and structure modifiers.
- Fabric uses Fabric entrypoint loading plus mixins for entity add, dimension travel, Nether portal creation, structure generation, and spawn-list filtering.
- Blocked dimension travel is cancelled in-place. Players are not teleported to spawn as a fallback.

## Building

Requirements: JDK 21 and Git.

```bash
./gradlew build
```

Per-loader artifacts are built from:

```bash
./gradlew :neoforge:build
./gradlew :forge:build
./gradlew :fabric:build
```

## License

MIT, see [LICENSE](LICENSE).
