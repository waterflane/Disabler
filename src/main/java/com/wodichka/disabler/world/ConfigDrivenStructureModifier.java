package com.wodichka.disabler.world;

import com.wodichka.disabler.config.DisablerConfig;
import com.mojang.serialization.MapCodec;
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
            settingsBuilder.setBiomes(HolderSet.<Biome>empty());
            for (MobCategory category : MobCategory.values()) {
                settingsBuilder.removeSpawnOverrides(category);
            }
            return;
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

    @Override
    public MapCodec<? extends StructureModifier> codec() {
        return DisablerModifiers.CONFIG_STRUCTURE_BLOCKER.get();
    }
}