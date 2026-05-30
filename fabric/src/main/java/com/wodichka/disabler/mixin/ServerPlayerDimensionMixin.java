package com.wodichka.disabler.mixin;

import com.wodichka.disabler.config.DisablerConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDimensionMixin {
    @Unique
    private static final int DISABLER$PLAYER_WARNING_COOLDOWN_TICKS = 40;

    @Unique
    private static final int DISABLER$BLOCKED_TRAVEL_COOLDOWN_TICKS = 100;

    @Unique
    private static final Map<UUID, Long> DISABLER$LAST_WARNING_GAME_TIME = new HashMap<>();

    @Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true)
    private void disabler$blockPlayerDimensionTravel(ServerLevel destination, CallbackInfoReturnable<Entity> cir) {
        if (!DisablerConfig.isBlockedDimension(destination.dimension())) {
            return;
        }

        ServerPlayer player = (ServerPlayer) (Object) this;
        player.setPortalCooldown(Math.max(player.getPortalCooldown(), DISABLER$BLOCKED_TRAVEL_COOLDOWN_TICKS));
        disabler$warnPlayer(player, destination.dimension().location().toString());
        cir.setReturnValue(null);
    }

    @Unique
    private static void disabler$warnPlayer(ServerPlayer player, String dimensionId) {
        long gameTime = player.serverLevel().getGameTime();
        UUID playerId = player.getUUID();
        Long lastWarning = DISABLER$LAST_WARNING_GAME_TIME.get(playerId);
        if (lastWarning != null && gameTime - lastWarning < DISABLER$PLAYER_WARNING_COOLDOWN_TICKS) {
            return;
        }

        DISABLER$LAST_WARNING_GAME_TIME.put(playerId, gameTime);
        player.displayClientMessage(Component.literal("Dimension is disabled: " + dimensionId), true);
    }
}
