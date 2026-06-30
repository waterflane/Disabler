package com.wodichka.disabler.world;

import com.wodichka.disabler.config.DisablerConfig;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.world.ModifiableStructureInfo;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.common.world.StructureSettingsBuilder;

public enum ConfigDrivenStructureModifier implements StructureModifier {
    INSTANCE;

    @Override
    public void modify(Holder<Structure> structure, Phase phase, ModifiableStructureInfo.StructureInfo.Builder builder) {
        if (phase != Phase.REMOVE) {
            return;
        }

        ResourceLocation structureId = structure.unwrapKey().map(ResourceKey::location).orElse(null);
        StructureSettingsBuilder settingsBuilder = builder.getStructureSettings();

        if (structureId != null && DisablerConfig.isBlockedStructure(structureId)) {
            clearStructure(settingsBuilder);
            return;
        }

        if (DisablerConfig.hasBlockedBiomes()) {
            List<Holder<Biome>> allowedBiomes = new ArrayList<>();
            for (Holder<Biome> biome : settingsBuilder.getBiomes()) {
                ResourceLocation biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
                if (biomeId != null && !DisablerConfig.isBlockedBiome(biomeId)) {
                    allowedBiomes.add(biome);
                }
            }

            if (allowedBiomes.isEmpty()) {
                clearStructure(settingsBuilder);
                return;
            }

            settingsBuilder.setBiomes(HolderSet.direct(allowedBiomes));
        }

        if (!DisablerConfig.hasBlockedMobs()) {
            return;
        }

        for (MobCategory category : MobCategory.values()) {
            var overrides = settingsBuilder.getSpawnOverrides(category);
            if (overrides == null || overrides.getSpawns().isEmpty()) {
                continue;
            }
            overrides.removeSpawns(spawnerData -> DisablerConfig.isBlockedMob(spawnerData.type));
        }
    }

    private static void clearStructure(StructureSettingsBuilder settingsBuilder) {
        settingsBuilder.setBiomes(HolderSet.empty());
        for (MobCategory category : MobCategory.values()) {
            settingsBuilder.removeSpawnOverrides(category);
        }
    }

    @Override
    public MapCodec<? extends StructureModifier> codec() {
        return DisablerModifiers.CONFIG_STRUCTURE_BLOCKER.get();
    }
}