package com.wodichka.disabler.event;

import com.wodichka.disabler.config.DisablerConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DimensionTravelBlockerForge {
    private static final int PLAYER_WARNING_COOLDOWN_TICKS = 40;
    private static final int BLOCKED_TRAVEL_COOLDOWN_TICKS = 100;

    private final Map<UUID, Long> lastWarningGameTime = new HashMap<>();

    @SubscribeEvent
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!DisablerConfig.isBlockedDimension(event.getDimension())) {
            return;
        }

        Entity entity = event.getEntity();
        event.setCanceled(true);
        entity.setPortalCooldown(Math.max(entity.getPortalCooldown(), BLOCKED_TRAVEL_COOLDOWN_TICKS));
        entity.portalProcess = null;

        if (entity instanceof ServerPlayer player) {
            warnPlayer(player, event.getDimension().location());
        }
    }

    @SubscribeEvent
    public void onPortalSpawn(BlockEvent.PortalSpawnEvent event) {
        if (DisablerConfig.isBlockedDimension(Level.NETHER)) {
            event.setCanceled(true);
        }
    }

    private void warnPlayer(ServerPlayer player, ResourceLocation dimensionId) {
        long gameTime = player.serverLevel().getGameTime();
        UUID playerId = player.getUUID();
        Long lastWarning = this.lastWarningGameTime.get(playerId);
        if (lastWarning != null && gameTime - lastWarning < PLAYER_WARNING_COOLDOWN_TICKS) {
            return;
        }

        this.lastWarningGameTime.put(playerId, gameTime);
        player.displayClientMessage(Component.literal("Dimension is disabled: " + dimensionId), true);
    }
}
