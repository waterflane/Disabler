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

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_MOBS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_STRUCTURES;

    private static volatile List<String> mobSnapshot = List.of();
    private static volatile Set<ResourceLocation> blockedMobIds = Set.of();
    private static volatile List<String> structureSnapshot = List.of();
    private static volatile Set<ResourceLocation> blockedStructureIds = Set.of();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("spawns");
        BLOCKED_MOBS = builder
                .comment(
                        "List of mob ids that should never appear in the world.",
                        "Examples: minecraft:zombie, minecraft:creeper")
                .defineListAllowEmpty("blocked_mobs", List::of, value -> value instanceof String);
        builder.pop();

        builder.push("structures");
        BLOCKED_STRUCTURES = builder
                .comment(
                        "List of structure ids that should be removed from world generation.",
                        "Examples: minecraft:village_plains, minecraft:mineshaft")
                .defineListAllowEmpty("blocked_structures", List::of, value -> value instanceof String);
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

    public static boolean isBlockedMob(EntityType<?> entityType) {
        return getBlockedMobIds().contains(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
    }

    public static boolean isBlockedStructure(ResourceLocation structureId) {
        return getBlockedStructureIds().contains(structureId);
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