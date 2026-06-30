package com.wodichka.disabler.mixin;

import com.mojang.datafixers.util.Either;
import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.world.BiomeRemovalResolver;
import com.wodichka.disabler.world.BiomeRemovalResolver.Candidate;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MultiNoiseBiomeSource.class, priority = -70000)
public abstract class ForgeMultiNoiseBiomeSourceMixin {
    @Shadow(remap = false)
    @Final
    private Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> f_48435_;

    @Unique
    private volatile List<Candidate> disabler$allowedBiomes;

    @Inject(
            method = "m_203407_(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private void disabler$replaceBlockedBiome(
            int quartX,
            int quartY,
            int quartZ,
            Climate.Sampler sampler,
            CallbackInfoReturnable<Holder<Biome>> cir) {
        disabler$replaceBlockedReturnValue(sampler.sample(quartX, quartY, quartZ), cir);
    }

    @Inject(
            method = "m_204269_(Lnet/minecraft/world/level/biome/Climate$TargetPoint;)Lnet/minecraft/core/Holder;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false)
    private void disabler$replaceBlockedBiomeFromTarget(
            Climate.TargetPoint targetPoint,
            CallbackInfoReturnable<Holder<Biome>> cir) {
        disabler$replaceBlockedReturnValue(targetPoint, cir);
    }

    @Unique
    private void disabler$replaceBlockedReturnValue(
            Climate.TargetPoint targetPoint,
            CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!DisablerConfig.hasBlockedBiomes()) {
            return;
        }

        Holder<Biome> selectedBiome = cir.getReturnValue();
        if (selectedBiome == null || !BiomeRemovalResolver.isBlocked(selectedBiome)) {
            return;
        }

        List<Candidate> allowedBiomes = disabler$getAllowedBiomes();
        if (!allowedBiomes.isEmpty()) {
            cir.setReturnValue(BiomeRemovalResolver.resolveReplacement(selectedBiome, targetPoint, allowedBiomes));
        }
    }

    @Unique
    private List<Candidate> disabler$getAllowedBiomes() {
        List<Candidate> cached = this.disabler$allowedBiomes;
        if (cached == null) {
            Climate.ParameterList<Holder<Biome>> parameterList = this.f_48435_.map(
                    directParameters -> directParameters,
                    presetParameters -> presetParameters.value().parameters());
            cached = BiomeRemovalResolver.collectAllowedBiomes(parameterList);
            this.disabler$allowedBiomes = cached;
        }
        return cached;
    }
}
