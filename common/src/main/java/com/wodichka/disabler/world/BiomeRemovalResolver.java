package com.wodichka.disabler.world;

import com.mojang.datafixers.util.Pair;
import com.wodichka.disabler.config.DisablerConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

public final class BiomeRemovalResolver {
    private BiomeRemovalResolver() {}

    public static Holder<Biome> resolveReplacement(Holder<Biome> selectedBiome, Climate.TargetPoint targetPoint, List<Candidate> possibleBiomes) {
        if (!DisablerConfig.hasBlockedBiomes() || !isBlocked(selectedBiome)) {
            return selectedBiome;
        }

        SurfaceKind selectedKind = SurfaceKind.fromBiome(selectedBiome);
        return possibleBiomes.stream()
                .filter(candidate -> candidate.surfaceKind() == selectedKind)
                .min(Comparator.comparingLong(candidate -> candidate.distanceTo(targetPoint)))
                .map(Candidate::biome)
                .orElse(selectedBiome);
    }

    public static List<Candidate> collectAllowedBiomes(Climate.ParameterList<Holder<Biome>> parameterList) {
        List<Candidate> allowedBiomes = new ArrayList<>();
        for (Pair<Climate.ParameterPoint, Holder<Biome>> entry : parameterList.values()) {
            Holder<Biome> biome = entry.getSecond();
            ResourceLocation biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
            if (!isBlocked(biome) && biomeId != null && !getBiomeExceptionIds().contains(biomeId)) {
                allowedBiomes.add(new Candidate(biome, entry.getFirst(), SurfaceKind.fromId(biomeId)));
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

    public record Candidate(Holder<Biome> biome, Climate.ParameterPoint parameterPoint, SurfaceKind surfaceKind) {
        private long distanceTo(Climate.TargetPoint targetPoint) {
            return square(parameterPoint.temperature().distance(targetPoint.temperature()))
                    + square(parameterPoint.humidity().distance(targetPoint.humidity()))
                    + square(parameterPoint.continentalness().distance(targetPoint.continentalness()))
                    + square(parameterPoint.erosion().distance(targetPoint.erosion()))
                    + square(parameterPoint.depth().distance(targetPoint.depth()))
                    + square(parameterPoint.weirdness().distance(targetPoint.weirdness()));
        }

        private static long square(long value) {
            return value * value;
        }
    }

    public enum SurfaceKind {
        LAND,
        WATER;

        private static SurfaceKind fromBiome(Holder<Biome> biome) {
            ResourceLocation biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
            return biomeId == null ? LAND : fromId(biomeId);
        }

        private static SurfaceKind fromId(ResourceLocation biomeId) {
            String path = biomeId.getPath().toLowerCase(Locale.ROOT);
            if (path.contains("ocean")
                    || path.contains("river")
                    || path.contains("lake")
                    || path.contains("water")
                    || path.contains("reef")
                    || path.contains("kelp")
                    || path.contains("coral")) {
                return WATER;
            }
            return LAND;
        }
    }
}
