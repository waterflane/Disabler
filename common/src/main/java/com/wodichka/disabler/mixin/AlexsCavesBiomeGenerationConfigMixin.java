package com.wodichka.disabler.mixin;

import com.wodichka.disabler.config.DisablerConfig;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.config.BiomeGenerationConfig", remap = false)
public abstract class AlexsCavesBiomeGenerationConfigMixin {
    @Unique
    private static volatile Map<ResourceKey<Biome>, ?> disabler$cachedSource;

    @Unique
    private static volatile Set<ResourceLocation> disabler$cachedBlockedBiomes = Set.of();

    @Unique
    private static volatile Map<ResourceKey<Biome>, ?> disabler$cachedFiltered;

    @Inject(method = "getBiomesSnapshot", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void disabler$removeBlockedBiomesFromAlexsCavesSnapshot(CallbackInfoReturnable<Map<ResourceKey<Biome>, ?>> cir) {
        if (!DisablerConfig.hasBlockedBiomes()) {
            return;
        }

        Map<ResourceKey<Biome>, ?> source = cir.getReturnValue();
        if (source == null || source.isEmpty()) {
            return;
        }

        Set<ResourceLocation> blockedBiomes = DisablerConfig.getBlockedBiomeIds();
        Map<ResourceKey<Biome>, ?> cached = disabler$cachedFiltered;
        if (source == disabler$cachedSource && blockedBiomes.equals(disabler$cachedBlockedBiomes) && cached != null) {
            cir.setReturnValue(cached);
            return;
        }

        boolean hasBlockedEntry = false;
        for (ResourceKey<Biome> biomeKey : source.keySet()) {
            if (blockedBiomes.contains(biomeKey.location())) {
                hasBlockedEntry = true;
                break;
            }
        }

        Map<ResourceKey<Biome>, ?> filtered = source;
        if (hasBlockedEntry) {
            LinkedHashMap<ResourceKey<Biome>, Object> mutableFiltered = new LinkedHashMap<>();
            for (Map.Entry<ResourceKey<Biome>, ?> entry : source.entrySet()) {
                if (!blockedBiomes.contains(entry.getKey().location())) {
                    mutableFiltered.put(entry.getKey(), entry.getValue());
                }
            }
            filtered = Collections.unmodifiableMap(mutableFiltered);
        }

        disabler$cachedSource = source;
        disabler$cachedBlockedBiomes = blockedBiomes;
        disabler$cachedFiltered = filtered;
        cir.setReturnValue(filtered);
    }
}
