package com.wodichka.disabler.world;

import com.mojang.datafixers.util.Pair;
import com.wodichka.disabler.config.DisablerConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

public final class BiomeRemovalResolver {
    private BiomeRemovalResolver() {}

    public static Holder<Biome> resolveReplacement(Holder<Biome> selectedBiome, List<Holder<Biome>> possibleBiomes) {
        if (!DisablerConfig.hasBlockedBiomes() || !isBlocked(selectedBiome)) {
            return selectedBiome;
        }

        for (Holder<Biome> candidate : possibleBiomes) {
            if (!isBlocked(candidate)) {
                return candidate;
            }
        }

        return selectedBiome;
    }

    public static List<Holder<Biome>> collectAllowedBiomes(Climate.ParameterList<Holder<Biome>> parameterList) {
        List<Holder<Biome>> allowedBiomes = new ArrayList<>();
        for (Pair<Climate.ParameterPoint, Holder<Biome>> entry : parameterList.values()) {
            Holder<Biome> biome = entry.getSecond();
            ResourceLocation biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
            if (!isBlocked(biome) && biomeId != null && !getBiomeExceptionIds().contains(biomeId)) {
                allowedBiomes.add(biome);
            }
        }
        return List.copyOf(allowedBiomes);
    }

    private static Set<ResourceLocation> getBiomeExceptionIds() {
        return DisablerConfig.getBiomeExceptionIds();
    }

    public static boolean isBlocked(Holder<Biome> biome) {
        ResourceLocation biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        return biomeId != null && DisablerConfig.isBlockedBiome(biomeId);
    }
}