package com.wodichka.disabler.mixin;

import com.wodichka.disabler.config.DisablerConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.world.level.block.BaseFireBlock;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {
    @Redirect(
            method = "onPlace",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/PortalShape;createPortalBlocks()V"))
    private void disabler$skipNetherPortalCreation(PortalShape portalShape) {
        if (!DisablerConfig.isBlockedDimension(Level.NETHER)) {
            portalShape.createPortalBlocks();
        }
    }
}
