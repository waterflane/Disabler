package com.wodichka.disabler.mixin;

import com.wodichka.disabler.item.BlockedItemCleaner;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void disabler$blockPickup(Player player, CallbackInfo ci) {
        ItemEntity item = (ItemEntity) (Object) this;
        if (BlockedItemCleaner.isBlocked(item.getItem())) {
            item.discard();
            ci.cancel();
        }
    }
}
