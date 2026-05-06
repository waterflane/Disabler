# Disabler

A server-side mod that lets server admins block specific mob spawns, biomes, dimensions, and structure generation through a simple config file. No GUI is required, and config changes are picked up on the next world load.

- **Block mob spawns** — remove mobs from biome spawn lists and cancel runtime spawn attempts
- **Block biomes** — strip their mob spawns, carvers, features, and structures from world generation
- **Block dimensions** — deny travel into them and relocate players out on login or respawn if they end up there
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
# List of mob resource IDs to block. Mobs are removed from biome spawn lists
# and their runtime spawn attempts are cancelled.
blocked_mobs = [
    "minecraft:zombie",
    "minecraft:creeper",
    "minecraft:phantom"
]

# List of biome resource IDs to disable. Matching biomes lose their worldgen,
# natural mob spawns, and all structures that could generate there.
blocked_biomes = [
    "minecraft:plains",
    "minecraft:crimson_forest"
]

[dimensions]
# List of dimension resource IDs to disable. Players and entities cannot enter
# them. Vanilla dimensions also have their biomes and structures stripped from worldgen.
blocked_dimensions = [
    "minecraft:the_nether",
    "minecraft:the_end"
]

[structures]
# List of structure resource IDs to block. Matching structures will not generate
# and their mob spawn overrides are removed.
blocked_structures = [
    "minecraft:village_plains",
    "minecraft:mineshaft",
    "minecraft:pillager_outpost"
]
```

Leave a list empty (`[]`) to disable that feature entirely.

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
