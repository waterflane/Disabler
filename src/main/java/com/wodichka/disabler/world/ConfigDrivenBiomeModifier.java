package com.wodichka.disabler.world;

import com.wodichka.disabler.config.DisablerConfig;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.neoforged.neoforge.common.world.BiomeGenerationSettingsBuilder;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public enum ConfigDrivenBiomeModifier implements BiomeModifier {
    INSTANCE;

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.REMOVE) {
            return;
        }

        if (DisablerConfig.isBlockedBiome(biome)) {
            clearBiome(builder);
            return;
        }

        if (!DisablerConfig.hasBlockedMobs()) {
            return;
        }

        MobSpawnSettingsBuilder spawnBuilder = builder.getMobSpawnSettings();
        for (MobCategory category : MobCategory.values()) {
            var spawns = spawnBuilder.getSpawner(category);
            spawns.removeIf(spawnerData -> DisablerConfig.isBlockedMob(spawnerData.type));
        }
    }

    private static void clearBiome(ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        MobSpawnSettingsBuilder spawnBuilder = builder.getMobSpawnSettings();
        for (MobCategory category : MobCategory.values()) {
            spawnBuilder.getSpawner(category).removeIf(spawnerData -> true);
        }

        for (EntityType<?> entityType : List.copyOf(spawnBuilder.getEntityTypes())) {
            spawnBuilder.removeSpawnCost(entityType);
        }

        BiomeGenerationSettingsBuilder generationBuilder = builder.getGenerationSettings();
        for (GenerationStep.Carving step : GenerationStep.Carving.values()) {
            generationBuilder.getCarvers(step).clear();
        }
        for (Decoration step : Decoration.values()) {
            generationBuilder.getFeatures(step).clear();
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return DisablerModifiers.CONFIG_SPAWN_BLOCKER.get();
    }
}