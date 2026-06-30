package com.wodichka.disabler.mixin;

import com.wodichka.disabler.config.DisablerConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDimensionMixin {
    @Unique private static final int DISABLER$WARNING_COOLDOWN_TICKS = 40;
    @Unique private static final int DISABLER$TRAVEL_COOLDOWN_TICKS = 100;
    @Unique private static final Map<UUID, Long> DISABLER$LAST_WARNING = new HashMap<>();

    @Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true)
    private void disabler$blockPlayerDimensionTravel(DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
        if (!DisablerConfig.isBlockedDimension(transition.newLevel().dimension())) {
            return;
        }

        ServerPlayer player = (ServerPlayer) (Object) this;
        player.setPortalCooldown(Math.max(player.getPortalCooldown(), DISABLER$TRAVEL_COOLDOWN_TICKS));
        player.portalProcess = null;
        disabler$warn(player, transition.newLevel().dimension().location().toString());
        cir.setReturnValue(null);
    }

    @Unique
    private static void disabler$warn(ServerPlayer player, String dimensionId) {
        long gameTime = player.serverLevel().getGameTime();
        Long lastWarning = DISABLER$LAST_WARNING.get(player.getUUID());
        if (lastWarning != null && gameTime - lastWarning < DISABLER$WARNING_COOLDOWN_TICKS) {
            return;
        }
        DISABLER$LAST_WARNING.put(player.getUUID(), gameTime);
        player.displayClientMessage(Component.literal("Dimension is disabled: " + dimensionId), true);
    }
}
