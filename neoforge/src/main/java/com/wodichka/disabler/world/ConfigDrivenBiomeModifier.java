package com.wodichka.disabler.world;

import com.wodichka.disabler.config.DisablerConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
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

        ResourceLocation biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        boolean blockedBiome = biomeId != null && DisablerConfig.isBlockedBiome(biomeId);

        if (!blockedBiome && !DisablerConfig.hasBlockedMobs()) {
            return;
        }

        MobSpawnSettingsBuilder spawnBuilder = builder.getMobSpawnSettings();
        for (MobCategory category : MobCategory.values()) {
            var spawns = spawnBuilder.getSpawner(category);
            if (blockedBiome) {
                spawns.clear();
            } else {
                spawns.removeIf(spawnerData -> DisablerConfig.isBlockedMob(spawnerData.type));
            }
        }

    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return DisablerModifiers.CONFIG_SPAWN_BLOCKER.get();
    }
}