package com.wodichka.disabler.mixin;

import com.wodichka.disabler.config.DisablerConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
    @Inject(method = "getMobsAt", at = @At("RETURN"), cancellable = true)
    private void disabler$filterMobSpawns(Holder<Biome> biome, StructureManager structureManager, MobCategory category, BlockPos pos, CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir) {
        if (!DisablerConfig.hasBlockedMobs() && !DisablerConfig.hasBlockedBiomes()) {
            return;
        }

        ResourceLocation biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        if (biomeId != null && DisablerConfig.isBlockedBiome(biomeId)) {
            cir.setReturnValue(WeightedRandomList.create(List.of()));
            return;
        }

        List<MobSpawnSettings.SpawnerData> filtered = new ArrayList<>();
        for (MobSpawnSettings.SpawnerData spawn : cir.getReturnValue().unwrap()) {
            if (!DisablerConfig.isBlockedMob(spawn.type)) {
                filtered.add(spawn);
            }
        }
        cir.setReturnValue(WeightedRandomList.create(filtered));
    }

    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void disabler$skipBlockedStructure(StructureSet.StructureSelectionEntry entry, StructureManager structureManager, RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager templateManager, long seed, ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos, CallbackInfoReturnable<Boolean> cir) {
        ResourceLocation structureId = entry.structure().unwrapKey().map(ResourceKey::location).orElse(null);
        if (structureId != null && DisablerConfig.isBlockedStructure(structureId)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyArg(
            method = "tryGenerateStructure",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/Structure;generate(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;JLnet/minecraft/world/level/ChunkPos;ILnet/minecraft/world/level/LevelHeightAccessor;Ljava/util/function/Predicate;)Lnet/minecraft/world/level/levelgen/structure/StructureStart;"),
            index = 9)
    private Predicate<Holder<Biome>> disabler$filterStructureBiomes(Predicate<Holder<Biome>> original) {
        if (!DisablerConfig.hasBlockedBiomes()) {
            return original;
        }
        return biome -> original.test(biome) && biome.unwrapKey()
                .map(ResourceKey::location)
                .map(id -> !DisablerConfig.isBlockedBiome(id))
                .orElse(true);
    }
}
