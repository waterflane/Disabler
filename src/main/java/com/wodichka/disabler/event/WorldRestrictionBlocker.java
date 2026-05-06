package com.wodichka.disabler.event;

import com.wodichka.disabler.config.DisablerConfig;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;

public final class WorldRestrictionBlocker {
    private static final Component BLOCKED_DESTINATION_MESSAGE = Component.literal("This destination is disabled on this server.");

    @SubscribeEvent
    public void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (isBlocked(event.getEntity().level(), event.getEntity().blockPosition())) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.getEntity() instanceof Player) {
            return;
        }

        if (DisablerConfig.isBlockedDimension(event.getLevel().dimension())) {
            event.setCanceled(true);
            event.getEntity().discard();
            return;
        }

        if (isBlockedBiome(event.getLevel(), event.getEntity().blockPosition())) {
            event.setCanceled(true);
            event.getEntity().discard();
        }
    }

    @SubscribeEvent
    public void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (DisablerConfig.isBlockedDimension(event.getDimension())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            relocateIfBlocked(player);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            relocateIfBlocked(player);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawnPosition(PlayerRespawnPositionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        DimensionTransition transition = event.getDimensionTransition();
        if (!isBlocked(transition.newLevel(), BlockPos.containing(transition.pos()))) {
            return;
        }

        ServerLevel fallbackLevel = findFallbackLevel(player.serverLevel());
        if (fallbackLevel == null) {
            return;
        }

        Vec3 spawnPosition = fallbackSpawnPosition(fallbackLevel);
        event.setDimensionTransition(new DimensionTransition(
                fallbackLevel,
                spawnPosition,
                transition.speed(),
                transition.yRot(),
                transition.xRot(),
                transition.missingRespawnBlock(),
                transition.postDimensionTransition()));
        event.setCopyOriginalSpawnPosition(false);
    }

    @SubscribeEvent
    public void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        BlockPos newSpawn = event.getNewSpawn();
        if (newSpawn == null || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel spawnLevel = player.server.getLevel(event.getSpawnLevel());
        if (spawnLevel != null && isBlocked(spawnLevel, newSpawn)) {
            event.setCanceled(true);
            player.displayClientMessage(BLOCKED_DESTINATION_MESSAGE, false);
        }
    }

    private static boolean isBlocked(Level level, BlockPos pos) {
        return DisablerConfig.isBlockedDimension(level.dimension()) || isBlockedBiome(level, pos);
    }

    private static boolean isBlockedBiome(Level level, BlockPos pos) {
        return DisablerConfig.isBlockedBiome(level.getBiome(pos));
    }

    private static void relocateIfBlocked(ServerPlayer player) {
        if (!isBlocked(player.level(), player.blockPosition())) {
            return;
        }

        ServerLevel fallbackLevel = findFallbackLevel(player.serverLevel());
        if (fallbackLevel == null) {
            return;
        }

        player.changeDimension(new DimensionTransition(
                fallbackLevel,
                fallbackSpawnPosition(fallbackLevel),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
            DimensionTransition.DO_NOTHING));
        player.displayClientMessage(BLOCKED_DESTINATION_MESSAGE, false);
    }

    @Nullable
    private static ServerLevel findFallbackLevel(ServerLevel currentLevel) {
        ServerLevel overworld = currentLevel.getServer().getLevel(Level.OVERWORLD);
        if (isSafeFallbackLevel(overworld)) {
            return overworld;
        }

        for (ServerLevel level : currentLevel.getServer().getAllLevels()) {
            if (isSafeFallbackLevel(level)) {
                return level;
            }
        }

        return null;
    }

    private static Vec3 fallbackSpawnPosition(ServerLevel level) {
        BlockPos spawnPos = level.getSharedSpawnPos();
        return new Vec3(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
    }

    private static boolean isSafeFallbackLevel(@Nullable ServerLevel level) {
        return level != null && !isBlocked(level, level.getSharedSpawnPos());
    }
}