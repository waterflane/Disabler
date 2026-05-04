# Disabler

A server-side mod that lets server admins block specific mob spawns and structure generation through a simple config file — no restarts required after config edits (changes apply on world load).

- **Block mob spawns** — remove mobs from biome spawn lists and cancel runtime spawn attempts
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
