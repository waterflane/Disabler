package com.wodichka.disabler.mixin;

import com.wodichka.disabler.item.BlockedItemCleaner;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceResultSlot.class)
public abstract class FurnaceResultSlotMixin {
    @Inject(method = "onTake", at = @At("RETURN"))
    private void disabler$sanitizeSmeltResult(Player player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            BlockedItemCleaner.sanitizePlayer(serverPlayer);
        }
    }
}
