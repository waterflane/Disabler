package com.wodichka.disabler.config;

import com.mojang.logging.LogUtils;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

public final class DisablerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<String> DEFAULT_BIOME_EXCEPTIONS = List.of("minecraft:mushroom_fields");

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_MOBS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_STRUCTURES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_BIOMES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BIOME_EXCEPTIONS;

    private static volatile List<String> mobSnapshot = List.of();
    private static volatile Set<ResourceLocation> blockedMobIds = Set.of();
    private static volatile List<String> structureSnapshot = List.of();
    private static volatile Set<ResourceLocation> blockedStructureIds = Set.of();
    private static volatile List<String> biomeSnapshot = List.of();
    private static volatile Set<ResourceLocation> blockedBiomeIds = Set.of();
    private static volatile List<String> biomeExceptionSnapshot = List.of();
    private static volatile Set<ResourceLocation> biomeExceptionIds = Set.of();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("spawns");
        BLOCKED_MOBS = builder
                .comment(
                        "List of mob ids that should never appear in the world.",
                        "Examples: \"minecraft:zombie\", \"minecraft:creeper\"")
                .defineListAllowEmpty("blocked_mobs", List::of, value -> value instanceof String);
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
                        "Examples: \"minecraft:plains\", \"minecraft:swamp\"")
                .defineListAllowEmpty("blocked_biomes", List::of, value -> value instanceof String);
        builder.pop();

        builder.push("biome_exceptions");
        BIOME_EXCEPTIONS = builder
                .comment(
                        "List of biome ids that should NOT be included in the replacement pool.",
                        "These biomes have special generation requirements (e.g., mushroom biome on islands).",
                        "They will be kept as fallback if no other allowed biomes exist.",
                        "By default, minecraft:mushroom_fields is excluded.",
                        "Examples: \"minecraft:mushroom_fields\", \"minecraft:deep_dark\"")
                .defineList("exceptions", DEFAULT_BIOME_EXCEPTIONS, value -> value instanceof String);
        builder.pop();

        SPEC = builder.build();
    }

    private DisablerConfig() {}

    public static boolean hasBlockedMobs() {
        return !getBlockedMobIds().isEmpty();
    }

    public static boolean hasBlockedStructures() {
        return !getBlockedStructureIds().isEmpty();
    }

    public static boolean hasBlockedBiomes() {
        return !getBlockedBiomeIds().isEmpty();
    }

    public static boolean hasBlockedBiomeRules() {
        return hasBlockedBiomes();
    }

    public static boolean isBlockedMob(EntityType<?> entityType) {
        return getBlockedMobIds().contains(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
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

    public static Set<ResourceLocation> getBiomeExceptionIds() {
        List<String> current = List.copyOf(BIOME_EXCEPTIONS.get());
        if (!current.equals(biomeExceptionSnapshot)) {
            synchronized (DisablerConfig.class) {
                if (!current.equals(biomeExceptionSnapshot)) {
                    biomeExceptionIds = parseLocations(current, "biome exception");
                    biomeExceptionSnapshot = current;
                }
            }
        }
        return biomeExceptionIds;
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
}