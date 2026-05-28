package com.wodichka.disabler.config;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public final class DisablerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CONFIG_FILE_NAME = "disabler-server.toml";
    private static final List<String> DEFAULT_BIOME_EXCEPTIONS = List.of("minecraft:mushroom_fields");

    private static volatile Set<ResourceLocation> blockedMobIds = Set.of();
    private static volatile Set<ResourceLocation> blockedStructureIds = Set.of();
    private static volatile Set<ResourceLocation> blockedBiomeIds = Set.of();
    private static volatile Set<ResourceLocation> blockedDimensionIds = Set.of();
    private static volatile Set<ResourceLocation> biomeExceptionIds = parseLocations(DEFAULT_BIOME_EXCEPTIONS, "biome exception");

    private DisablerConfig() {}

    public static void load(Path configDirectory) {
        Path configPath = configDirectory.resolve(CONFIG_FILE_NAME);
        try {
            Files.createDirectories(configDirectory);
            if (Files.notExists(configPath)) {
                Files.writeString(configPath, defaultConfig(), StandardCharsets.UTF_8);
            }

            String rawConfig = Files.readString(configPath, StandardCharsets.UTF_8);
            blockedMobIds = parseLocations(readStringList(rawConfig, "spawns", "blocked_mobs"), "mob");
            blockedStructureIds = parseLocations(readStringList(rawConfig, "structures", "blocked_structures"), "structure");
            blockedBiomeIds = parseLocations(readStringList(rawConfig, "biomes", "blocked_biomes"), "biome");
            blockedDimensionIds = parseLocations(readStringList(rawConfig, "dimensions", "blocked_dimensions"), "dimension");

            List<String> rawBiomeExceptions = readStringList(rawConfig, "biome_exceptions", "exceptions");
            if (rawBiomeExceptions.isEmpty()) {
                rawBiomeExceptions = DEFAULT_BIOME_EXCEPTIONS;
            }
            biomeExceptionIds = parseLocations(rawBiomeExceptions, "biome exception");
        } catch (IOException exception) {
            LOGGER.warn("Could not load Disabler config from {}", configPath, exception);
        }
    }

    public static boolean hasBlockedMobs() {
        return !blockedMobIds.isEmpty();
    }

    public static boolean hasBlockedStructures() {
        return !blockedStructureIds.isEmpty();
    }

    public static boolean hasBlockedBiomes() {
        return !blockedBiomeIds.isEmpty();
    }

    public static boolean hasBlockedDimensions() {
        return !blockedDimensionIds.isEmpty();
    }

    public static boolean hasBlockedBiomeRules() {
        return hasBlockedBiomes();
    }

    public static boolean isBlockedMob(EntityType<?> entityType) {
        return blockedMobIds.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }

    public static boolean isBlockedStructure(ResourceLocation structureId) {
        return blockedStructureIds.contains(structureId);
    }

    public static boolean isBlockedBiome(ResourceLocation biomeId) {
        return blockedBiomeIds.contains(biomeId);
    }

    public static boolean isBlockedDimension(ResourceKey<Level> dimensionKey) {
        return isBlockedDimension(dimensionKey.location());
    }

    public static boolean isBlockedDimension(ResourceLocation dimensionId) {
        return blockedDimensionIds.contains(dimensionId);
    }

    public static Set<ResourceLocation> getBlockedBiomeIds() {
        return blockedBiomeIds;
    }

    public static Set<ResourceLocation> getBiomeExceptionIds() {
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

    private static List<String> readStringList(String rawConfig, String section, String key) {
        String currentSection = "";
        for (String rawLine : rawConfig.split("\\R")) {
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).trim();
                continue;
            }
            if (!currentSection.equals(section) || !line.startsWith(key)) {
                continue;
            }

            int equalsIndex = line.indexOf('=');
            if (equalsIndex < 0) {
                return List.of();
            }
            return parseStringList(line.substring(equalsIndex + 1).trim());
        }
        return List.of();
    }

    private static String stripComment(String rawLine) {
        boolean inString = false;
        StringBuilder result = new StringBuilder(rawLine.length());
        for (int i = 0; i < rawLine.length(); i++) {
            char current = rawLine.charAt(i);
            if (current == '"' && (i == 0 || rawLine.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (current == '#' && !inString) {
                break;
            }
            result.append(current);
        }
        return result.toString();
    }

    private static List<String> parseStringList(String rawValue) {
        String value = rawValue.trim();
        if (!value.startsWith("[") || !value.endsWith("]")) {
            return List.of();
        }

        List<String> entries = new ArrayList<>();
        String body = value.substring(1, value.length() - 1).trim();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        for (int i = 0; i < body.length(); i++) {
            char character = body.charAt(i);
            if (character == '"' && (i == 0 || body.charAt(i - 1) != '\\')) {
                inString = !inString;
                continue;
            }
            if (character == ',' && !inString) {
                addEntry(entries, current);
                continue;
            }
            current.append(character);
        }
        addEntry(entries, current);
        return List.copyOf(entries);
    }

    private static void addEntry(List<String> entries, StringBuilder rawEntry) {
        String entry = rawEntry.toString().trim();
        rawEntry.setLength(0);
        if (!entry.isEmpty()) {
            entries.add(entry.replace("\\\"", "\""));
        }
    }

    private static String defaultConfig() {
        return """
                [spawns]
                \t#List of mob ids that should never appear in the world.
                \t#Examples: "minecraft:zombie", "minecraft:creeper"
                \tblocked_mobs = []

                [structures]
                \t#List of structure ids that should be removed from world generation.
                \t#Examples: "minecraft:village_plains", "minecraft:mineshaft"
                \tblocked_structures = []

                [biomes]
                \t#List of biome ids that should be fully removed from world generation.
                \t#Examples: "minecraft:plains", "minecraft:swamp"
                \tblocked_biomes = []

                [dimensions]
                \t#List of dimension ids that entities should not be able to enter.
                \t#Dimension travel is cancelled before the entity changes level.
                \t#Examples: "minecraft:the_nether", "minecraft:the_end"
                \tblocked_dimensions = []

                [biome_exceptions]
                \t#List of biome ids that should NOT be included in the replacement pool.
                \t#These biomes have special generation requirements (e.g., mushroom biome on islands).
                \t#They will be kept as fallback if no other allowed biomes exist.
                \t#By default, minecraft:mushroom_fields is excluded.
                \t#Examples: "minecraft:mushroom_fields", "minecraft:deep_dark"
                \texceptions = ["minecraft:mushroom_fields"]
                """;
    }
}
