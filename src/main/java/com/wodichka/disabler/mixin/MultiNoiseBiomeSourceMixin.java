package com.wodichka.disabler.mixin;

import com.mojang.datafixers.util.Either;
import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.world.BiomeRemovalResolver;
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

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
    @Shadow
    @Final
    private Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters;

    @Unique
    private volatile List<Holder<Biome>> disabler$allowedBiomes;

    @Inject(method = "getNoiseBiome", at = @At("RETURN"), cancellable = true)
    private void disabler$replaceBlockedBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!DisablerConfig.hasBlockedBiomes()) {
            return;
        }

        Holder<Biome> selectedBiome = cir.getReturnValue();
        if (!BiomeRemovalResolver.isBlocked(selectedBiome)) {
            return;
        }

        List<Holder<Biome>> allowedBiomes = disabler$getAllowedBiomes();
        if (!allowedBiomes.isEmpty()) {
            cir.setReturnValue(BiomeRemovalResolver.resolveReplacement(selectedBiome, allowedBiomes));
        }
    }

    @Unique
    private List<Holder<Biome>> disabler$getAllowedBiomes() {
        List<Holder<Biome>> cached = this.disabler$allowedBiomes;
        if (cached == null) {
            Climate.ParameterList<Holder<Biome>> parameterList = this.parameters.map(
                    directParameters -> directParameters,
                    presetParameters -> presetParameters.value().parameters());
            cached = BiomeRemovalResolver.collectAllowedBiomes(parameterList);
            this.disabler$allowedBiomes = cached;
        }
        return cached;
    }
}