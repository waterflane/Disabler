package com.wodichka.disabler.config;

import com.mojang.logging.LogUtils;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

public final class DisablerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_MOBS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_BIOMES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_DIMENSIONS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_STRUCTURES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_BIOMES;

    private static volatile List<String> mobSnapshot = List.of();
    private static volatile Set<ResourceLocation> blockedMobIds = Set.of();
    private static volatile List<String> biomeSnapshot = List.of();
    private static volatile Set<ResourceLocation> blockedBiomeIds = Set.of();
    private static volatile List<String> dimensionSnapshot = List.of();
    private static volatile Set<ResourceKey<Level>> blockedDimensionKeys = Set.of();
    private static volatile List<String> structureSnapshot = List.of();
    private static volatile Set<ResourceLocation> blockedStructureIds = Set.of();
    private static volatile List<String> biomeSnapshot = List.of();
    private static volatile Set<ResourceLocation> blockedBiomeIds = Set.of();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("spawns");
        BLOCKED_MOBS = builder
                .comment(
                        "List of mob ids that should never appear in the world.",
                        "Examples: \"minecraft:zombie\", \"minecraft:creeper\"")
                .defineListAllowEmpty("blocked_mobs", List::of, value -> value instanceof String);

        BLOCKED_BIOMES = builder
            .comment(
                "List of biome ids that should be stripped of mob spawns, carvers, features, and structures.",
                "Examples: \"minecraft:plains\", \"minecraft:crimson_forest\"")
            .defineListAllowEmpty("blocked_biomes", List::of, value -> value instanceof String);
        builder.pop();

        builder.push("dimensions");
        BLOCKED_DIMENSIONS = builder
            .comment(
                "List of dimension ids that players and entities may not enter.",
                "Vanilla dimensions also have their biomes and structures stripped from worldgen.",
                "Examples: \"minecraft:the_nether\", \"minecraft:the_end\"")
            .defineListAllowEmpty("blocked_dimensions", List::of, value -> value instanceof String);
        builder.pop();

        builder.push("structures");
        BLOCKED_STRUCTURES = builder
                .comment(
                        "List of structure ids that should be removed from world generation.",
                        "Examples: \"minecraft:village_plains\", \"minecraft:mineshaft\"")
                .defineListAllowEmpty("blocked_structures", List::of, value -> value instanceof String);
        builder.pop();

        builder.push("biomes");
        BLOCKED_BIOMES = builder
            .comment(
                "List of biome ids that should be fully removed from world generation.",
                "Examples: minecraft:plains, minecraft:swamp")
            .defineListAllowEmpty("blocked_biomes", List::of, value -> value instanceof String);
        builder.pop();

        SPEC = builder.build();
    }

    private DisablerConfig() {}

    public static boolean hasBlockedMobs() {
        return !getBlockedMobIds().isEmpty();
    }

    public static boolean hasBlockedBiomeRules() {
        return !getBlockedBiomeIds().isEmpty()
                || isBlockedDimension(Level.OVERWORLD)
                || isBlockedDimension(Level.NETHER)
                || isBlockedDimension(Level.END);
    }

    public static boolean hasBlockedDimensions() {
        return !getBlockedDimensionKeys().isEmpty();
    }

    public static boolean hasBlockedStructures() {
        return !getBlockedStructureIds().isEmpty();
    }

    public static boolean hasBlockedBiomes() {
        return !getBlockedBiomeIds().isEmpty();
    }

    public static boolean isBlockedMob(EntityType<?> entityType) {
        return getBlockedMobIds().contains(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }

    public static boolean isBlockedBiome(Holder<Biome> biome) {
        ResourceLocation biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        return (biomeId != null && getBlockedBiomeIds().contains(biomeId)) || isBlockedByVanillaDimension(biome);
    }

    public static boolean isBlockedDimension(ResourceKey<Level> dimension) {
        return getBlockedDimensionKeys().contains(dimension);
    }

    public static boolean isBlockedStructure(ResourceLocation structureId) {
        return getBlockedStructureIds().contains(structureId);
    }

    public static boolean isBlockedBiome(ResourceLocation biomeId) {
        return getBlockedBiomeIds().contains(biomeId);
    }

    private static Set<ResourceLocation> getBlockedMobIds() {
        List<String> current = List.copyOf(BLOCKED_MOBS.get());
        if (!current.equals(mobSnapshot)) {
            synchronized (DisablerConfig.class) {
                if (!current.equals(mobSnapshot)) {
                    blockedMobIds = parseLocations(current, "mob");
                    mobSnapshot = current;
                }
            }
        }
        return blockedMobIds;
    }

    private static Set<ResourceLocation> getBlockedBiomeIds() {
        List<String> current = List.copyOf(BLOCKED_BIOMES.get());
        if (!current.equals(biomeSnapshot)) {
            synchronized (DisablerConfig.class) {
                if (!current.equals(biomeSnapshot)) {
                    blockedBiomeIds = parseLocations(current, "biome");
                    biomeSnapshot = current;
                }
            }
        }
        return blockedBiomeIds;
    }

    private static Set<ResourceKey<Level>> getBlockedDimensionKeys() {
        List<String> current = List.copyOf(BLOCKED_DIMENSIONS.get());
        if (!current.equals(dimensionSnapshot)) {
            synchronized (DisablerConfig.class) {
                if (!current.equals(dimensionSnapshot)) {
                    blockedDimensionKeys = parseDimensionKeys(current);
                    dimensionSnapshot = current;
                }
            }
        }
        return blockedDimensionKeys;
    }

    private static Set<ResourceLocation> getBlockedStructureIds() {
        List<String> current = List.copyOf(BLOCKED_STRUCTURES.get());
        if (!current.equals(structureSnapshot)) {
            synchronized (DisablerConfig.class) {
                if (!current.equals(structureSnapshot)) {
                    blockedStructureIds = parseLocations(current, "structure");
                    structureSnapshot = current;
                }
            }
        }
        return blockedStructureIds;
    }

<<<<<<< Updated upstream
    private static boolean isBlockedByVanillaDimension(Holder<Biome> biome) {
        return (isBlockedDimension(Level.OVERWORLD) && biome.is(BiomeTags.IS_OVERWORLD))
                || (isBlockedDimension(Level.NETHER) && biome.is(BiomeTags.IS_NETHER))
                || (isBlockedDimension(Level.END) && biome.is(BiomeTags.IS_END));
=======
    private static Set<ResourceLocation> getBlockedBiomeIds() {
        List<String> current = List.copyOf(BLOCKED_BIOMES.get());
        if (!current.equals(biomeSnapshot)) {
            synchronized (DisablerConfig.class) {
                if (!current.equals(biomeSnapshot)) {
                    blockedBiomeIds = parseLocations(current, "biome");
                    biomeSnapshot = current;
                }
            }
        }
        return blockedBiomeIds;
>>>>>>> Stashed changes
    }

    private static Set<ResourceLocation> parseLocations(List<String> rawIds, String kind) {
        Set<ResourceLocation> parsedIds = new LinkedHashSet<>();
        for (String rawId : rawIds) {
            ResourceLocation id = ResourceLocation.tryParse(rawId);
            if (id == null) {
                LOGGER.warn("Ignoring invalid {} id '{}' in Disabler config", kind, rawId);
                continue;
            }
            parsedIds.add(id);
        }
        return Set.copyOf(parsedIds);
    }

    private static Set<ResourceKey<Level>> parseDimensionKeys(List<String> rawIds) {
        Set<ResourceKey<Level>> parsedKeys = new LinkedHashSet<>();
        for (ResourceLocation id : parseLocations(rawIds, "dimension")) {
            parsedKeys.add(ResourceKey.create(Registries.DIMENSION, id));
        }
        return Set.copyOf(parsedKeys);
    }
}