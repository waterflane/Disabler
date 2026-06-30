package com.wodichka.disabler.mixin;

import com.wodichka.disabler.item.BlockedItemCleaner;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "noobanidus.mods.lootr.common.data.LootrSavedData", remap = false)
public abstract class LootrSavedDataMixin {
    @Inject(method = "createInventory", at = @At("RETURN"), require = 0)
    private void disabler$removeBlockedItemsFromGeneratedLootrInventory(
            @Coerce Object provider,
            ServerPlayer player,
            @Coerce Object filler,
            CallbackInfoReturnable<Object> cir
    ) {
        Object inventory = cir.getReturnValue();
        if (inventory instanceof Container container) {
            BlockedItemCleaner.sanitizeContainer(container);
        }
    }
}
