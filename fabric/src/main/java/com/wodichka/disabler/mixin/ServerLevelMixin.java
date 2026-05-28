package com.wodichka.disabler.mixin;

import com.wodichka.disabler.config.DisablerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void disabler$blockMobEntityAdd(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Mob mob && DisablerConfig.isBlockedMob(mob.getType())) {
            cir.setReturnValue(false);
        }
    }
}
