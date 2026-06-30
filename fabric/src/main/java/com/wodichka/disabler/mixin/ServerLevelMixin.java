package com.wodichka.disabler.mixin;

import com.wodichka.disabler.config.DisablerConfig;
import com.wodichka.disabler.item.BlockedItemCleaner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void disabler$blockEntityAdd(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Mob mob && DisablerConfig.isBlockedMob(mob.getType())) {
            cir.setReturnValue(false);
        } else if (entity instanceof ItemEntity item && BlockedItemCleaner.isBlocked(item.getItem())) {
            cir.setReturnValue(false);
        }
    }
}
