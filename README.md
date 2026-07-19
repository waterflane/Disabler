<div align="center">

# Disabler
<img width="1280" height="512" alt="изображение" src="https://github.com/user-attachments/assets/cbbb44c0-1ba5-4f82-9cf8-b1df523a661a" />

[![GitHub Stars](https://img.shields.io/github/stars/waterflane/Disabler?style=for-the-badge\&logo=github)](https://github.com/waterflane/Disabler/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/waterflane/Disabler?style=for-the-badge\&logo=github)](https://github.com/waterflane/Disabler/network/members)
[![Latest Release](https://img.shields.io/github/v/release/waterflane/Disabler?style=for-the-badge\&logo=github)](https://github.com/waterflane/Disabler/releases/latest)
[![License](https://img.shields.io/github/license/waterflane/Disabler?style=for-the-badge)](LICENSE)

[![Download on Modrinth](https://img.shields.io/badge/Download-Modrinth-00AF5C?style=for-the-badge\&logo=modrinth\&logoColor=white)](https://modrinth.com/mod/disabler/versions)
[![Download on CurseForge](https://img.shields.io/badge/Download-CurseForge-F16436?style=for-the-badge\&logo=curseforge\&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/disabler/files/all)

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/disabler?style=flat-square\&logo=modrinth\&label=Modrinth%20downloads)](https://modrinth.com/mod/disabler)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1537081?style=flat-square\&logo=curseforge\&label=CurseForge%20downloads)](https://www.curseforge.com/minecraft/mc-mods/disabler)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%201.21.1-62B47A?style=flat-square)](#supported-versions)
[![Loaders](https://img.shields.io/badge/loaders-Fabric%20%7C%20Forge%20%7C%20NeoForge-4C8BF5?style=flat-square)](#supported-versions)
[![Environment](https://img.shields.io/badge/environment-server--side-6E40C9?style=flat-square)](#installation)

A server-side Minecraft mod for blocking mobs, biomes, structures, dimensions, and items through a single JSON configuration file.

No GUI, commands, datapacks, or client-side installation required for dedicated servers.

<br/>

**[Download](#download)** · **[Features](#features)** · **[Installation](#installation)** · **[Configuration](#configuration)** · **[How It Works](#how-it-works)**

</div>

---

> [!IMPORTANT]
> Disabler is installed only on the dedicated server. Players can join without installing the mod on their clients.

> [!WARNING]
> Back up the world before changing biome or structure settings. World-generation changes affect newly generated chunks and do not rewrite existing terrain.

## Download

The latest public release is **Disabler 1.3**.

Choose the file matching both your Minecraft version and mod loader.

| Platform       | Download page                                                                                |
| -------------- | -------------------------------------------------------------------------------------------- |
| **Modrinth**   | [Browse versions and download](https://modrinth.com/mod/disabler/versions)                   |
| **CurseForge** | [Browse files and download](https://www.curseforge.com/minecraft/mc-mods/disabler/files/all) |
| **GitHub**     | [View source releases](https://github.com/waterflane/Disabler/releases/latest)               |

> [!CAUTION]
> Do not install a `1.20.1` artifact on Minecraft `1.21.1`, or a `1.21.1` artifact on Minecraft `1.20.1`. Also make sure the artifact name matches your loader: `fabric`, `forge`, or `neoforge`.

## Features

| Feature                        | Behavior                                                                                                      |
| ------------------------------ | ------------------------------------------------------------------------------------------------------------- |
| **Mob blocking**               | Removes configured mobs from biome spawn lists and cancels runtime spawn attempts                             |
| **Biome blocking**             | Excludes configured biomes from newly generated terrain and replaces them with allowed alternatives           |
| **Structure blocking**         | Prevents configured structures from starting and removes their mob spawn overrides                            |
| **Dimension blocking**         | Cancels travel before an entity enters a configured vanilla or modded dimension                               |
| **Item blocking**              | Removes configured items from loot, entities, players, containers, crafting, smelting, and supported storages |
| **Storage scanning**           | Cleans loaded vanilla and modded inventories in configurable batches                                          |
| **Lootr compatibility**        | Filters generated per-player Lootr inventories without replacing Lootr containers                             |
| **Alex's Caves compatibility** | Includes additional handling for Alex's Caves biome-generation paths                                          |
| **Legacy migration**           | Migrates known settings from the previous TOML configuration format                                           |

## Release 1.3

Disabler 1.3 expands the mod from world-generation and spawn control into a broader server-side restriction system.

Main changes include:

* Full blocking of vanilla and modded items
* Removal of blocked items from generated loot
* Cleanup of player inventories, equipment, ender chests, crafting results, furnace results, open containers, and supported storages
* Migration from TOML to strict JSON configuration
* Automatic migration of known values from an existing `disabler-server.toml`
* Automatic generation of `config/DisablerGuide.md`
* Fixed biome replacement so land biomes are not replaced with ocean biomes
* Native builds for Fabric, Forge, and NeoForge
* Support for Minecraft 1.20.1 and 1.21.1

## Supported Versions

| Minecraft  | Java    | Fabric                                          | Forge    | NeoForge               |
| ---------- | ------- | ----------------------------------------------- | -------- | ---------------------- |
| **1.21.1** | Java 21 | Loader `0.16.10+`, Fabric API `0.116.12+1.21.1` | `52.1.x` | `21.1.x`               |
| **1.20.1** | Java 17 | Loader `0.16.10+`, Fabric API `0.92.9+1.20.1`   | `47.4.x` | `47.1.x` legacy branch |

Every supported combination has its own native artifact.

| Loader   | Artifact                               |
| -------- | -------------------------------------- |
| NeoForge | Native NeoForge JAR                    |
| Forge    | Native Forge JAR                       |
| Fabric   | Native Fabric JAR; Fabric API required |

> [!NOTE]
> The NeoForge build for Minecraft 1.20.1 targets the legacy NeoForge 47.1.x line. Do not replace it with the Forge artifact even though both loaders originate from the same ecosystem.

## Installation

### Dedicated server

1. Select your Minecraft version:

   * `1.20.1`
   * `1.21.1`

2. Select your mod loader:

   * Fabric
   * Forge
   * NeoForge

3. Download the matching Disabler artifact from:

   * [Modrinth](https://modrinth.com/mod/disabler/versions)
   * [CurseForge](https://www.curseforge.com/minecraft/mc-mods/disabler/files/all)

4. Place the downloaded JAR in the server's `mods` directory.

5. When using Fabric, install the corresponding version of Fabric API.

6. Start the server once to generate the configuration.

7. Stop the server and edit:

   ```text
   config/disabler-server.json
   ```

8. Start the server again to apply the configuration.

### Singleplayer

Disabler also works in singleplayer through Minecraft's integrated server.

In that case, install the mod in the local instance's `mods` directory. Fabric installations also require Fabric API.

## Configuration

Disabler creates two files:

```text
config/
├── disabler-server.json
└── DisablerGuide.md
```

| File                   | Purpose                                               |
| ---------------------- | ----------------------------------------------------- |
| `disabler-server.json` | Active server configuration                           |
| `DisablerGuide.md`     | Automatically generated reference for every parameter |

The same JSON format is used on Fabric, Forge, and NeoForge.

### Default configuration

```json
{
  "blocked_mobs": [],
  "blocked_biomes": [],
  "biome_exceptions": [
    "minecraft:mushroom_fields"
  ],
  "blocked_structures": [],
  "blocked_dimensions": [],
  "blocked_items": [],
  "storage_scan": {
    "enabled": true,
    "interval_ticks": 300,
    "block_entities_per_tick": 512,
    "skipped_namespaces": []
  }
}
```

### Configuration reference

| Parameter                              | Type             | Description                                                                   |
| -------------------------------------- | ---------------- | ----------------------------------------------------------------------------- |
| `blocked_mobs`                         | Resource ID list | Entity types that cannot spawn or enter a server level                        |
| `blocked_biomes`                       | Resource ID list | Biomes excluded from newly generated chunks                                   |
| `biome_exceptions`                     | Resource ID list | Biomes that cannot be selected as replacements                                |
| `blocked_structures`                   | Resource ID list | Structures prevented from starting in new chunks                              |
| `blocked_dimensions`                   | Resource ID list | Dimensions that entities cannot enter                                         |
| `blocked_items`                        | Resource ID list | Items removed from loot, entities, inventories, menus, and supported storages |
| `storage_scan.enabled`                 | Boolean          | Enables periodic scanning of loaded block entity inventories                  |
| `storage_scan.interval_ticks`          | Integer          | Delay between complete storage scan cycles                                    |
| `storage_scan.block_entities_per_tick` | Integer          | Maximum queued block entities processed during one server tick                |
| `storage_scan.skipped_namespaces`      | Namespace list   | Mod namespaces excluded from physical storage scanning                        |

All blocking lists may remain empty:

```json
"blocked_mobs": []
```

An empty list disables that restriction without affecting the other features.

## Example Configuration

The following example blocks zombies, plains biomes, plains villages, the End, and elytras:

```json
{
  "blocked_mobs": [
    "minecraft:zombie"
  ],
  "blocked_biomes": [
    "minecraft:plains"
  ],
  "biome_exceptions": [
    "minecraft:mushroom_fields"
  ],
  "blocked_structures": [
    "minecraft:village_plains"
  ],
  "blocked_dimensions": [
    "minecraft:the_end"
  ],
  "blocked_items": [
    "minecraft:elytra"
  ],
  "storage_scan": {
    "enabled": true,
    "interval_ticks": 300,
    "block_entities_per_tick": 512,
    "skipped_namespaces": []
  }
}
```

Configuration changes are loaded during mod initialization and again before the server world starts.

Restart the server after editing the file.

## Biome Exceptions

`minecraft:mushroom_fields` is included in `biome_exceptions` by default.

Mushroom fields have special terrain-generation requirements and normally generate as isolated islands. Excluding them prevents mushroom fields from unexpectedly replacing blocked continental biomes.

You may remove the entry when mushroom fields should be available as replacement candidates:

```json
"biome_exceptions": []
```

Disabler also preserves the broad terrain category during biome replacement:

* Land biomes are replaced with land candidates
* Water biomes are replaced with water candidates
* Replacement candidates remain inside the current dimension

If no valid replacement can be found, the original biome is retained as a safe fallback instead of breaking world generation.

## Item and Storage Blocking

Blocked items are handled at several points rather than through a full inventory scan every tick.

### Generated loot

Blocked items are removed from:

* Vanilla loot tables
* Modded loot tables
* Datapack loot tables
* Generated Lootr per-player inventories

Unopened loot-table containers remain untouched until their loot is generated.

### Item entities

Disabler prevents blocked items from remaining in the world:

* Ground item entities are cancelled
* Player pickups are denied
* Tossed blocked items are removed

### Player inventories

Blocked items are cleaned from:

* Main inventory
* Armor
* Offhand
* Ender chest
* Carried cursor stack
* Open containers
* Crafting results
* Smelting results

Cleanup is triggered during relevant events such as login, respawn, dimension changes, container interaction, crafting, and smelting.

A lightweight safety sweep runs once every `100` server ticks, approximately every 5 seconds.

### Storage scanning

Loaded block entity inventories are checked through:

* Forge and NeoForge item capabilities
* Fabric Transfer API

This allows Disabler to work with many technical and magic mod storages without hardcoding every mod ID.

Storage scanning is interval-based and processed in batches to reduce server tick spikes.

Default settings:

```json
{
  "storage_scan": {
    "enabled": true,
    "interval_ticks": 300,
    "block_entities_per_tick": 512,
    "skipped_namespaces": []
  }
}
```

`300` ticks is approximately 15 seconds.

Increasing `interval_ticks` reduces scan frequency. Reducing `block_entities_per_tick` spreads the work across more server ticks.

To exclude all physical storages belonging to a particular mod:

```json
"skipped_namespaces": [
  "examplemod"
]
```

The namespace is checked before Disabler accesses the inventory or item capability.

## Legacy TOML Migration

Older versions used:

```text
config/disabler-server.toml
```

When the JSON file does not exist but the old TOML file is present, Disabler migrates known list settings into:

```text
config/disabler-server.json
```

The old TOML file is retained as a backup. All further configuration uses JSON.

> [!NOTE]
> Review the generated JSON after migration, especially when the old TOML was manually modified or contained unsupported keys.

## Important Behavior

* Existing chunks are not regenerated when a biome is blocked
* Existing structures are not removed from generated chunks
* Blocking a dimension does not delete its world data
* Dimension travel is cancelled before the entity changes level
* Players are not teleported to spawn when travel is denied
* Blocking the Nether also prevents new Nether portals from forming
* Existing blocked items are removed when the relevant inventory, container, or storage cleanup runs
* Configuration changes require a server restart or world reload
* Dedicated-server players do not need the mod installed client-side

## Finding Resource IDs

Disabler uses namespaced Minecraft resource IDs:

```text
namespace:path
```

Vanilla examples:

```text
minecraft:zombie
minecraft:plains
minecraft:village_plains
minecraft:the_end
minecraft:elytra
```

Modded resources use the namespace registered by their mod:

```text
examplemod:custom_mob
examplemod:custom_biome
examplemod:custom_structure
examplemod:custom_dimension
examplemod:custom_item
```

| Resource       | How to find its ID                                                                    |
| -------------- | ------------------------------------------------------------------------------------- |
| **Mobs**       | Enter `/summon ` and use command completion                                           |
| **Biomes**     | Enter `/locate biome ` and use command completion                                     |
| **Structures** | Enter `/locate structure ` and use command completion                                 |
| **Dimensions** | Check the mod documentation, datapack, or registry dump                               |
| **Items**      | Enter `/give <player> ` and use command completion, or inspect the item in JEI or EMI |

Additional vanilla references:

* [Minecraft Wiki — Entities](https://minecraft.wiki/w/Entity#List_of_entities)
* [Minecraft Wiki — Generated structures](https://minecraft.wiki/w/Generated_structures)

## How It Works

<details>
<summary><strong>Mob blocking</strong></summary>

Blocked mobs are removed from biome spawn lists so they are not selected by normal spawning.

Runtime checks also reject blocked entities when they attempt to enter a server level through:

* Natural spawning
* Commands
* Mods
* NBT loading
* Creative actions
* Other non-standard creation paths

Loader-native events and mixins provide equivalent behavior across Fabric, Forge, and NeoForge.

</details>

<details>
<summary><strong>Biome blocking</strong></summary>

For blocked biomes, Disabler removes:

* Mob spawn entries
* Carvers
* Placed features
* Associated structures
* Structure-generation eligibility

When a blocked biome would be selected during generation, Disabler replaces it with an allowed candidate from the current dimension.

Replacement candidates are cached for performance and selected while preserving the land or water category.

Additional compatibility handling is included for Alex's Caves cave biome-generation paths.

</details>

<details>
<summary><strong>Structure blocking</strong></summary>

Blocked structures are filtered out before they can start generating.

Disabler also:

* Clears their biome eligibility
* Removes their mob spawn overrides
* Removes blocked biomes from structures that remain enabled
* Disables a structure completely when all of its eligible biomes are blocked
* Removes blocked mob overrides from structures that are otherwise allowed

</details>

<details>
<summary><strong>Dimension blocking</strong></summary>

Dimension travel is intercepted before an entity changes level.

Both vanilla and modded dimension IDs are supported.

When travel is denied:

* The entity remains in its current dimension
* No spawn teleport fallback is used
* New Nether portals are prevented when `minecraft:the_nether` is blocked

</details>

<details>
<summary><strong>Item blocking</strong></summary>

Disabler combines several mechanisms:

* Global loot filtering
* Item entity rejection
* Pickup prevention
* Player inventory cleanup
* Crafting and smelting result cleanup
* Open-container cleanup
* Batched block entity storage scanning
* Optional Lootr compatibility handling

This avoids scanning every player and every storage during every server tick.

</details>

## Project Structure

The project uses shared common code with loader-specific integration modules.

```text
Disabler/
├── common/
├── fabric/
├── forge/
└── neoforge/
```

### Common module

The common module owns:

* JSON configuration parsing
* Immutable runtime configuration snapshots
* Shared blocking logic
* Biome replacement
* Shared Minecraft mixins
* Optional compatibility mixins

### Loader modules

Forge and NeoForge primarily use:

* Loader-native events
* Biome modifiers
* Structure modifiers
* Global loot modifiers
* Item capabilities

Fabric primarily uses:

* Fabric entrypoint loading
* Fabric API
* Fabric Transfer API
* Mixins for entity insertion
* Dimension travel interception
* Portal handling
* Structure generation
* Spawn-list filtering
* Loot output filtering

## Building from Source

The repository uses separate branches for each supported Minecraft version.

| Minecraft | Branch        | Required JDK |
| --------- | ------------- | ------------ |
| `1.21.1`  | `1.21.1/main` | JDK 21       |
| `1.20.1`  | `1.20.1/main` | JDK 17       |

### Clone the repository

```bash
git clone https://github.com/waterflane/Disabler.git
cd Disabler
```

### Build for Minecraft 1.21.1

```bash
git checkout 1.21.1/main
./gradlew build
```

Windows:

```powershell
git checkout 1.21.1/main
.\gradlew.bat build
```

### Build for Minecraft 1.20.1

```bash
git checkout 1.20.1/main
./gradlew build
```

Windows:

```powershell
git checkout 1.20.1/main
.\gradlew.bat build
```

Loader artifacts are copied into:

```text
releases/
```

Artifact names follow this format:

```text
disabler-<loader>-<minecraft-version>-<mod-version>.jar
```

Examples:

```text
disabler-fabric-1.20.1-1.3.jar
disabler-forge-1.20.1-1.3.jar
disabler-neoforge-1.20.1-1.3.jar

disabler-fabric-1.21.1-1.3.jar
disabler-forge-1.21.1-1.3.jar
disabler-neoforge-1.21.1-1.3.jar
```

## Bug Reports

Report bugs through [GitHub Issues](https://github.com/waterflane/Disabler/issues).

Include:

* Minecraft version
* Loader and loader version
* Disabler version
* Relevant section of `disabler-server.json`
* Server log or crash report
* Minimal reproduction steps
* Names and versions of potentially related mods

Do not include authentication tokens, private server addresses, or unrelated log content.

## License

Disabler is distributed under the [MIT License](LICENSE).
