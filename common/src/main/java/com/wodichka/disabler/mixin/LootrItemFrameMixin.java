package com.wodichka.disabler.mixin;

import com.wodichka.disabler.item.BlockedItemCleaner;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "noobanidus.mods.lootr.common.entity.LootrItemFrame", remap = false)
public abstract class LootrItemFrameMixin {
    @ModifyVariable(method = "lootrSetItem", at = @At("HEAD"), argsOnly = true, require = 0)
    private ItemStack disabler$removeBlockedConvertedFrameItem(ItemStack stack) {
        return BlockedItemCleaner.isBlocked(stack) ? ItemStack.EMPTY : stack;
    }
}
