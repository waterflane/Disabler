package com.wodichka.disabler.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

public final class DisablerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final List<String> DEFAULT_BIOME_EXCEPTIONS = List.of("minecraft:mushroom_fields");
    private static final String CONFIG_FILE_NAME = "disabler-server.json";
    private static final String LEGACY_CONFIG_FILE_NAME = "disabler-server.toml";
    private static final Pattern LEGACY_TOML_STRING_PATTERN = Pattern.compile("\"([^\"]+)\"");

    private static volatile Snapshot snapshot = Snapshot.defaults();

    private DisablerConfig() {}

    public static void load() {
        Path configPath = getConfigPath();

        try {
            ensureConfigExists(configPath);

            try (Reader reader = Files.newBufferedReader(configPath)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (!parsed.isJsonObject()) {
                    LOGGER.warn("Disabler config '{}' must contain a JSON object. Keeping previous config.", configPath);
                    return;
                }

                snapshot = parseSnapshot(parsed.getAsJsonObject());
                LOGGER.info("Loaded Disabler JSON config from {}", configPath);
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to load Disabler JSON config '{}'. Keeping previous config.", configPath, exception);
        }
    }

    public static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_NAME);
    }

    public static boolean hasBlockedMobs() {
        return !snapshot.blockedMobIds().isEmpty();
    }

    public static boolean hasBlockedStructures() {
        return !snapshot.blockedStructureIds().isEmpty();
    }

    public static boolean hasBlockedBiomes() {
        return !snapshot.blockedBiomeIds().isEmpty();
    }

    public static boolean hasBlockedItems() {
        return !snapshot.blockedItemIds().isEmpty();
    }

    public static boolean hasBlockedBiomeRules() {
        return hasBlockedBiomes();
    }

    public static boolean isBlockedMob(EntityType<?> entityType) {
        return snapshot.blockedMobIds().contains(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }

    public static boolean isBlockedStructure(ResourceLocation structureId) {
        return snapshot.blockedStructureIds().contains(structureId);
    }

    public static boolean isBlockedBiome(ResourceLocation biomeId) {
        return snapshot.blockedBiomeIds().contains(biomeId);
    }

    public static boolean isBlockedItem(Item item) {
        return snapshot.blockedItemIds().contains(BuiltInRegistries.ITEM.getKey(item));
    }

    public static Set<ResourceLocation> getBiomeExceptionIds() {
        return snapshot.biomeExceptionIds();
    }

    private static void ensureConfigExists(Path configPath) throws IOException {
        if (Files.exists(configPath)) {
            return;
        }

        Files.createDirectories(configPath.getParent());
        JsonObject config = createInitialConfig(configPath);
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(config, writer);
        }
        LOGGER.info("Created Disabler JSON config at {}", configPath);
    }

    private static JsonObject createInitialConfig(Path configPath) throws IOException {
        Path legacyConfigPath = configPath.resolveSibling(LEGACY_CONFIG_FILE_NAME);
        if (!Files.exists(legacyConfigPath)) {
            return defaultConfigJson();
        }

        JsonObject migrated = migrateLegacyToml(legacyConfigPath);
        LOGGER.info("Migrated Disabler config from '{}' to JSON format", legacyConfigPath);
        return migrated;
    }

    private static JsonObject defaultConfigJson() {
        JsonObject root = new JsonObject();
        root.add("blocked_mobs", new JsonArray());
        root.add("blocked_structures", new JsonArray());
        root.add("blocked_biomes", new JsonArray());
        root.add("blocked_items", new JsonArray());

        JsonArray exceptions = new JsonArray();
        for (String exception : DEFAULT_BIOME_EXCEPTIONS) {
            exceptions.add(exception);
        }
        root.add("biome_exceptions", exceptions);
        return root;
    }

    private static JsonObject migrateLegacyToml(Path legacyConfigPath) throws IOException {
        List<String> lines = Files.readAllLines(legacyConfigPath);
        JsonObject root = defaultConfigJson();

        copyLegacyArray(root, lines, "blocked_mobs", "blocked_mobs");
        copyLegacyArray(root, lines, "blocked_structures", "blocked_structures");
        copyLegacyArray(root, lines, "blocked_biomes", "blocked_biomes");
        copyLegacyArray(root, lines, "blocked_items", "blocked_items");
        copyLegacyArray(root, lines, "exceptions", "biome_exceptions");
        return root;
    }

    private static void copyLegacyArray(JsonObject root, List<String> lines, String legacyKey, String jsonKey) {
        List<String> values = readLegacyTomlArray(lines, legacyKey);
        if (values != null) {
            root.add(jsonKey, toJsonArray(values));
        }
    }

    private static List<String> readLegacyTomlArray(List<String> lines, String key) {
        for (String rawLine : lines) {
            String line = rawLine.split("#", 2)[0].trim();
            if (!line.startsWith(key) || line.indexOf('=') < 0) {
                continue;
            }

            int start = line.indexOf('[');
            int end = line.lastIndexOf(']');
            if (start < 0 || end < start) {
                return List.of();
            }

            List<String> values = new ArrayList<>();
            Matcher matcher = LEGACY_TOML_STRING_PATTERN.matcher(line.substring(start + 1, end));
            while (matcher.find()) {
                values.add(matcher.group(1));
            }
            return List.copyOf(values);
        }
        return null;
    }

    private static JsonArray toJsonArray(List<String> values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private static Snapshot parseSnapshot(JsonObject root) {
        return new Snapshot(
                parseLocations(readStringList(root, "blocked_mobs", "spawns", List.of()), "mob"),
                parseLocations(readStringList(root, "blocked_structures", "structures", List.of()), "structure"),
                parseLocations(readStringList(root, "blocked_biomes", "biomes", List.of()), "biome"),
                parseLocations(readStringList(root, "blocked_items", "items", List.of()), "item"),
                parseLocations(readBiomeExceptions(root), "biome exception"));
    }

    private static List<String> readBiomeExceptions(JsonObject root) {
        JsonElement element = root.get("biome_exceptions");
        if (element != null && element.isJsonObject()) {
            return readArray(element.getAsJsonObject().get("exceptions"), "biome_exceptions.exceptions", DEFAULT_BIOME_EXCEPTIONS);
        }
        return readArray(element, "biome_exceptions", DEFAULT_BIOME_EXCEPTIONS);
    }

    private static List<String> readStringList(JsonObject root, String key, String section, List<String> defaultValue) {
        JsonElement element = root.get(key);
        if (element == null && root.has(section) && root.get(section).isJsonObject()) {
            element = root.getAsJsonObject(section).get(key);
        }
        return readArray(element, key, defaultValue);
    }

    private static List<String> readArray(JsonElement element, String name, List<String> defaultValue) {
        if (element == null || element.isJsonNull()) {
            return defaultValue;
        }

        if (!element.isJsonArray()) {
            LOGGER.warn("Ignoring Disabler config key '{}' because it is not a JSON array", name);
            return defaultValue;
        }

        List<String> values = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString()) {
                LOGGER.warn("Ignoring non-string value '{}' in Disabler config key '{}'", entry, name);
                continue;
            }
            values.add(entry.getAsString());
        }
        return List.copyOf(values);
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

    private record Snapshot(
            Set<ResourceLocation> blockedMobIds,
            Set<ResourceLocation> blockedStructureIds,
            Set<ResourceLocation> blockedBiomeIds,
            Set<ResourceLocation> blockedItemIds,
            Set<ResourceLocation> biomeExceptionIds) {
        private static Snapshot defaults() {
            return new Snapshot(
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    parseLocations(DEFAULT_BIOME_EXCEPTIONS, "biome exception"));
        }
    }
}
