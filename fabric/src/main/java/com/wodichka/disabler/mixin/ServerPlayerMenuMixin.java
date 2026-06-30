package com.wodichka.disabler.mixin;

import com.wodichka.disabler.item.BlockedItemCleaner;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMenuMixin {
    @Inject(method = "initMenu", at = @At("RETURN"))
    private void disabler$sanitizeOpenedMenu(AbstractContainerMenu menu, CallbackInfo ci) {
        BlockedItemCleaner.sanitizeMenu(menu);
        BlockedItemCleaner.sanitizePlayer((ServerPlayer) (Object) this);
    }

    @Inject(method = "doCloseContainer", at = @At("HEAD"))
    private void disabler$sanitizeClosingMenu(CallbackInfo ci) {
        BlockedItemCleaner.sanitizePlayer((ServerPlayer) (Object) this);
    }
}
