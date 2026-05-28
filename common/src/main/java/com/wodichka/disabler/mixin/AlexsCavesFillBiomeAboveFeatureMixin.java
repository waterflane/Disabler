package com.wodichka.disabler.mixin;

import com.wodichka.disabler.world.BiomeRemovalResolver;
import java.lang.reflect.Field;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.github.alexmodguy.alexscaves.server.level.feature.FillBiomeAboveFeature", remap = false)
public abstract class AlexsCavesFillBiomeAboveFeatureMixin {
    @Unique
    private static volatile Field disabler$newBiomeField;

    @Inject(method = "place", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void disabler$skipBlockedBiomeRewrite(FeaturePlaceContext<?> context, CallbackInfoReturnable<Boolean> cir) {
        Holder<Biome> newBiome = disabler$getConfiguredNewBiome(context.config());
        if (newBiome != null && BiomeRemovalResolver.isBlocked(newBiome)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static Holder<Biome> disabler$getConfiguredNewBiome(Object config) {
        if (config == null) {
            return null;
        }

        try {
            Field field = disabler$newBiomeField;
            if (field == null || field.getDeclaringClass() != config.getClass()) {
                field = config.getClass().getField("newBiome");
                disabler$newBiomeField = field;
            }

            Object value = field.get(config);
            if (value instanceof Holder<?> holder && holder.value() instanceof Biome) {
                return (Holder<Biome>) holder;
            }
        } catch (ReflectiveOperationException ignored) {
            return null;
        }

        return null;
    }
}
