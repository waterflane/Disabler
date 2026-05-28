package com.wodichka.disabler.mixin;

import com.wodichka.disabler.config.DisablerConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.level.structure.piece.AbstractCaveGenerationStructurePiece", remap = false)
public abstract class AlexsCavesCaveGenerationStructurePieceMixin {
    @Inject(method = "replaceBiomes", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void disabler$skipBlockedBiomeRewrite(WorldGenLevel level, ResourceKey<Biome> with, int belowLevel, CallbackInfo ci) {
        if (DisablerConfig.isBlockedBiome(with.location())) {
            ci.cancel();
        }
    }
}
